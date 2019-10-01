/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.*;
import sedonac.namespace.ArrayType;
import sedonac.namespace.PrimitiveType;
import sedonac.namespace.Type;

/**
 * Converts type references into a plain string.
 */
public class StringifyType extends CompilerStep {

    private Type simplifiedTypeType;

    public StringifyType(Compiler compiler) {
        super(compiler);

        simplifiedTypeType = ns.strType;
    }

    @Override
    public void run() {
        log.debug("  StringifyType");
        walkAst(WALK_TO_EXPRS);
        quitIfErrors();
    }

    @Override
    public void enterField(FieldDef f) {
        super.enterField(f);

        if (f.type.isType()) {
            f.type = toSimplifiedTypeType(f.type);
            if (f.init != null) {
                f.init = toSimplifiedTypeLiteral(f.init);
            }
        }

    }

    @Override
    public void enterMethod(MethodDef m) {
        super.enterMethod(m);

        m.ret = toSimplifiedTypeType(m.ret);
        for (int i = 0; i < m.params.length; i++) {
            ParamDef param = m.params[i];
            param.type = toSimplifiedTypeType(param.type);
        }
    }

    @Override
    public Expr expr(Expr expr) {
        if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;

            binary.rhs.type = toSimplifiedTypeType(binary.rhs.type);
        }

        if (expr instanceof Expr.Unary) {
            Expr.Unary unary = (Expr.Unary) expr;

            if (unary.operand.type.isType()) {
                unary.operand = toSimplifiedTypeLiteral(unary.operand);
            }
        }

        if (expr.id == Expr.TYPE_LITERAL) {
            @SuppressWarnings("ConstantConditions") Expr.Literal literal = (Expr.Literal) expr;
            return toSimplifiedTypeLiteral(literal);
        }

        expr.type = toSimplifiedTypeType(expr.type);

        return expr;
    }

    @Override
    public void enterStmt(Stmt s) {
        super.enterStmt(s);

        if (s instanceof Stmt.Return) {
            Stmt.Return aReturn = (Stmt.Return) s;
            if (aReturn.expr != null && aReturn.expr.type.isType()) {
                aReturn.expr.type = toSimplifiedTypeType(aReturn.expr.type);
            }
        }

        if (s instanceof Stmt.LocalDef) {
            Stmt.LocalDef localDef = (Stmt.LocalDef) s;
            localDef.type = toSimplifiedTypeType(localDef.type);

        }
    }

    /**
     * Returns (our) simplified type def for the given type. If the given type is an array,
     * the element type will be changed.
     * If the given type refers to a primitive type, the type is left untouched.
     * @param originalType the type def to convert
     * @return our simplified type or the original type, if given type does not denote a type def or is a
     * primitive type def.
     */
    private Type toSimplifiedTypeType(Type originalType) {
        if (originalType == null) {
            return null;
        }

        if (originalType.isArray()) {
            ArrayType arrayType = (ArrayType) originalType;
            if (arrayType.of.isType()) {
                arrayType.of = simplifiedTypeType;
                log.debug(String.format("Changed array type to %s", originalType));
            }
            return originalType;
        } else if (originalType.isType()) {
            log.debug(String.format("Changed plain type to %s", originalType));
            return simplifiedTypeType;
        } else {
            return originalType;
        }
    }

    /**
     * Converts a type literal into a literal which matches our simplified type.
     * @param expr the expression to convert
     * @return the modified expression
     */
    private Expr toSimplifiedTypeLiteral(Expr expr) {
        if (expr instanceof Expr.Literal) {
            Expr.Literal literal = (Expr.Literal) expr;

            if (literal.id == Expr.TYPE_LITERAL && !literal.type.isPrimitive()) {
                if (literal.value instanceof PrimitiveType) {
                    return expr;
                }

                if (literal.value instanceof Type) {
                    Type type = (Type) literal.value;
                    if (type.isBuf()) {
                        return expr;
                    }
                }

                final String value = literal.value.toString();
                final Expr.Literal newLiteral = new Expr.Literal(new Location(expr.loc + "|" + expr.getClass().getName()), Expr.STR_LITERAL, simplifiedTypeType, value);
                log.debug(String.format("Changed literal: %s -> %s (%s)", literal, newLiteral, literal.loc));
                return newLiteral;
            }
        }
        return expr;
    }
}
