//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   15 Mar 07  Brian Frank  Creation
//           

// this code is in a vegetative state

package sedonac.translate;

import java.io.*;
import java.util.*;

import sedona.Env;
import sedona.util.*;
import sedonac.*;
import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.namespace.*;

import static sedonac.translate.CppDefaults.SYS_KIT;

/**
 * AbstractTranslator
 */
@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public abstract class AbstractTranslator
        extends CompilerSupport {


//////////////////////////////////////////////////////////////////////////
// Constructor
//////////////////////////////////////////////////////////////////////////

    public AbstractTranslator(Compiler compiler, TypeDef type) {
        super(compiler);
        this.type = type;
        this.outDir = compiler.translation.outDir;

        boolean lIsSys = false;
        try {
            lIsSys = compiler.ast.name.equals(SYS_KIT);
        } catch (Exception e) {
            log.warn("Cannot determine sys: " + e.getMessage());
        }
        isSys = lIsSys;
    }

    //region Translation methods
    public void translate()
            throws IOException {

        if (!acceptType(type)) {
            log.info(String.format("Type %s not accepted by translator '%s'", type, this.getClass().getName()));
            return;
        }

        File file = toFile();
        log.debug("    Translate [" + file + "]");
        out = new PrintWriter(new FileWriter(file));
        try {
            header();
            doTranslate();
            log.debug("    Wrote [" + file.getAbsolutePath() + "]");
        } finally {
            out.close();
        }
    }

    public abstract File toFile();

    public abstract void doTranslate();

    /**
     * Checks if given type should be translated or not. This method returns always true
     * @param type the type to check
     * @return true, if given type shall be translated; otherwise false.
     */
    protected boolean acceptType(Type type) {
        return true;
    }

    public final boolean isSys() {
        return isSys;
    }

    /**
     * Checks if given type is a (unit) test (derives from sys::Test).
     * @return true, if type is a unit test
     */
    protected boolean isUnitTest() {
        final String test = type.kit.name + File.separator + "test";
        return TypeUtil.isaTest(type)  && !"sys::Test".equals(type.qname) ||
                // HACK
                type.loc.file.contains(test);
    }
    //endregion

    //region File header
    public void header() {
        w("//").nl();
        w("// sedonac translation").nl();
        w("// " + Env.timestamp()).nl();
        w("//").nl();
        nl();
    }
    //endregion


    //region Statements

    public void block(Block block) {
        indent().w("{").nl();
        ++indent;
        stmts(block.stmts);
        --indent;
        indent().w("}").nl();
    }

    public void stmts(ArrayList stmts) {
        for (Object stmt : stmts) {
            activeStatement = (Stmt) stmt;
            expressionStack.clear();
            stmt((Stmt) stmt);
        }
    }

    public void stmt(Stmt stmt) {
        stmt(stmt, true);
    }

    public void stmt(Stmt stmt, boolean standAlone) {
        switch (stmt.id) {
            case Stmt.EXPR_STMT:
                exprStmt((Stmt.ExprStmt) stmt, standAlone);
                break;
            case Stmt.LOCAL_DEF:
                localDef((Stmt.LocalDef) stmt, standAlone);
                break;
            case Stmt.RETURN:
                returnStmt((Stmt.Return) stmt);
                break;
            case Stmt.IF:
                ifStmt((Stmt.If) stmt);
                break;
            case Stmt.FOR:
                forStmt((Stmt.For) stmt);
                break;
            case Stmt.FOREACH:
                forEachStmt((Stmt.Foreach) stmt);
                break;
            case Stmt.WHILE:
                whileStmt((Stmt.While) stmt);
                break;
            case Stmt.DO_WHILE:
                doWhileStmt((Stmt.DoWhile) stmt);
                break;
            case Stmt.BREAK:
                breakStmt((Stmt.Break) stmt);
                break;
            case Stmt.CONTINUE:
                continueStmt((Stmt.Continue) stmt);
                break;
            case Stmt.SWITCH:
                switchStmt((Stmt.Switch) stmt);
                break;
            case Stmt.GOTO:
                gotoStmt((Stmt.Goto) stmt);
                break;

            case Stmt.ASSERT:
                assertStmt((Stmt.Assert) stmt);
                break;
            default:
                throw err("Unsupported statement: " + stmt.toString());
        }
    }

    public void exprStmt(Stmt.ExprStmt stmt, boolean standAlone) {
        if (standAlone) indent();
        expr(stmt.expr, true);

        if (standAlone) w(";").nl();
    }

    public void localDefs(Block block) {
        Stmt.LocalDef[] locals = TranslationUtil.findLocalVariables(block);
        for (Stmt.LocalDef local : locals) {
            indent().wtype(local.type).w(" ").w(local.name).w(";").nl();
        }
        if (locals.length > 0) nl();
    }


    public void localDef(Stmt.LocalDef stmt, boolean standAlone) {
        if (standAlone) indent();
        wtype(stmt.type).w(" ").w(stmt.name);
        if (stmt.init != null) {
            w(" = ");
            expr(stmt.init, true);
        }
        if (standAlone) w(";").nl();
    }

    public void returnStmt(Stmt.Return stmt) {
        indent().w("return");
        if (stmt.expr != null) {
            w(" ");
            expr(stmt.expr, true);
        }
        w(";").nl();
    }

    public void ifStmt(Stmt.If stmt) {
        indent().w("if (");
        expr(stmt.cond, true);
        w(")").nl();
        block(stmt.trueBlock);
        if (stmt.falseBlock != null) {
            indent().w("else").nl();
            block(stmt.falseBlock);
        }
    }

    public void forStmt(Stmt.For stmt) {
        indent().w("for (");
        if (stmt.init != null) stmt(stmt.init, false);
        w("; ");
        if (stmt.cond != null) expr(stmt.cond, true);
        w("; ");
        if (stmt.update != null) expr(stmt.update, true);
        w(")").nl();
        block(stmt.block);
    }

    private void forEachStmt(Stmt.Foreach stmt) {
        // for ( it = v.begin(); it != v.end(); ++it)
        indent().w("for(auto it = ");
        expr(stmt.array);
        w(".begin(); it != ");
        expr(stmt.array);
        w(".end(); ++it) ");
        block(stmt.block);
    }


    public void whileStmt(Stmt.While stmt) {
        indent().w("while (");
        expr(stmt.cond, true);
        w(")").nl();
        block(stmt.block);
    }

    public void doWhileStmt(Stmt.DoWhile stmt) {
        indent().w("do {");
        block(stmt.block);
        indent().w("} while (");
        expr(stmt.cond, true);
        w(")").nl();
    }

    private void gotoStmt(Stmt.Goto stmt) {
        indent().w("goto ").w(stmt.destLabel).w("; // stmt = ").w(stmt.destStmt).nl();
    }

    @SuppressWarnings("unused")
    public void breakStmt(Stmt.Break stmt) {
        indent().w("break;").nl();
    }

    @SuppressWarnings("unused")
    public void continueStmt(Stmt.Continue stmt) {
        indent().w("continue;").nl();
    }

    private void switchStmt(Stmt.Switch stmt) {
        indent().w("switch (");
        expr(stmt.cond);
        w(") {").nl();

        indent++;
        for (int i = 0; i < stmt.cases.length; i++) {
            Stmt.Case aCase = stmt.cases[i];

            indent().w("case ").w(aCase.label).w(":").nl();
            indent++;
            if (aCase.block != null) {
                stmts(aCase.block.stmts);
            } else {
                indent().w("// empty block").nl();
            }
            indent--;
        }
        indent--;
        indent().w("} // switch").nl();
    }

    public void assertStmt(Stmt.Assert stmt) {
        indent().w("assert(");
        expr(stmt.cond, true);
        w(");").nl();
    }
    //endregion

    /**
     * Processes a (non-top-level) expression from the AST. Same as <code>expr(expr, false)</code>.
     * @param expr the expression instance to process
     * @return processed expression (argument 'expr')
     *
     */
    public Expr expr(Expr expr) {
        return expr(expr, false);
    }

    /**
     * Processes an expression from the AST.
     * @param expr the expression instance to process
     * @param isTopLevel true, if
     * @return processed expression (argument 'expr')
     */
    public Expr expr(Expr expr, boolean isTopLevel) {
        if ((expr instanceof Expr.Literal)) {
            Expr.Literal literal = (Expr.Literal) expr;
            //w(String.format("/* Id=%d, T=%s %d, v=%s*/", expr.id, expr.getClass().getName(), literal.id, literal.value));
        } else if ((expr instanceof Expr.Binary)) {
            Expr.Binary binary = (Expr.Binary) expr;
            /*
            nl().w(String.format("/* Binary [%s|%b] %s %s [%s|%b] %s",
                    binary.lhs.getClass().getName(),
                    binary.lhs.type.isEnum(),
                    binary.lhs,
                    binary.op,
                    binary.rhs.getClass().getName(),
                    binary.rhs.type.isEnum(),
                    binary.rhs));*/
        } else {
            //nl().indent().w(String.format("/* '%s' (Id=%d,T=%s) */", expr, expr.id, expr.getClass().getName())).nl();
        }
        expressionStack.push(expr);

        try {
            switch (expr.id) {
                case Expr.TRUE_LITERAL:
                    trueLiteral();
                    break;
                case Expr.FALSE_LITERAL:
                    falseLiteral();
                    break;
                case Expr.INT_LITERAL:
                    intLiteral(((Expr.Literal) expr).asInt());
                    break;
                case Expr.LONG_LITERAL:
                    longLiteral(((Expr.Literal) expr).asLong());
                    break;
                case Expr.FLOAT_LITERAL:
                    floatLiteral(((Expr.Literal) expr).asFloat());
                    break;
                case Expr.DOUBLE_LITERAL:
                    doubleLiteral(((Expr.Literal) expr).asDouble());
                    break;
                case Expr.TIME_LITERAL:
                    timeLiteral((Expr.Literal) expr);
                    break;
                case Expr.NULL_LITERAL:
                    nullLiteral();
                    break;
                case Expr.STR_LITERAL:
                    stringLiteral((Expr.Literal) expr);
                    break;
                case Expr.BUF_LITERAL:
                    bufLiteral((Expr.Literal) expr);
                    break;
                case Expr.SLOT_LITERAL:
                    slotIdLiteral((Expr.Literal) expr);
                    break;
                case Expr.SLOT_ID_LITERAL:
                    slotLiteral((Expr.Literal) expr);
                    break;
                case Expr.TYPE_LITERAL:
                    typeLiteral((Expr.Literal) expr);
                    break;
                case Expr.ENUM_LITERAL:
                    enumLiteral((Expr.Literal) expr);
                    break;
                case Expr.ARRAY_LITERAL:
                    arrayLiteral((Expr.Literal) expr);
                    break;
                case Expr.SIZE_OF:
                    sizeOf((Expr.Literal) expr);
                    break;
                case Expr.NEGATE:
                case Expr.BIT_NOT:
                case Expr.COND_NOT:
                    unary((Expr.Unary) expr);
                    break;
                case Expr.PRE_INCR:
                case Expr.PRE_DECR:
                case Expr.POST_INCR:
                case Expr.POST_DECR:
                    increment((Expr.Unary) expr);
                    break;
                case Expr.COND_OR:
                case Expr.COND_AND:
                    cond((Expr.Cond) expr, isTopLevel);
                    break;
                case Expr.EQ:
                case Expr.NOT_EQ:
                case Expr.GT:
                case Expr.GT_EQ:
                case Expr.LT:
                case Expr.LT_EQ:
                case Expr.BIT_OR:
                case Expr.BIT_XOR:
                case Expr.BIT_AND:
                case Expr.LSHIFT:
                case Expr.RSHIFT:
                case Expr.ASSIGN_LSHIFT:
                case Expr.ASSIGN_RSHIFT:
                case Expr.MUL:
                case Expr.DIV:
                case Expr.MOD:
                case Expr.ADD:
                case Expr.SUB:
                    binary((Expr.Binary) expr, isTopLevel);
                    break;
                case Expr.ASSIGN:
                case Expr.ASSIGN_ADD:
                case Expr.ASSIGN_SUB:
                case Expr.ASSIGN_MUL:
                case Expr.ASSIGN_DIV:
                case Expr.ASSIGN_MOD:
                case Expr.ASSIGN_BIT_AND:
                case Expr.ASSIGN_BIT_OR:
                case Expr.ASSIGN_BIT_XOR:
                    assign((Expr.Binary) expr, isTopLevel);
                    break;
                case Expr.TERNARY:
                    ternary((Expr.Ternary) expr, isTopLevel);
                    break;
                case Expr.ELVIS:
                    binary((Expr.Binary) expr, isTopLevel);
                    break;
                case Expr.PARAM:
                    param((Expr.Param) expr);
                    break;
                case Expr.LOCAL:
                    local((Expr.Local) expr);
                    break;
                case Expr.FIELD:
                    field((Expr.Field) expr);
                    break;
                case Expr.INDEX:
                    index((Expr.Index) expr);
                    break;
                case Expr.CALL:
                case Expr.PROP_SET:
                    call((Expr.Call)expr, isTopLevel);
                    break;
                case Expr.CAST:
                    cast((Expr.Cast) expr);
                    break;
                case Expr.STATIC_TYPE:
                    staticType((Expr.StaticType) expr);
                    break;
                // Added branches
                case Expr.NAME:
                    name((Expr.Name) expr);
                    break;
                case Expr.THIS:
                    self((Expr.This) expr);
                    break;
                case Expr.SUPER:
                    callSuper((Expr.Super) expr);
                    break;
                case Expr.INIT_VIRT:
                    initVirtual((Expr.InitVirt) expr);
                    break;
                case Expr.INIT_COMP:
                    initComponent((Expr.InitComp) expr);
                    break;
                case Expr.INIT_ARRAY:
                    // internal inline Handler[handlersLen] handlers  = {...}

                    initArray((Expr.InitArray)expr);
                    break;
                case Expr.INTERPOLATION:
                    interpolation((Expr.Interpolation) expr);
                    break;
                case Expr.NEW:
                    newObject((Expr.New) expr);
                    break;
                case Expr.DELETE:
                    deleteObject((Expr.Delete) expr);
                    break;

                default:
                    throw err("AbstractTranslator not done: " + expr.id + " " + expr.toString(), expr.loc);
            }
        } catch (Throwable e) {
            log.error(String.format("Error processing expression: %s (%s)", expr, expr.loc));
            dumpExpressionStack();
            throw e;
        }
        return expr;
    }

    //region Literals

    public void trueLiteral() {
        w("true");
    }
    public void falseLiteral() {
        w("false");
    }

    public void intLiteral(int v) {
        w(v);
    }

    public void longLiteral(long v) {
        w(v);
    }

    public void floatLiteral(float v) {
        w(Env.floatFormat(v)).w("f");
    }

    public void doubleLiteral(double v) {
        w(Env.doubleFormat(v));
    }

    public void nullLiteral() {
        w("null");
    }

    public void typeLiteral(Expr.Literal expr) {
        w(expr);
    }

    public void slotLiteral(Expr.Literal expr) {
        w(expr.value.toString());
    }

    public void slotIdLiteral(Expr.Literal expr) {
        w("/* Id */ ");
        w(expr.value.toString());
    }

    public void name(Expr.Name expr) {
        w(expr.toString());
    }

    public void stringLiteral(Expr.Literal expr) {
        w(expr);
    }

    public void bufLiteral(Expr.Literal expr) { w(expr); }

    public void timeLiteral(Expr.Literal expr) {
        w(expr);
    }

    public void enumLiteral(Expr.Literal expr) {
        w(expr);
    }

    public void arrayLiteral(Expr.Literal expr) {
        w(expr);
    }

    //endregion

    //region Expressions
    private void sizeOf(Expr.Literal expr) {
        w("sizeof(").w(expr).w(")");
    }

    public void unary(Expr.Unary expr) {
        w(expr.op);
        w(" ");
        expr(expr.operand);
        w(" ");
    }

    public void increment(Expr.Unary expr) {
        if (!expr.isPostfix()) w(expr.op);
        w("(");
        expr(expr.operand);
        w(")");
        if (expr.isPostfix()) w(expr.op);
    }

    public void cond(Expr.Cond expr, boolean top) {
        if (!top) w("(");
        for (int i = 0; i < expr.operands.size(); ++i) {
            if (i > 0) w(expr.op);
            expr((Expr) expr.operands.get(i));
        }
        if (!top) w(")");
    }

    public void binary(Expr.Binary expr, boolean top) {
        if (!top) w("(");
        expr(expr.lhs);
        w(" ").w(expr.op).w(" ");
        expr(expr.rhs);
        if (!top) w(")");
    }

    public void assign(Expr.Binary expr, boolean top) {
        if (!top) w("(");
        expr(expr.lhs);
        w(" ").w(expr.op).w(" ");
        assignNarrow(expr.lhs.type, expr.rhs);
        if (!top) w(")");
    }

    public void assignNarrow(Type lhs, Expr rhs) {
        if (lhs.isByte()) {
            w("(");
            expr(rhs);
            w(" & 0xFF)");
        } else if (lhs.isShort()) {
            w("(");
            expr(rhs);
            w(" & 0xFFFF)");
        } else {
            expr(rhs);
        }
    }

    public void ternary(Expr.Ternary expr, boolean top) {
        if (!top) w("(");
        expr(expr.cond);
        w(" ? ");
        expr(expr.trueExpr);
        w(" : ");
        expr(expr.falseExpr);
        if (!top) w(")");
    }
    //endregion

    //region Declarations (parameters, (local) fields, ...)
    public void param(Expr.Param expr) {
        w(expr.name);
    }

    public void local(Expr.Local expr) {
        // use def b/c we might change name for C
        w(expr.def.name);
    }

    public void field(Expr.Field expr) {
        if (expr.target != null) {
            expr(expr.target);
            w(".");
        }

        w(expr.name);
    }

    public void index(Expr.Index expr) {
        expr(expr.target);
        w("[");
        expr(expr.index);
        w("]");
    }
    //endregion

    //region Calls
    public void call(Expr.Call expr, boolean isTopLevel) {
        if (expr.target != null) {
            expr(expr.target);
            w(".");
        }

        w(expr.name);
        callArgs(expr);
    }

    public void callArgs(Expr.Call expr) {
        w("(");
        for (int i = 0; i < expr.args.length; ++i) {
            if (i > 0) w(", ");
            expr(expr.args[i]);
        }
        w(")");
    }
    //endregion

    //region Type support
    public void cast(Expr.Cast expr) {
        w("((");
        wtype(expr.type);
        w(")");
        expr(expr.target);
        w(")");
    }

    public void staticType(Expr.StaticType expr) {
        wtype(expr.type);
    }
    //endregion

    //region THIS and super
    /**
     * Represents a 'this' expression.
     * @param expr the expression
     */
    protected abstract void self(Expr.This expr);

    protected abstract void callSuper(Expr.Super expr);


    //endregion

    //region Interpolation (what's this?)
    /**
     * Array of values, e. g. "foo", "bar", baz" (?)
     * @param expr the expression
     */
    protected abstract void interpolation(Expr.Interpolation expr);
    //endregion

    //region Init
    protected abstract void initVirtual(Expr.InitVirt expr);

    protected abstract void initComponent(Expr.InitComp expr);

    protected abstract void initArray(Expr.InitArray expr);


    /**
     * Represents a 'new' call (allocates memory)
     * @param expr the expression
     */
    protected abstract void newObject(Expr.New expr);

    /**
     * Represents a 'new' call (frees memory)
     * @param expr the expression
     */
    protected abstract void deleteObject(Expr.Delete expr);

    //endregion

//////////////////////////////////////////////////////////////////////////
// Typing
//////////////////////////////////////////////////////////////////////////

    public abstract String toType(Type t);

//////////////////////////////////////////////////////////////////////////
// Write
//////////////////////////////////////////////////////////////////////////

    public AbstractTranslator w(Object s) {
        out.print(s);
        return this;
    }

    public AbstractTranslator w(int i) {
        out.print(i);
        return this;
    }

    public AbstractTranslator wtype(Type t) {
        out.print(toType(t));
        return this;
    }

    public AbstractTranslator indent() {
        out.print(TextUtil.getSpaces(indent * SPACES_PER_INDENT));
        return this;
    }

    public AbstractTranslator nl() {
        out.println();
        return this;
    }

//////////////////////////////////////////////////////////////////////////
// Stack
//////////////////////////////////////////////////////////////////////////
    private void dumpExpressionStack() {
        int i = 0;
        if (activeStatement != null) {
            log.debug(String.format("Stmt [%s] %s, %s", activeStatement.getClass().getName(), activeStatement, activeStatement.loc));
        }
        while (!expressionStack.isEmpty()) {
            Expr expr = expressionStack.pop();
            log.debug(String.format("[%03d|%s] %s", i, expr.getClass().getName(), expr));
            i++;
        }
    }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

    /**
     * Indentation (number of spaces)
     */
    public static final int SPACES_PER_INDENT = 4;
    public TypeDef type;

    public File outDir;
    public PrintWriter out;
    public int indent;

    private final boolean isSys;
    private Stmt activeStatement = null;
    private Stack<Expr> expressionStack = new Stack<>();
}
