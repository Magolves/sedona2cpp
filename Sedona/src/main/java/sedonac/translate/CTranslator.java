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

import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.namespace.*;

import static sedonac.translate.TranslationUtil.findDependentTypes;

/**
 * CTranslator
 */
public class CTranslator
        extends AbstractTranslator {

    public static final String THIS = "this";

//////////////////////////////////////////////////////////////////////////
// Constructor
//////////////////////////////////////////////////////////////////////////

    public CTranslator(Compiler compiler, TypeDef type) {
        super(compiler, type);
    }

//////////////////////////////////////////////////////////////////////////
// Translate
//////////////////////////////////////////////////////////////////////////

    public File toFile() {
        return new File(outDir, qname(type) + ".c");
    }

    public void doTranslate() {
        renderInclude(type);
        nl();
        w("extern bool approx(float a, float b);").nl();
        w("extern void doAssert(bool cond, const char* file, int line);").nl();
        w("#define assert(c) doAssert((c), __FILE__, __LINE__)").nl();
        nl();
        staticFields();
        nl();
        MethodDef[] methods = type.methodDefs();
        for (int i = 0; i < methods.length; ++i)
            method(methods[i]);

        if (type.qname.equals("sys::Obj")) {
            nl();
            w("bool approx(float a, float b)").nl();
            w("{").nl();
            w("  return a < b ? b-a < 0.001f : a-b < 0.001f;").nl();
            w("}").nl();
            nl();
            w("static int successes = 0;").nl();
            w("static int failures = 0;").nl();
            w("void doAssert(bool cond, const char* file, int line)").nl();
            w("{").nl();
            w("  if (cond) successes++;").nl();
            w("  else failures++;").nl();
            w("}").nl();
            nl();
            w("int main(int argc, char** argv)").nl();
            w("{").nl();
            w("  sys_FieldTest__staticInit();").nl();
            w("  sys_Sys_main();").nl();
            w("  printf(\"-- successes = %d\\n\", successes);").nl();
            w("  printf(\"-- failures  = %d\\n\", failures);").nl();
            w("}").nl();
        }
    }

//////////////////////////////////////////////////////////////////////////
// Includes
//////////////////////////////////////////////////////////////////////////

    public void includes(boolean withImpl, TranslationUtil.ITypeSelector filter) {
        Type[] includes = findDependentTypes(this.type, filter, withImpl);
        for (int i = 0; i < includes.length; ++i) {
            final Type include = includes[i];
            if (filter.acceptType(type)) {
                renderInclude(include);
            }
        }
    }

    protected void renderInclude(Type include) {
        w("#include \"").w(qname(include)).w(".h\"").nl();
    }

    protected void renderForwardDecl(Type include) {
        w("class ").w(qname(include)).w(";").nl();
    }

//////////////////////////////////////////////////////////////////////////
// Field
//////////////////////////////////////////////////////////////////////////

    public void staticFields() {
        // static field containing internal vars
        w("/* Struct containing static fields */").nl();
        w(String.format("static %s _%s;", toType(type, true), THIS, THIS)).nl().nl();
        w("/* 'THIS' pointer */").nl();
        w(String.format("static %s %s = &_%s;", toType(type), THIS, THIS)).nl().nl();

        FieldDef[] fields = type.fieldDefs();
        for (int i = 0; i < fields.length; ++i) {
            FieldDef f = fields[i];
            if (f.isStatic()) {
                fieldSig(f);
                w(";").nl();
            }
        }
    }

    public void fieldSig(FieldDef f) {
        w(toType(f.type, f.isInline()));
        w(" ").w(qname(f));
        if (f.type.isArray() && f.isInline())
            w("[").w(f.type.arrayLength()).w("]");
    }

//////////////////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////////////////

    public void method(MethodDef m) {
        methodSig(m).nl();

        if (m.code != null) {
            block(m.code);
        } else if (m.isNative()) {
            // native call
            nativeCall(m);
        }
        nl();
    }

    public CTranslator methodSig(MethodDef m) {
        wtype(m.ret).w(" ").w(qname(m)).w("(");
        for (int i = 0; i < m.params.length; ++i) {
            ParamDef p = m.params[i];
            if (i > 0) w(", ");
            wtype(p.type).w(" ").w(p.name);
        }
        w(")");
        return this;
    }

//////////////////////////////////////////////////////////////////////////
// Block
//////////////////////////////////////////////////////////////////////////

    public void block(Block block) {
        if (block != null) {
            indent().w("{").nl();
            ++indent;
            localDefs(block);
            stmts(block.stmts);
            --indent;
            indent().w("}").nl();
        } else {
            indent().w("/* / Native or external function call */").nl();
            indent().w("{ throw \"Not implemented yet\"; } ").nl();
        }
    }

    //////////////////////////////////////////////////////////////////////////
// Stmts
//////////////////////////////////////////////////////////////////////////

    public void localDef(Stmt.LocalDef stmt, boolean standAlone) {
        // we only output initializers, since we declare at top of block
        if (stmt.init != null) {
            if (standAlone) indent();
            w(stmt.name).w(" = ");
            expr(stmt.init, true);
            if (standAlone) w(";").nl();
        }
    }

//////////////////////////////////////////////////////////////////////////
// Expr
//////////////////////////////////////////////////////////////////////////

    public void trueLiteral() {
        w("TRUE");
    }

    public void falseLiteral() {
        w("FALSE");
    }

    public void nullLiteral() {
        w("NULL");
    }

    public void binary(Expr.Binary expr, boolean top) {
        if (expr.id == Expr.EQ && expr.lhs.type.isFloat()) {
            w("approx(");
            expr(expr.lhs, true);
            w(", ");
            expr(expr.rhs, true);
            w(")");
        } else {
            super.binary(expr, top);
        }
    }

    public void field(Expr.Field expr) {
        Field f = expr.field;
        w("(");
        if (f.isStatic()) {
            if (f.isInline() && !f.type().isArray())
                w("(&").w(qname(f)).w(")");
            else
                w(qname(f));
        } else {
            expr(expr.target);
            w("->");
            w(expr.name);
        }
        w(")");
    }

    public void assignNarrow(Type lhs, Expr rhs) {
        if (lhs.isByte()) {
            w("(uint8_t)(");
            expr(rhs);
            w(")");
        } else if (lhs.isShort()) {
            w("(uint16_t)(");
            expr(rhs);
            w(")");
        } else {
            expr(rhs);
        }
    }

    protected void initVirtual(Expr.InitVirt expr) {
        w(expr);
        if (log.isDebug()) {
            w(" /* InitVirt */");
        }
    }

    protected void initComponent(Expr.InitComp expr) {
        w(expr);
        if (log.isDebug()) {
            w(" /* InitComp */");
        }
    }

    @Override
    protected void initArray(Expr.InitArray expr) {
        w(String.format(" /* InitArray t=%s/f=%s */", expr.type, expr.field));
        w(expr.length);
    }

    @Override
    protected void self(Expr.This expr) {
        w(THIS);
    }

    @Override
    protected void callSuper(Expr.Super expr) {
        // Any ideas?
        err("Not implemented (?)");
    }

    protected void interpolation(Expr.Interpolation expr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expr.parts.size(); i++) {
            Expr part = (Expr) expr.parts.get(i);
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(part);
        }
        w(String.format("%s", sb.toString()));
        if (log.isDebug()) {
            w("/* " + expr.loc + " */");
        }
    }


    @Override
    protected void newObject(Expr.New expr) {
        w("malloc(");
        w(String.format("sizeof(%s)", toType(expr.of)));
        if (expr.arrayLength != null) {
            w(String.format(" * %s", expr.arrayLength));
        }
        w(")");
    }

    @Override
    protected void deleteObject(Expr.Delete expr) {
        w("free(");
        w(expr.target);
        w(")");
    }


