/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.steps;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.ast.Expr;
import sedonac.ast.Stmt;
import sedonac.parser.Token;

/**
 * Replace 'a.equals(b)' (where a and b are of type Str) with 'a == b' expression.
 */
public class ReplaceStrEquals extends CompilerStep {
    private int replaceCounter = 0;

    public ReplaceStrEquals(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  ReplaceStrEquals");
        walkAst(WALK_TO_EXPRS);
        quitIfErrors();
        log.debug("  ReplaceStrEquals [" + replaceCounter + "]");
    }

    @Override
    public Expr expr(Expr expr) {
        if (expr.id == Expr.CALL) {
            Expr.Call call = (Expr.Call) expr;
            if ("equals".equals(call.name)) {
                //log.debug("Found equals call on Str: " + expr.loc);
                Expr.Binary newExpr = new Expr.Binary(call.loc, new Token(call.loc, Token.EQ), call.target, call.args[0]);
                // Set binary type to bool. This is required, since expr.type is determined by lhs type
                // (which is Str in our case) and would lead to an error "If cond. must be bool!".
                newExpr.type = ns.boolType;

                replaceCounter++;

                return newExpr;
            }
        }

        return super.expr(expr);
    }
}
