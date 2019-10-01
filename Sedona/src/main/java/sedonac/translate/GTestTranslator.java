/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.Expr;
import sedonac.ast.MethodDef;
import sedonac.ast.Stmt;
import sedonac.ast.TypeDef;
import sedonac.parser.Token;

import static sedonac.translate.CppDefaults.isStdFileStream;
import static sedonac.translate.TranslationUtil.isCppType;
import static sedonac.translate.TranslationUtil.isTestMethod;

public class GTestTranslator extends CppTranslator {

    public GTestTranslator(Compiler compiler, TypeDef type) {
        super(compiler, type);
    }

    public void assertStmt(Stmt.Assert stmt) {

        if (stmt.cond instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) stmt.cond;
            if (binary.op.type == Token.EQ) {
                assertEqualStmt(binary.rhs, binary.lhs, "EXPECT_EQ");
            } else if (binary.op.type == Token.NOT_EQ) {
                assertEqualStmt(binary.rhs, binary.lhs, "EXPECT_NEQ");
            } else {
                assertTrueStmt(stmt);
            }
        } else {
            assertTrueStmt(stmt);
        }
    }

    /**
     * Writes a equal/not equal assertion.
     * @param rhs the RHS expr (expected value)
     * @param lhs the LHS expr (value to test on)
     * @param assertStatement the assert statement, e. g. "EXPECT_NEQ"
     */
    private void assertEqualStmt(Expr rhs, Expr lhs, String assertStatement) {
        indent().w(assertStatement);
        w("(");
        expr(rhs, true);
        w(", ");
        expr(lhs, true);
        w(");").nl();
    }

    /**
     * Writes "EXPECT_TRUE" assertion
     * @param stmt the assert statement
     */
    private void assertTrueStmt(Stmt.Assert stmt) {
        indent().w("EXPECT_TRUE(");
        expr(stmt.cond, true);
        w(");").nl();
    }

    @Override
    public void header() {
        header("Test for for class '" + type.name + "'.");
    }

    @Override
    protected void translateCPlusPlus() {
        w("").nl();
        w("/* STL includes */").nl();
        for (String STL_INCLUDE : STL_INCLUDES) {
            w(String.format("#include <%s>", STL_INCLUDE)).nl();
        }

        nl();

        w("/* VS precompiled header */").nl();
        w("//#include \"pch.h\"").nl();

        // include our own header
        renderInclude(type);
        // required include files for impl
        includes(true, t -> !isCppType(t.qname()));

        if (!CppDefaults.getCppOptions().requiresNamespaceInSource()) {
            w("").nl();
            w("using namespace std;").nl();
        }

        nl();
        beginNamespace();


        if (CppDefaults.getCppOptions().isGenerateMethodImpl()) {
            if (type.isaVirtual()) {
                writeDocBlock("Destructor");
                indent().w(type.name).w("::~").w(type.name).w("() {}").nl().nl();
            }

            translateMethod(m -> !m.isAbstract() && m.isInstanceInit(), this::writeTestSetupImpl, false);

            indent().w(String.format("void %s::SetUpTestCase() {}", type.name)).nl();
            indent().w(String.format("void %s::TearDown() {}", type.name)).nl();
            indent().w(String.format("void %s::TearDownTestCase() {}", type.name)).nl();

            // Test method
            translateMethod(m -> !m.isAbstract() && !m.isInstanceInit() && isTestMethod(m), this::writeTestMethodImpl, false);
            // Helper methods
            beginRegion("Helpers");
            translateMethod(m -> !m.isAbstract() && !m.isInstanceInit() && !isTestMethod(m), this::writeMethodImpl, false);
            endRegion();

            beginRegion("Static field init");
            translateField(f -> f.isStatic() && !isStdFileStream(f.type) && !f.synthetic, this::writeFieldDeclaration, false);
            endRegion();
        } else {
            nl().nl();
            w("// NOTE: Translation for implementation is disabled. Set generateMethodImpl=\"true\" in XML file to enable it").nl().nl();
        }

        endNamespace();
    }

    private void writeTestSetupImpl(MethodDef methodDef, TranslateContext translateContext) {
        translateContext.initFromMethod(methodDef);
        // Doc header
        if (methodDef.isInstanceInit()) {
            writeDocBlock(methodDef, String.format("Test setup for '%s (%s)'", type.name, methodDef.name));
        } else {
            throw new TranslationException("Method is not instance init", methodDef.loc);
        }
        // Method signature
        //w(methodSignature(methodDef, translateContext));
        w(String.format("void %s::SetUp()", type.name));
        // Method body (safe for null argument)
        block(methodDef.code);
        nl();
    }

    private void writeTestMethodImpl(MethodDef methodDef, TranslateContext translateContext) {
        translateContext.initFromMethod(methodDef);
            // Doc header
            if (methodDef.isInstanceInit()) {
                writeDocBlock(methodDef, String.format("Constructor for '%s (%s)'", type.name, methodDef.name));
            } else {
                writeDocBlock(methodDef, String.format("Implementation of method '%s'", methodDef.name));
            }
            // Method signature
            //w(methodSignature(methodDef, translateContext));
            w("TEST_F(").w(type.name).w(", ").w(methodDef.name).w(") {").nl();
            // Method body (safe for null argument)
            block(methodDef.code);
            w("}").nl();
            nl();
    }
}
