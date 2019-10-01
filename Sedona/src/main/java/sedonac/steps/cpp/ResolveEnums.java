package sedonac.steps.cpp;

import sedona.Facets;
import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.ast.*;
import sedonac.namespace.Field;
import sedonac.translate.CppDefaults;
import sedonac.translate.TranslationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static sedonac.ast.Expr.*;
import static sedonac.translate.TranslationUtil.canBeEnum;

public class ResolveEnums extends CompilerStep {
    private TypeDef astType;

    public ResolveEnums(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        findEnums();
        linkEnums();
        walkAst(CompilerStep.WALK_TO_EXPRS);
    }

    @Override
    public void enterType(TypeDef t) {
        astType = t;
        super.enterType(t);
    }

    @Override
    public Expr expr(Expr expr) {
        if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            if (binary.lhs instanceof Expr.Field) {
                Expr.Field fieldLhs = (Expr.Field) binary.lhs;

                if(fieldLhs.type.isArray()) {
                    return super.expr(expr);
                }

                final String name = fieldLhs.field.type().name();
                EnumDef enumDef = enumDefHashMap.getOrDefault(name, null);
                if (enumDef != null) {
                    if (binary.rhs instanceof Expr.Field) {
                        Expr.Field fieldRhs = (Expr.Field) binary.rhs;
                        if (fieldRhs.field instanceof FieldDef) {
                            FieldDef fieldDef = (FieldDef) fieldRhs.field;
                            binary.rhs = translateEnumInit(fieldRhs.field, enumDef, fieldDef.init);
                            return binary;
                        }
                    }
                }

            }
        }
        return super.expr(expr);
    }

    /**
     * Scans all defines for possible enum types, e. g. <code>define Str srcLevelStr= "in0, emergency, in2, in3, in4, ...
     * , in15, in16, fallback"</code>, creates a field definition and marks it as <it>synthetic</it>.
     */
    private void findEnums() {
        KitDef kit = compiler.ast;

        List<TypeDef> newTypes = new ArrayList<>();

        for (int j = 0; j < kit.types.length; j++) {
            TypeDef type = kit.types[j];

            for (int k = 0; k < type.fieldDefs().length; k++) {
                FieldDef fieldDef = type.fieldDefs()[k];

                if (canBeEnum(fieldDef)) {
                    String init = fieldDef.init.toString();
                    String[] values = extractEnumValues(init);

                    final EnumDef enumDef = new EnumDef(fieldDef.loc, kit, fieldDef.flags, fieldDef.name, new FacetDef[0], values);

                    newTypes.add(enumDef);
                    enumDefHashMap.put(fieldDef.qname(), enumDef);
                    enumDefHashMap.put(fieldDef.name, enumDef);

                    // mark field as 'synthetic' since it is covered by the enum
                    fieldDef.synthetic = true;
                }
            }

            newTypes.add(type);
        }

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        kit.types = newTypes.toArray(new TypeDef[newTypes.size()]);
    }

    /**
     * Extracts the enum values as string array from the Sedona enum string.
     *
     * @param enumExpression the enum expression (from the AST)
     * @return the string array representing the enum values
     */
    private String[] extractEnumValues(String enumExpression) {
        String[] values = enumExpression.replace("\"", "").split(",");

        for (int l = 0; l < values.length; l++) {
            values[l] = values[l].trim();
        }
        return values;
    }

    /**
     * Iterates over all types and fields and check for possible enum members.
     * If an enum type is found, the field will be modified accordingly, e. g.
     * the field type will become an int and the initial value will be mapped to
     * the proper enum tag.
     */
    private void linkEnums() {
        KitDef kit = compiler.ast;

        for (int j = 0; j < kit.types.length; j++) {
            TypeDef type = kit.types[j];
            for (int k = 0; k < type.fieldDefs().length; k++) {
                FieldDef fieldDef = type.fieldDefs()[k];

                if (!fieldDef.isProperty()) continue;

                String range = TranslationUtil.getEnumValue(fieldDef);

                if (range != null) {
                    EnumDef enumDef = enumDefHashMap.getOrDefault(range, null);
                    if (enumDef != null) {
                        fieldDef.type = enumDef;
                        // Redirect init literal to proper enum value
                        fieldDef.init = translateEnumInit(fieldDef, enumDef,fieldDef.init);
                    }
                }
            }
        }
    }

    /**
     * Translates the init/assign expression for the given field. Sedona usually assigns a constant/define which is
     * translated into the appropriate enum ref.
     * @param field the (enum) field
     * @param enumDef the enum type def
     * @param expr the expression which inits/assigns the field
     * @return the modified expression or 'expr', if expr is either null or conatins an invalid value
     */
    private Expr translateEnumInit(Field field, EnumDef enumDef, Expr expr) {
        Expr newExpr = null;
        boolean asLiteral = false;

        if (expr != null) {
            int ordinal = 0; // ordinal value of enum
            if (expr instanceof Expr.Field) {
                Expr.Field fieldExpr = (Expr.Field) expr;
                Integer intLiteral = fieldExpr.toIntLiteral();
                if (intLiteral != null) {
                    ordinal = intLiteral.byteValue();
                }
            } else if (expr instanceof Expr.Literal) {
                asLiteral = true;
                ordinal = ((Expr.Literal)expr).asInt();
            } else {
                log.error("Cannot determine enum value from " + expr);
            }

            // check enum range
            if (ordinal >= 0 && ordinal <= enumDef.maxValue()) {
                // Expr.Literal does not support 'byte'
                /*
                if (asLiteral) {
                    newExpr = new Expr.Literal(expr.loc, ENUM_LITERAL, null, new EnumObject(enumDef.name, ordinal, enumDef.tags()));
                } else {
                    newExpr = new Expr.Literal(expr.loc, INT_LITERAL, null, String.format("%s%s%s", enumDef.name, "::", enumDef.tag(ordinal)));
                }*/
                newExpr = new Expr.Literal(expr.loc, ENUM_LITERAL, null, new EnumObject(enumDef.name, ordinal, enumDef.tags()));
                newExpr.type = enumDef;
            } else {
                log.error(String.format("Invalid enum value %d for %s [0..%d]", ordinal, enumDef, enumDef.maxValue()));
            }
        }

        return newExpr != null ? newExpr : expr;
    }


    private HashMap<String, EnumDef> enumDefHashMap = new HashMap<>();
}
