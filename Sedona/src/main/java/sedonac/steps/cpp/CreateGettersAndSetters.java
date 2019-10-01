package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.*;
import sedonac.namespace.*;
import sedonac.parser.Token;
import sedonac.translate.TranslationUtil;

import java.util.HashMap;

import static sedonac.translate.TranslationUtil.*;

@SuppressWarnings({"unchecked", "ThrowableNotThrown"})
public class CreateGettersAndSetters extends CompilerStep {
    private HashMap<String, Method> methodMap = new HashMap<>();

    public CreateGettersAndSetters(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  CreateGettersAndSetters");
        walkAst(WALK_TO_EXPRS);
        quitIfErrors();
        log.debug("  CreateGettersAndSetters [" + methodMap.size() + "]");

        log.info("  -- Getter/setter list");
        for(String key : methodMap.keySet()) {
            log.info(String.format("    [%s] %s", key, methodMap.get(key)));
        }
    }

    @Override
    public void enterField(FieldDef f) {
        Location loc = f.loc;
        if (TranslationUtil.isPublicField(f)) {
            addGetter(loc, f);
            addSetter(loc, f);
            // flag is modified in step PublicToProtected
        }
    }

    @Override
    public void enterStmt(Stmt s) {
        if (s instanceof Stmt.Return) {
            Stmt.Return stmt = (Stmt.Return) s;
            stmt.expr = translateToGetter(stmt.expr);
        }

        if (s instanceof Stmt.LocalDef) {
            Stmt.LocalDef localDef = (Stmt.LocalDef) s;
            localDef.init = translateToGetter(localDef.init);
        }

        // Unfortunately If, while, for,... do not have a common base class :-(
        if (s instanceof Stmt.If) {
            Stmt.If anIf = (Stmt.If) s;
            anIf.cond = translateToGetter(anIf.cond);
        }

        if (s instanceof Stmt.While) {
            Stmt.While aWhile = (Stmt.While) s;
            aWhile.cond = translateToGetter(aWhile.cond);
        }

        if (s instanceof Stmt.DoWhile) {
            Stmt.DoWhile doWhile = (Stmt.DoWhile) s;
            doWhile.cond = translateToGetter(doWhile.cond);
        }

        if (s instanceof Stmt.For) {
            Stmt.For aFor = (Stmt.For) s;
            aFor.cond = translateToGetter(aFor.cond);
        }

        if (s instanceof Stmt.Assert) {
            Stmt.Assert anAssert = (Stmt.Assert) s;
            anAssert.cond = translateToGetter(anAssert.cond);
        }
    }

    /**
     * Adds a setter for the given field
     * @param loc the location
     * @param f the field to add the setter for
     */
    private void addSetter(Location loc, FieldDef f) {
        final ParamDef paramDef = new ParamDef(loc, 0,  f.type, "newValue");
        final Expr.Param param = new Expr.Param(loc, "newValue", paramDef);
        final String setter = toSetter(f.name);

        if (f.parent.slot(setter) != null) {
            //log.debug(String.format("Slot already exists: %s [%s]", setter, f.parent));
            return;
        }

        Block setterBlock = new Block(loc);
        Stmt.ExprStmt assign = new Stmt.ExprStmt(loc, new Expr.Binary(loc, new Token(loc, f.isProperty() ? Token.PROP_ASSIGN : Token.ASSIGN),
                new Expr.Field(loc, new Expr.This(loc), f),
                param));

        // simple return without expr
        Stmt.Return setterReturn = new Stmt.Return(loc);

        setterBlock.stmts.add(assign);
        setterBlock.stmts.add(setterReturn);
        // NOTE: Setter returns new value to support cascade
        setterReturn.expr = new Expr.Field(loc, new Expr.This(loc), f);

        MethodDef setterMethod = new MethodDef(loc, f.parent, Slot.PUBLIC|Slot.INLINE, setter, NO_FACETS, f.type, new ParamDef[]{paramDef}, setterBlock);
        setterMethod.doc = String.format("Setter method for member '%s' (generated). " + f.doc, f.name);

        addMethod(setterMethod);

        f.parent.addSlot(setterMethod);

        if (f.parent.slot(setter) == null) {
            log.warn("Slot was NOT added " + setter + " [" + f.parent + "]");
        }
    }

