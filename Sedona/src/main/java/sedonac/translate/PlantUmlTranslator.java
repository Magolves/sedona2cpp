package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;

import java.io.File;
import java.util.*;

import static sedonac.translate.TranslationUtil.isCppType;

public class PlantUmlTranslator extends AbstractCppKitTranslator {
    private List<Association>  associations = new ArrayList<Association>();

    public PlantUmlTranslator(Compiler compiler, KitDef kit) {
        super(compiler, kit);
    }

    @Override
    public File toFile() {
        File dir = new File(outDir + File.separator + kit.name);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return new File(dir, kit.name + ".puml");
    }

    @Override
    public void doTranslate() {
        w("@startuml").nl();
        for (int i = 0; i < compiler.ast.types.length; i++) {
            TypeDef typeDef = compiler.ast.types[i];
            final String className = typeDef.qname();
            associations.clear();

            if (!acceptType(typeDef)) continue;

            w("class ");
            w(className).w(" {").nl();

            indent++;
            w("__ Fields __").nl();
            writeFields(typeDef);
            w("__ Methods __").nl();
            writeMethods(typeDef, null,       m -> !m.isNative() && !m.isVirtual());
            writeMethods(typeDef, "Actions/Virtuals",  m -> !m.isNative() &&  m.isVirtual());
            writeMethods(typeDef, "Natives",  m ->  m.isNative() && !m.isVirtual());

            indent--;

            w("}").nl().w("' end of type ").w(className).nl().nl();

            w("' Associations of ").w(className).nl().nl();
            nl();
            for (int j = 0; j < associations.size(); j++) {
                Association association = associations.get(j);
                w(association.renderAssociation()).nl();
            }
            nl();
        }

        // Derives
        w("' Relations ------------------").nl();
        for (int i = 0; i < kit.types.length; i++) {
            TypeDef typeDef = kit.types[i];
            final String className = typeDef.qname();

            if (typeDef.base != null) {
                w(className).w(" -> ").w(typeDef.base.qname()).nl();
            }
        }



        w("@enduml").nl();
    }

    private void writeFields(TypeDef typeDef) {
        FieldDef[] fieldDefs = typeDef.fieldDefs();
        Arrays.sort(fieldDefs, new Comparator<FieldDef>() {
            @Override
            public int compare(FieldDef o1, FieldDef o2) {
                return Integer.compare(o1.flags, o2.flags);
            }
        });

        for (int i = 0; i < fieldDefs.length; i++) {
            FieldDef fieldDef = fieldDefs[i];

            // Omit inherited fields
            if (typeDef.base != null && fieldDef.isInherited(typeDef.base)) {
                continue;
            }

            // Association?
            if (isAssociation(fieldDef)) {
                // Assemble asociation and store it for later use
                Association association = new Association(toType(typeDef), toType(fieldDef.type), fieldDef.name, fieldDef.type.isArray(), fieldDef.isInline(), true);
                associations.add(association);
            }

            indent().w("{field} ");
            if (fieldDef.isAbstract()) {
                w("{abstract} ");
            }
            if (fieldDef.isStatic()) {
                w("{static} ");
            }
            w(getAccessModifier(fieldDef)).w(fieldDef.name).w(": ").w(fieldDef.type.qname());
            if (fieldDef.isDefine() && fieldDef.init != null) {
                w(" = ");
                w(fieldDef.init.toCodeString());
            }
            nl();
        }
    }

    /**
     * Checks if given field represents an association (with another type).
     * @param fieldDef the field to checkl
     * @return true, if given field denotes an association (insteaf of a member)
     */
    private boolean isAssociation(FieldDef fieldDef) {
        final Type type = fieldDef.type;
        boolean isPrimitive = type.isPrimitive() || (type.isArray() && type.arrayOf().isPrimitive());
        boolean isStr = type.isStr() || (type.isArray() && type.arrayOf().isStr());
        boolean isBuf = type.isBuf() || (type.isArray() && type.arrayOf().isBuf());
        return !isPrimitive && !isBuf && !isStr;
    }

