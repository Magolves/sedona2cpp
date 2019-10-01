/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.namespace.Slot;
import sedonac.namespace.TypeUtil;

import static sedonac.translate.CppDefaults.getHeaderExtension;
import static sedonac.translate.TranslationUtil.isCppType;
import static sedonac.translate.TranslationUtil.makeSysInclude;

public class CppHTranslator extends CppTranslator {

    public CppHTranslator(Compiler compiler, TypeDef type) {
        super(compiler, type);
    }


    @Override
    protected void translateCPlusPlus() {
        beginGuard();

        lineComment("STL includes");
        for (String STL_INCLUDE : STL_INCLUDES) {
            w(String.format("#include <%s>", STL_INCLUDE)).nl();
        }
        w("").nl();

        // Helper includes for components
        if (TypeUtil.isaComponent(type)) {
            // Internal includes
            for (int i = 0; i < SOURCE_INCLUDES.length; i++) {
                String helperInclude = makeSysInclude(type, SOURCE_INCLUDES[i]);
                w(String.format("#include \"%s\"", helperInclude)).nl();
            }
        }

        includes(false, t -> !isCppType(t.qname()));

        /* NOTE: Includes must go before the namespace decl!! */
        w("").nl();
        w("").nl();
        beginNamespace();

        enums();

        lineComment("Forward declaration");
        w("class " + type.name + ";").nl();

        w("").nl();
        if (type.doc != null) {
            writeDocBlock(type.doc);
        }

        String baseTypeName = null;
        if (TranslationUtil.hasCppBaseType(qname(type))) {
            baseTypeName = TranslationUtil.toCppBaseType(qname(type), null);
        } else if (type.base != null) {
            if (!TranslationUtil.isCppType(type.base.qname())) {
                baseTypeName = qname(type.base);
            }
        }

        if (baseTypeName != null) {
            w("class " + type.name + " : public " + baseTypeName + " {").nl();
        } else {
            w("class " + type.name + " {").nl();
        }

        indent++;
        beginRegion("Public members");
        indent().w("public:").nl();
        indent++;

        // Constructor
        final TranslateContext methodContext = makeMethodContext(true);
        writeHeaderMethods(MethodDef::isInstanceInit, methodContext);
        // Destructor
        writeHeaderMethods(MethodDef::isInstanceDestroy, methodContext);

        beginRegion("Properties");
        translateField(SlotDef::isProperty, this::writePropertiesDecl, true);
        translateMethod(SlotDef::isAction, this::writeActionsDecl, true);
        endRegion();

        beginRegion("Public members/methods");
        writeHeaderMethods(m -> m.isPublic() && !m.isInitOrDestroy(), methodContext);
        translateField(SlotDef::isPublic, this::writeFieldDeclaration, true);

        if (type.isaComponent()) {
            writeSlotArrayDecl(type);
        }

        endRegion();
        indent--;
        endRegion(); // public

        indent().w("protected:").nl();
        beginRegion("Protected members/methods");
        indent++;
        writeHeaderMethods(SlotDef::isProtected, methodContext);
        writeHeaderMethods(SlotDef::isInternal, methodContext);

        beginRegion("Fields");
        translateField(f -> matchFlags(f, Slot.PROTECTED|Slot.INTERNAL), this::writeFieldDeclaration, true);
        endRegion();
        indent--;
        endRegion(); // protected

        // Private members
        beginRegion("Private members/methods");

        indent().w("private:").nl();
        indent++;
        writeHeaderMethods(SlotDef::isPrivate, methodContext);

        beginRegion("Fields");
        translateField(SlotDef::isPrivate, this::writeFieldDeclaration, true);
        endRegion();
        indent--;
        endRegion();

        indent().w("}; // class " + type.name).nl().nl();
        endNamespace();

        endGuard();
    }

    private boolean isSynthetic(FieldDef f) {
        return f.synthetic;
    }

    protected boolean matchFlags(FieldDef f, int modifiers) {
        return !isSynthetic(f) && (f.flags & modifiers) > 0;
    }

    protected void writeHeaderMethods(TranslationUtil.IMethodSelector selector, TranslateContext translateContext) {
        MethodDef[] methodDefs = type.methodDefs();

        for (MethodDef methodDef : methodDefs) {
            translateContext.initFromMethod(methodDef);

            if (selector.acceptMethod(methodDef)) {
                writeDocBlock(methodDef, String.format("Method '%s' (%s)", methodDef.name, translateContext.getModifiers()));

                indent().w(methodSignature(methodDef, translateContext));
                if (methodDef.isAbstract()) {
                    w(" = 0");
                }
                w(";").nl().nl();
            }
        }
    }

    /**
     * Generates the enum definitions
     */
    private void enums() {
        FieldDef[] fieldDefs = type.fieldDefs();
        for (FieldDef fieldDef : fieldDefs) {
            if (!fieldDef.type.isEnum()) continue;

            EnumDef enumDef = (EnumDef) fieldDef.type;

            writeDocBlock(new String[]{String.format("Enum '%s'", fieldDef.name), enumDef.loc.toFileName()});
            indent().w("typedef enum ").w(fieldDef.type.name()).w("_t {").nl();
            String[] values = enumDef.tags();
            indent++;
            for (String value : values) {
                indent().w(value).w(",").nl();
            }
            indent--;
            indent().w("} ").w(fieldDef.type.name()).w(";").nl().nl();
        }
    }

    /**
     * Gets the file extension.
     * @return see {@link CppDefaults#DEFAULT_SOURCE_EXTENSION}
     */
    public String getFileExt() {
        return getHeaderExtension();
    }

    @Override
    public void header() {
        header("Header file for class '" + type.name + "'.");
    }



}
