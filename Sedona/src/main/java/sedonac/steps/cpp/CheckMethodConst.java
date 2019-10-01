/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.ast.*;
import sedonac.namespace.Slot;
import sedonac.namespace.TypeUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Stack;

/**
 * Check for const'ness of methods (const according to C++ means this point can be be treated as const pointer)
 * and assigns the CONST flag to the method.
 */
public class CheckMethodConst extends CompilerStep {
    private HashMap<String, ConstResult> constCache = new HashMap<>();
    private Stack<MethodDef> methodStack = new Stack<>();
    private int count = 0;

    public CheckMethodConst(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  CheckMethodConst");
        walkAst(WALK_TO_TYPES);
        quitIfErrors();
        log.debug("  CheckMethodConst [Made " + count + " method(s) const]");
    }

    @Override
    public void enterType(TypeDef t) {
        super.enterType(t);
        // Skip tests
        if (TypeUtil.isaTest(t)) {
            return;
        }

        for(SlotDef slot : t.slotDefs()) {
            if (slot.isMethod() && isConst((MethodDef) slot, null)) {
                slot.flags |= Slot.CONST; // Set const flag
                ++count;
            }
        }
    }

    /**
     * Checks const'ness of given method by walking over the expressions of it's code.
     * Method calls to IR types treated as non-const, since the expressions cannot be evaluated.
     * @param methodDef the method to check
     * @param parentVisitor the calling visitor (is null for root visitor)
     * @return true, if method is const (parent type is not changed)
     */
    private boolean isConst(MethodDef methodDef, ConstVisitor parentVisitor) {
        // check cache if method has been examined before
        if (inCache(methodDef)) {
            return isConstFromCache(methodDef);
        }
        // Skip ctors/dtor
        if (methodDef.isInitOrDestroy()) {
            storeConstResult(methodDef, EnumSet.of(NonConstViolation.initOrDestroy));
            return false;
        }

        // Native methods (cannot affect 'this' pointer)
        if (methodDef.isNative()) {
            storeConstResult(methodDef, EnumSet.noneOf(NonConstViolation.class));
            return true;
        }

        // Obtain const'ness from overridden method
        if (methodDef.isOverride() && methodDef.overrides instanceof MethodDef) {

            return isConst((MethodDef) methodDef.overrides, parentVisitor);
        }

        // Check stack for dup call
        if (methodStack.contains(methodDef)) {
            // A method is called at least twice -> we return true here and let the other call decide
            return true;
        }

        // Push method on stack to prevent duplicate calls
        methodStack.push(methodDef);
        // Visit the method code
        final ConstVisitor constVisitor = new ConstVisitor();
        methodDef.walk(constVisitor, WALK_TO_EXPRS);

        // Merge result from parent visitor
        if (parentVisitor != null) {
            parentVisitor.reason.addAll(constVisitor.reason);
        }
        // Report and store result
        log.debug("    Method " + methodDef.qname +  " is " + (constVisitor.reason.isEmpty() ? "const" : "NOT const [" + constVisitor.reason + "]"));

        storeConstResult(methodDef, constVisitor.reason);

        methodStack.pop();
        return constVisitor.isConstCall();
    }

    private void storeConstResult(MethodDef methodDef, EnumSet<NonConstViolation> reason) {
        String key = key(methodDef);
        if (constCache.containsKey(key)) {
            err("Duplicate entry " + key);
        } else {
            constCache.put(key, new ConstResult(reason));
        }
    }

    private boolean inCache(MethodDef methodDef) {
        return constCache.containsKey(key(methodDef));
    }

    private String key(MethodDef methodDef) {
        return methodDef.qname();
    }

    private boolean isConstFromCache(MethodDef methodDef) {
        if (inCache(methodDef)) {
            return constCache.get(key(methodDef)).isConst();
        }

        return false;
    }

    /**
     * Internal visitor for checking const'ness of expressions
     */
    class ConstVisitor extends AstVisitor {
        EnumSet<NonConstViolation> reason = EnumSet.noneOf(NonConstViolation.class);

        @Override
        public Expr expr(Expr expr) {
            if (expr instanceof Expr.Call) {
                Expr.Call call = (Expr.Call) expr;
                if (call.method instanceof MethodDef) {
                    // Check AST of called method
                    if (!isConst((MethodDef) call.method, this)) {
                        log.debug(String.format("    > Const violated by call %s [%s]", call.method, call.target));
                        reason.add(NonConstViolation.nonConstCall);
                    }
                } else {
                    log.debug(String.format("    > External call violates const %s [%s]", call.method, call.target));
                    reason.add(NonConstViolation.externalCall);
                }
            } else {
                final boolean exprIsConst = expr.isConst();
                if (reason.isEmpty() && !exprIsConst) {
                    log.debug(String.format("    > Const violated by expression %s [%s]", expr, expr.getClass().getName()));
                    reason.add(NonConstViolation.nonConstExpr);
                }
            }

            return super.expr(expr);
        }

        final boolean isConstCall() {
            return reason.isEmpty();
        }
    }

    enum NonConstViolation {
        initOrDestroy,
        nonConstCall,
        externalCall,
        nonConstExpr
    }

    static class ConstResult {
        EnumSet<NonConstViolation> reason;

        ConstResult(EnumSet<NonConstViolation> reason) {
            this.reason = reason;
        }

        public boolean isConst() {
            return reason.isEmpty();
        }

        @Override
        public String toString() {
            return "ConstResult{" +
                    "isConst = " + isConst() + " " + reason +
                    '}';
        }
    }
}