    /**
     * Writes teh methods to the UML definition.
     * @param typeDef the parent type
     * @param section the section text
     * @param methodSelector the method selector to apply
     */
    private void writeMethods(TypeDef typeDef, String section, TranslationUtil.IMethodSelector methodSelector) {
        MethodDef[] methodDefs = typeDef.methodDefs();
        Arrays.sort(methodDefs, new Comparator<MethodDef>() {
            @Override
            public int compare(MethodDef o1, MethodDef o2) {
                return Integer.compare(o1.flags, o2.flags);
            }
        });

        boolean hasMethods = false;
        for (int i = 0; i < methodDefs.length; i++) {
            MethodDef methodDef = methodDefs[i];
            if (acceptMethod(typeDef, methodSelector, methodDef)) {
                hasMethods = true;
                break;
            }
        }

        if (hasMethods) {
            if (section != null) {
                w("-- ").w(section).w(" --").nl();
            }
            for (int i = 0; i < methodDefs.length; i++) {
                MethodDef methodDef = methodDefs[i];
                if (!acceptMethod(typeDef, methodSelector, methodDef)) continue;

                indent();

                w("{method} ");
                if (methodDef.isAbstract()) {
                    w("{abstract} ");
                }
                if (methodDef.isStatic()) {
                    w("{static} ");
                }

                if (methodDef.isVirtual()) {
                    w("<<virtual>> ");
                }
                methodSig(methodDef).nl();
            }
        }
    }

    private boolean acceptMethod(TypeDef typeDef, TranslationUtil.IMethodSelector methodSelector, MethodDef methodDef) {
        // Omit inherited methods
        if (typeDef.base != null && methodDef.isInherited(typeDef.base)) {
            return false;
        }

        // Omit initializers
        if (methodDef.isInitOrDestroy()) {
            return false;
        }

        // Finally ask the selector
        if (!methodSelector.acceptMethod(methodDef)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the access modifier for plantUML, e. g. '+' for a public entity.
     * @param slotDef the slot definition (field or method)
     * @return the access modifier
     */
    private String getAccessModifier(SlotDef slotDef) {
        if (slotDef.isPublic()) {
            return "+";
        } else if (slotDef.isProtected()) {
            return "#";
        } else if (slotDef.isInternal()) {
            return "~";
        } else {
            return "-";
        }
    }

    public PlantUmlTranslator methodSig(MethodDef methodDef) {
        //wtype(methodDef.ret).w(" ").w(methodDef.name).w("(");
        w(getAccessModifier(methodDef)).w(methodDef.name).w("(");
        for (int i = 0; i < methodDef.params.length; ++i) {
            ParamDef p = methodDef.params[i];
            if (i > 0) w(", ");
            wtype(p.type).w(" ").w(p.name);
        }
        w(")");
        return this;
    }

    @Override
    public void header(String purpose) {
        w("' " + purpose).nl();
        w("' (C) Robert Bosch GmbH 2019").nl();
        w("' Tag      : $Id$").nl();
        w("' Namespace: " + kit.name).nl();
        w("' Generated: " + new Date()).nl();

    }

    @Override
    protected String getHeaderText() {
        return "UML diagram for kit '" + kit.name + "'";
    }

    public String qname(Type type) {
        return type.kit().name() + "::" + type.name();
    }

    public String qname(Slot s) {
        return qname(s.parent()) + "::" + s.name();
    }

    /**
     * Helper class for associations.
     */
    class Association {
        String sourceType;
        String targetType;
        String name;
        boolean isOneToMany;
        boolean isContained;
        boolean isDirected;

        /**
         * Creates a direct 1:1 association from source to target.
         * @param sourceType
         * @param targetType
         * @param isContained
         */
        public Association(String sourceType, String targetType, String name, boolean isContained) {
            this(sourceType, targetType, name, false, isContained, true);
        }

        public Association(String sourceType, String targetType, String name, boolean isOneToMany, boolean isContained, boolean isDirected) {
            this.sourceType = sourceType.replace("[]", ""); // HACK!
            this.targetType = targetType.replace("[]", "");
            this.name = name;
            this.isOneToMany = isOneToMany;
            this.isContained = isContained;
            this.isDirected = isDirected;
        }

        public String renderAssociation() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(sourceType);
            stringBuilder.append(" ");
            stringBuilder.append(isContained ? "*" : "-");
            stringBuilder.append("-");
            stringBuilder.append(isDirected ? ">" : "-");
            stringBuilder.append(" ");
            if (isOneToMany) {
                stringBuilder.append("\"many\" ");
            }

            stringBuilder.append(targetType);
            stringBuilder.append(" : ");
            stringBuilder.append(name);

            return stringBuilder.toString();
        }
    }
}
