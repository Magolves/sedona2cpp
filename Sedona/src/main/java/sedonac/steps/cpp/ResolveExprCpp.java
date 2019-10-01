//
// Original Work:
//   Copyright (c) 2006, Brian Frank and Andy Frank
// 
// Derivative Work:
//   Copyright (c) 2007 Tridium, Inc.
//   Licensed under the Academic Free License version 3.0
//
// History:
//   13 Feb 07  Brian Frank  Creation
//

package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.Location;
import sedonac.ast.*;
import sedonac.namespace.*;
import sedonac.steps.ResolveExpr;

import static sedonac.namespace.TypeUtil.findSlotInHierarchy;


/**
 * ResolvedExprCpp replaces ResolveExpr in case of C++ translation. The original
 * step relied on Inherit which basically copied all slots from the base classes
 * into the current one.
 */
public class ResolveExprCpp
        extends ResolveExpr {

//////////////////////////////////////////////////////////////////////////
// Run
//////////////////////////////////////////////////////////////////////////

    public ResolveExprCpp(Compiler compiler) {
        super(compiler);
    }

    public void run() {
        log.debug("  ResolveExpr");
        walkAst(WALK_TO_EXPRS);
        quitIfErrors();
    }

//////////////////////////////////////////////////////////////////////////
// AstVisitor
//////////////////////////////////////////////////////////////////////////

    public void enterMethod(MethodDef m) {
        super.enterMethod(m);
        numLocalsInScope = 0;
        maxLocals = 0;
        labels = null;
        gotos = null;
    }


    private void exprStmt(Stmt.ExprStmt stmt) {
        stmt.expr.leave = false;
    }

    private void localDefStmt(Stmt.LocalDef stmt) {
        stmt.declared = true;
    }

    private void returnStmt(Stmt.Return stmt) {
        stmt.foreachDepth = foreachDepth;
    }

    private void forStmt(Stmt.For stmt) {
        if (stmt.update != null) stmt.update.leave = false;
    }


//////////////////////////////////////////////////////////////////////////
// Call Resolution
//////////////////////////////////////////////////////////////////////////

    public Expr resolveCall(Expr.Call call) {
        // get target type
        Type target = curType;
        if (call.target != null) {
            target = call.target.type;
        }

        // lookup slot
        String name = call.name;
        // we search for the in this type and all base types

        Type targetType = findSlotInHierarchy(target, call.method, name);
        Slot slot = null;
        if (targetType == null) {
            if (target != Namespace.error)
                err("Unknown method '" + target + "." + name + "'", call.loc);
            call.type = Namespace.error;
            return call;
        } else {
            //log.debug(String.format(String.format("Resolved slot '%s' to target type '%s (%s)'", name, targetType.name(), target.name())));
            slot = targetType.slot(name);
            call.type = targetType;

        }

        // check not field
        if (slot.isField()) {
            err("Cannot call field '" + target + "." + name + "' as method", call.loc);
            call.type = Namespace.error;
            return call;
        }

        // resolved (we do error checking in later step)
        Method method = (Method) slot;
        call.type = method.returnType();
        call.method = method;

        // if the argument is an interpolated string, then
        // check that this method returns an OutStream
        if (call.args.length == 1 && call.args[0].id == Expr.INTERPOLATION) {
            Type os = ns.resolveType("sys::OutStream");
            if (method.returnType().is(os))
                ((Expr.Interpolation) call.args[0]).callOk = true;
            else
                err("String interpolation requires that '" + method.qname() + "' return OutStream", call.loc);
        }

        // add implicit this if needed
        if (call.target == null && !method.isStatic())
            call.target = new Expr.This(call.loc);

        return call;
    }

    @Override
    public Expr resolveName(Expr.Name expr) {
        Location loc = expr.loc;
        String name = expr.name;
        Expr target = expr.target;

        // if target, this must be a field on the target type
        if (target != null) {
            Type base = TypeUtil.findSlotInHierarchy(target.type, null, name);
            if (base == null) {
                base = target.type;
            } else {
                log.debug(String.format(String.format("Resolved slot '%s' to base type '%s (%s)'", name, base.name(), target.type.name())));
            }

            Slot slot = base.slot(name);

            // check for special type literals
            if (target.id == Expr.STATIC_TYPE) {
                // TypeName.type is the syntax for type literal
                if (name.equals("type"))
                    return new Expr.Literal(target.loc, ns, Expr.TYPE_LITERAL, target.type);

                // TypeName.sizeof is the syntax for sizeof literal
                if (name.equals("sizeof"))
                    return new Expr.Literal(target.loc, ns, Expr.SIZE_OF, target.type);

                // check for TypeName.slot which is the syntax for slot literal
                if (slot != null && slot.isReflective())
                    return new Expr.Literal(target.loc, ns, Expr.SLOT_LITERAL, slot);
            }

            // maps to a field access
            if (slot instanceof Field) {
                Field f = (Field) slot;

                // Catch a slot ID literal... will treat it specially later
                if ((target.id == Expr.SLOT_LITERAL) && slot.qname().equals("sys::Slot.id"))
                    return new Expr.Literal(loc, Expr.SLOT_ID_LITERAL, f.type(), target);

                return new Expr.Field(loc, target, f, expr.safeNav);
            }

            if (target.type != Namespace.error)
                err("Unknown field: " + base.signature() + "." + name, expr.loc);
            expr.type = Namespace.error;
            return expr;
        }

        // check for a param or local binding
        VarDef var = resolveVar(name);
        if (var != null) {
            if (var.isParam())
                return new Expr.Param(loc, name, (ParamDef) var);
            else
                return new Expr.Local(loc, (Stmt.LocalDef) var);
        }

        // check for a field on my current type
        Type baseType = findSlotInHierarchy(curType, null, name);
        if (baseType == null) {
            err("Unknown var: " + name, expr.loc);
            expr.type = Namespace.error;
            return expr;
        } else {
            Slot slot = baseType.slot(name);
            //log.debug(String.format(String.format("Resolved var '%s' to base type '%s (%s)'", name, baseType.name(), curType.name())));
            if (slot instanceof Field) {
                // add implicit this if needed
                target = slot.isStatic() ? null : new Expr.This(loc);
                return new Expr.Field(loc, target, (Field) slot, expr.safeNav);
            }
        }

        return expr;
    }

}