    /**
     * Adds a getter for the given field
     * @param loc the location
     * @param f the field to add the getter for
     */
    private void addGetter(Location loc, FieldDef f) {
        final String getter = toGetter(f.name);
        if (f.parent.slot(getter) != null) {
            return;
        }

        Block getterBlock = new Block(loc);
        Stmt.Return getterReturn = new Stmt.Return(loc);
        getterReturn.expr = new Expr.Field(loc, new Expr.This(loc), f);
        getterBlock.stmts.add(getterReturn);
        // Make getter also const and inline
        MethodDef getterMethod = new MethodDef(loc, f.parent, Slot.PUBLIC|Slot.INLINE|Slot.CONST, getter, NO_FACETS, f.type, NO_PARAMETERS, getterBlock);
        getterMethod.doc = String.format("Getter method for member '%s' (generated).\r\n" + f.doc, f.name);

        addMethod(getterMethod);

        f.parent.addSlot(getterMethod);
        if (f.parent.slot(getter) == null) {
            log.warn("Slot was not added " + getter + " [" + f.parent + "]");
        }
    }

    /**
     * Adds a method to the internal cache.
     * @param method the method to add
     */
    private void addMethod(Method method) {
        String key = makeKey(method);
        if (methodMap.containsKey(key)) {
            return;
        }
        methodMap.put(key, method);
    }

    /**
     * Creates a hash key for the given method
     * @param method the method
     * @return the hash key
     */
    private String makeKey(Method method) {
        return method.parent().name() + "_" + method.name();
    }

    /**
     * Creates a hash key for the given type and method name
     * @param type the type owning the method
     * @param methodName the method name
     * @return the hash key
     */
    private String makeKey(Type type, String methodName) {
        if (type == null) {
            type = curType;
        }
        return type.name() + "_" + methodName;
    }

    //region Expression (transform assignments into calls)
    @Override
    public Expr expr(Expr expr) {
        if (expr instanceof Expr.Binary) {
            // E.g. x = y; where x is public class member -> setX(y)
            Expr.Binary binary = (Expr.Binary) expr;

            if (expr.id == Expr.ASSIGN || expr.id == Expr.PROP_ASSIGN) {
                if (binary.lhs instanceof Expr.Field) {
                    Expr newRhs = binary.rhs;
                    newRhs = translateToGetter(binary.rhs);

                    // this results either in a call expr or in a modified rhs
                    return translateToSetter(binary, (Expr.Field) binary.lhs, newRhs);
                } else {
                    binary.rhs = translateToGetter(binary.rhs);
                }
            } else {
                binary.lhs = translateToGetter(binary.lhs);
                binary.rhs = translateToGetter(binary.rhs);
            }
        } else if (expr instanceof Expr.Call) {
            Expr.Call call = (Expr.Call) expr;
            for (int i = 0; i < call.args.length; i++) {
                call.args[i] = translateToGetter(call.args[i]);
            }

            return call;
        } else if (expr instanceof Expr.Interpolation) {
            Expr.Interpolation interpolation = (Expr.Interpolation) expr;
            for(int i = 0; i < interpolation.parts.size(); i++) {
                Expr part = (Expr) interpolation.parts.get(i);
                part = translateToGetter(part);
                interpolation.parts.set(i, part);
            }
            return interpolation;
        } else if (expr instanceof Expr.Cast) {
            Expr.Cast cast = (Expr.Cast) expr;
            cast.target = translateToGetter(cast.target);
            return cast;
        }  else if (expr instanceof Expr.Cond) {
            Expr.Cond cond = (Expr.Cond) expr;
            for(int i = 0; i < cond.operands.size(); i++) {
                Expr part = (Expr) cond.operands.get(i);
                part = translateToGetter(part);
                cond.operands.set(i, part);
            }
            return cond;
        } else if (expr instanceof Expr.Unary) {
            Expr.Unary unary = (Expr.Unary) expr;

            Expr newExpr = translateToGetter(unary.operand);
            if (newExpr != unary.operand) {
                unary.operand = newExpr;
            }

            return unary;
        }

        return super.expr(expr);
    }

    /**
     * Helper method to save some casts
     * @param expr
     * @return
     */
    private Expr translateToGetter(Expr expr) {
        if (expr instanceof Expr.Field) {
            return translateToGetter((Expr.Field) expr);
        } else {
            return expr;
        }
    }