//////////////////////////////////////////////////////////////////////////
// Type Utils
//////////////////////////////////////////////////////////////////////////

    public String qname(Type type) {
        return type.kit().name() + "_" + type.name();
    }

    public String qname(Slot s) {
        return qname(s.parent()) + "_" + s.name();
    }

    public String toType(Type type) {
        return toType(type, false);
    }

    public String toType(Type type, boolean inline) {
        String s = toInlineType(type);
        if (type.isRef() && !inline) s += "*";
        return s;
    }

    public String toInlineType(Type type) {
        if (type.isArray()) {
            return toType(type.arrayOf());
        } else if (type.isPrimitive()) {
            if (type.isBool()) return "bool";
            if (type.isByte()) return "uint8_t";
            if (type.isShort()) return "uint16_t";
            if (type.isInt()) return "int32_t";
            return type.signature();
        } else if (type instanceof StubType) {
            // shouldn't need this once I wrap up
            // bootstrap type resolve
            return "/*STUB!*/sys::" + type.name();
        } else {
            return qname(type);
        }
    }

    public void nativeCall(MethodDef m) {
        w(" {").nl();
        indent().w(String.format("// native call: %s);",
                m.nativeId)).nl();
        indent().w(String.format("return (%s)0;", m.ret)).nl();
        w(" }").nl();
    }
    //endregion


}
