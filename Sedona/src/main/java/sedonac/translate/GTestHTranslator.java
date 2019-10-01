package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.SlotDef;
import sedonac.ast.TypeDef;
import sedonac.namespace.Slot;

import static sedonac.translate.TranslationUtil.*;

public class GTestHTranslator extends CppHTranslator {
    public GTestHTranslator(Compiler compiler, TypeDef type) {
        super(compiler, type);
    }

    @Override
    protected void translateCPlusPlus() {
        beginGuard();

        lineComment("STL includes");
        for (String STL_INCLUDE : STL_INCLUDES) {
            w(String.format("#include <%s>", STL_INCLUDE)).nl();
        }
        w("").nl();
        // Internal includes
        for (String include : HELPER_INCLUDES) {
            String helperInclude = makeSysInclude(type, include);
            w(String.format("#include \"%s\"", helperInclude)).nl();
        }

        includes(false, t -> !isCppType(t.qname()));

        w("/* gtest include */").nl();
        w("#include \"gtest/gtest.h\"").nl();

        /* NOTE: Includes must go before the namespace decl!! */
        w("").nl();
        w("").nl();
        beginNamespace();

        w("").nl();
        if (type.doc != null) {
            writeDocBlock(type.doc);
        }

        // If no base class is given, we derive from gtest base class
        String baseTypeName = "::testing::Test";
        final String qname = type.base.qname();
        if (type.base != null && !"sys::Test".equals(qname)) {
            baseTypeName = qname;
        }

        if (baseTypeName != null) {
            w("class " + type.name + " : public " + baseTypeName + " {").nl();
        } else {
            w("class " + type.name + " {").nl();
        }

        final TranslateContext methodContext = makeMethodContext(true);

        indent++;
        beginRegion("Public members");
        indent().w("public:").nl();
        indent++;

        // GTest methods
        indent().w("virtual void SetUp();").nl();
        indent().w("virtual void TearDown();").nl();
        indent().w("static void SetUpTestCase();").nl();
        indent().w("static void TearDownTestCase();").nl();


        // We need a private destructor, if type has virtual functions
        if (type.isaVirtual()) {
            // If we have a non-default dtor, we need also copy and move ctor (https://en.cppreference.com/w/cpp/language/move_constructor)
            //writeCopyConstructor(f -> isCppVariable(f), HEADER_CONTEXT);
            //writeMoveConstructor(f -> !f.isStatic(), HEADER_CONTEXT);
            writeDocBlock("Virtual destructor");
            indent().w("virtual ~").w(type.name).w("();").nl();
        }
        nl();

        beginRegion("Properties");
        translateField(SlotDef::isProperty, this::writePropertiesDecl, true);
        endRegion();


        beginRegion("Public members/methods");
        writeHeaderMethods(m -> m.isPublic() && !m.isInstanceInit() && !isTestMethod(m), methodContext);

        translateField(f -> f.isPublic() && !f.isProperty(), this::writeFieldDeclaration, true);

        endRegion();
        indent--;
        endRegion(); // public

        indent().w("protected:").nl();
        beginRegion("Protected members/methods");
        indent++;
        writeHeaderMethods(SlotDef::isProtected, methodContext);
        writeHeaderMethods(SlotDef::isInternal, methodContext);

        beginRegion("Fields");
        translateField(f -> matchFlags(f, Slot.PROTECTED|Slot.INTERNAL), this::writeFieldDeclaration, true);
        endRegion();
        indent--;
        endRegion(); // protected

        // Private members
        beginRegion("Private members/methods");

        indent().w("private:").nl();
        indent++;
        writeHeaderMethods(SlotDef::isPrivate, methodContext);

        beginRegion("Fields");
        translateField(SlotDef::isPrivate, this::writeFieldDeclaration, true);
        endRegion();
        indent--;
        endRegion();

        indent().w("}; // class " + type.name).nl().nl();
        endNamespace();

        endGuard();
    }
}