    /**
     * Translates the (public instance) field access to a getter call. If the field is local (e. g. this->x), the expression
     * remains unchanged.
     * @param field the field expression to translate
     * @return the (modified or original) expression
     */
    private Expr translateToGetter(Expr.Field field) {
        // if we access a field of this instance, we do not generate a getter call
        final Field fieldDef = field.field;
        if (TranslationUtil.isPublicField(fieldDef) && isExternalField(field)) {
            final String getter = TranslationUtil.toGetter(field.name);
            final Type fieldType = field.field.parent();

            Method methodDef = resolveMethod(fieldType, getter);

            if (methodDef != null) {
                Expr targetExpr = field.target;
                // Check if target itself is an expr, e. g. a.services.nextService -> a.getServices().getNextService()
                if (targetExpr instanceof Expr.Field) {
                    targetExpr = translateToGetter((Expr.Field) targetExpr);
                }
                return new Expr.Call(field.loc, targetExpr, methodDef, new Expr[]{});
            } else {
                // Getter does not exist -> try to create one
                if (fieldDef instanceof FieldDef) {
                    addGetter(field.loc, (FieldDef) fieldDef);
                    if (resolveMethod(fieldType, getter) != null) {
                        // ok -> try again
                        return translateToGetter(field);
                    } else {
                        err(String.format("Getter '%s' has not been added [%s, %s]", getter, fieldType, fieldType.slot(getter)));
                        return field;
                    }
                } else {
                    log.warn("Cannot add getter to IRType " + fieldDef.parent());
                    return field;
                }
            }
        } else {
            return field;
        }
    }

    private boolean isExternalField(Expr.Field field) {
        return !TypeUtil.equals(curType, field.target.type) && !(field.target instanceof Expr.This);
    }

    /**
     * Translates the (public instance) field assignment to a setter call, e. g. <code>this->x = 15</code> becomes
     * <code>setX(15)</code>.
     * @param field the field expression to translate
     * @return the (modified or original) expression
     */
    private Expr translateToSetter(Expr.Binary binary, Expr.Field field, Expr rhs) {
        final Field fieldDef = field.field;

        if (TranslationUtil.isPublicField(fieldDef)) {
            final String setter = TranslationUtil.toSetter(field.name);
            // Do not modify our own setter! (would result in stack overflow)
            if (setter.equals(curMethod.name)) {
                return binary;
            }

            final Type fieldType = field.field.parent();
            Method methodDef = resolveMethod(fieldType, setter);

            if (methodDef != null) {
                return new Expr.Call(field.loc, field.target, methodDef, new Expr[]{rhs});
            } else {
                if (fieldDef instanceof FieldDef) {
                    addSetter(field.loc, (FieldDef) fieldDef);
                    if (resolveMethod(fieldType, setter) != null) {
                        return translateToSetter(binary, field, rhs);
                    } else {
                        err(String.format("Setter '%s' has not been added [%s, %s]", setter, fieldDef.parent(), fieldType.slot(setter)));
                        return binary;
                    }
                } else {
                    log.warn("Cannot add setter to IRType " + fieldDef.parent());
                    return binary;
                }
            }
        } else {
            // no setter required, just change the rhs
            binary.rhs = rhs;
            return binary;
        }
    }

    /**
     * Finds the given method in the following order
     * <ol>
     *     <li>internal cache</li>
     *     <li>type definition of given type</li>
     *     <li>type definition of super type (if any)</li>
     * </ol>
     * @param type the type owning the method. If <code>null</code>, the current type
     *             is used.
     * @param methodName the name of the method to search
     * @return the method instance or <code>null</code>, if method could not been found
     */
    private Method resolveMethod(Type type, String methodName) {
        if (type == null) {
            type = curType;
        }

        String key = makeKey(type, methodName);
        if (methodMap.containsKey(key)) {
            return methodMap.get(key);
        } else {
            for (int i = 0; i < type.slots().length; i++) {
                Slot slot = type.slots()[i];
                if (methodName.equals(slot.name()) && slot.isMethod()) {
                    addMethod((Method) slot);
                    return (Method) slot;
                }
            }

            // Check super class (skip C++ types for performance reasons)
            if (type.base() != null && !TranslationUtil.isCppType(type.base().qname())) {
                return resolveMethod(type.base(), methodName);
            }
            return null;
        }
    }
    //endregion
}
