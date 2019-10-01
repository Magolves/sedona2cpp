/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.ast.Expr;
import sedonac.ast.MethodDef;
import sedonac.ast.Stmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes unnecessary expression statements having InitVirt and InitComp
 */
public class RemoveInitVirtAndComp extends CompilerStep {
    private List<Stmt> newStmts = new ArrayList<>();
    private int count = 0;

    public RemoveInitVirtAndComp(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  RemoveInitVirtAndComp");
        walkAst(WALK_TO_EXPRS);
        log.debug("  RemoveInitVirtAndComp [" + count + " statements killed]");
    }

    @Override
    public void enterMethod(MethodDef m) {
        super.enterMethod(m);

        if (m.isInstanceInit()) {
            newStmts.clear();
            for (int i = 0; i < m.code.stmts().length; i++) {
                Stmt stmt = m.code.stmts()[i];
                if (stmt instanceof Stmt.ExprStmt) {
                    Stmt.ExprStmt exprStmt = (Stmt.ExprStmt) stmt;
                    // Remove InitVirt and InitComp
                    if (exprStmt.expr instanceof Expr.InitComp || exprStmt.expr instanceof Expr.InitVirt) {
                        count++;
                        continue;
                    }

                    // Remove also super._iInit()
                    if (exprStmt.expr instanceof Expr.Call && ((Expr.Call)exprStmt.expr).target instanceof Expr.Super) {
                        count++;
                        continue;
                    }
                    newStmts.add(stmt);
                }
            }

            if (m.code.stmts.size() > newStmts.size()) {
                m.code.stmts.clear();
                m.code.stmts.addAll(newStmts);
            }
        }
    }
}
