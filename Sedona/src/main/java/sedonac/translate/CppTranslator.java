/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.namespace.*;

import java.io.File;
import java.util.*;

import static sedonac.translate.CppDefaults.*;
import static sedonac.translate.TranslationUtil.*;

@SuppressWarnings("WeakerAccess")
public class CppTranslator extends CTranslator {
    static final String TRUE_LITERAL = "true";
    static final String FALSE_LITERAL = "false";

    static final String[] STL_INCLUDES = new String[]{"vector", "iterator", "iostream", "fstream", "sstream", "stdint.h"};
    // List of additional internal includes
    static final String[] HELPER_INCLUDES = new String[]{SYS_APPROX, PROPERTY_CLASS, "Units"};
    static final TranslateContext HEADER_CONTEXT = new TranslateContext(true);
    static final TranslateContext SOURCE_CONTEXT = new TranslateContext(false);

    public CppTranslator(Compiler compiler, TypeDef type) {
        super(compiler, type);
    }

    @Override
    public final void doTranslate() {
        // Ignore types which are covered by native C++ classes (e. g. streams)
        if (isCppType(type.qname())) {
            log.debug("Ignore type " + type.qname);
            return;
        }

        translateCPlusPlus();
    }

    protected void translateCPlusPlus() {
        w("").nl();
        w("/* STL includes */").nl();
        for (String STL_INCLUDE : STL_INCLUDES) {
            w(String.format("#include <%s>", STL_INCLUDE)).nl();
        }

        w("").nl();

        // include our own header
        renderInclude(type);
        // required include files for impl
        includes(true, t -> !isCppType(t.qname()));

        if (!CppDefaults.getCppOptions().requiresNamespaceInSource()) {
            w("").nl();
            w("// NOTE: Set usingStd=\"false\" to get full-qualified STL types").nl();
            w("using namespace std;").nl();
        }

        w("").nl();
        beginNamespace();
        if (CppDefaults.getCppOptions().isGenerateMethodImpl()) {
            // Ctor
            translateMethod(MethodDef::isInstanceInit, this::writeMethodImpl, false);
            // Dtor
            translateMethod(MethodDef::isInstanceDestroy, this::writeMethodImpl, false);
            // Method impl
            translateMethod(m -> !m.isAbstract() && !m.isInitOrDestroy(), this::writeMethodImpl, false);
            // Properties
            translateField(SlotDef::isProperty, this::writePropertiesImpl, false);
            // Action
            translateMethod(SlotDef::isAction, this::writeActionsImpl, false);
            // Slots
            writeSlotArrayImpl(type);
            // Static fields
            translateField(f -> f.isStatic() && !isStdFileStream(f.type) && !f.synthetic && !isHeaderInitPossible(f), this::writeFieldDeclaration, false);
        } else {
            nl().nl();
            w("// NOTE: Translation for implementation is disabled. Set generateMethodImpl=\"true\" in XML file to enable it").nl().nl();
        }
        endNamespace();
    }


    @Override
    protected boolean acceptType(Type type) {
        return !isCppType(type.qname()) && !type.isEnum();
    }

    //region File-related methods

    /**
     * Gets the guard string for the header file
     *
     * @return the guard string for the header file
     */
    final String getBarrier() {
        return (type.kit.name + "_" + type.name + "_H").toUpperCase();
    }

    protected void beginGuard() {
        String barrier = getBarrier();
        w("#ifndef ").w(barrier).nl();
        w("#define ").w(barrier).nl().nl();
    }

    protected void endGuard() {
        nl();
        w("#endif").nl();
    }

    @Override
    public File toFile() {
        File dir = new File(outDir + File.separator + getSubdirectory());

        // Check if current class is a unit test
        if (isUnitTest()) {
            dir = new File(outDir + File.separator + getSubdirectory() + File.separator + "test");
        }

        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return new File(dir, type.name + "." + getFileExt());
    }

    /**
     * Gets the sub directory within the output directory (name of the kit).
     *
     * @return the sub directory
     */
    private String getSubdirectory() {
        return type.kit.name;
    }

    /**
     * Gets the file extension.
     *
     * @return see {@link CppDefaults#DEFAULT_HEADER_EXTENSION}
     */
    public String getFileExt() {
        return CppDefaults.getSourceExtension();
    }

    @Override
    public void header() {
        header("Implementation for class '" + type.name + "'.");
    }

    public void header(String purpose) {
        w("/*********************************************************").nl();
        w(" * " + purpose).nl();
        w(" * " + COPY_RIGHT).nl();
        w(" * Tag      : $Id$").nl();
        w(" * Namespace: " + type.kit.name).nl();
        w(" * Class    : " + type.name).nl();
        w(" * Generated: " + new Date()).nl();
        w(" *********************************************************/").nl().nl();
    }

    @Override
    protected void renderInclude(Type include) {
        if (isCppType(include.qname())) {
            return;
        }

        if (include.isArray()) {
            throw new TranslationException("Cannot include array type: " + include, this);
        }

        if (type.kit.name.equals(include.kit().name())) {
            if (isUnitTest()) {
                if (type.name.equals(include.name())) {
                    // Out own header file
                    w(String.format("#include \"%s.%s\"", include.name(), CppDefaults.getHeaderExtension())).nl();
                } else {
                    // else look in parent dir
                    w(String.format("#include \"../%s/%s.%s\"", type.kit.name, include.name(), CppDefaults.getHeaderExtension())).nl();
                }
            } else {
                w(String.format("#include \"%s.%s\"", include.name(), CppDefaults.getHeaderExtension())).nl();
            }
        } else {
            w(String.format("#include \"../%s/%s.%s\"", include.kit().name(), include.name(), CppDefaults.getHeaderExtension())).nl();
        }
    }

    protected void endNamespace() {
        w("} // namespace " + type.kit.name).nl();
    }

    protected void beginNamespace() {
        w("namespace " + type.kit.name + " {").nl().nl();
    }
    //endregion

    /**
     * Generates the method signature for the given method def with modifiers.
     *
     * @param methodDef the method def
     * @return the signature, e. g. 'void foo(int bar, int baz)'
     */
    String methodSignature(MethodDef methodDef, TranslateContext translateContext) {
        translateContext.initFromMethod(methodDef);

        StringBuilder s = new StringBuilder();
        // Method modifiers
        if (translateContext.isHeader()) {
            // static and virtual are not allowed in source and cannot be combined
            if (translateContext.isVirtual()) {
                s.append("virtual ");
            } else if (translateContext.isStatic()) {
                s.append("static ");
            }

            if (methodDef.isInline()) {
                s.append("inline ");
            }
        }

        if (!methodDef.isInitOrDestroy()) {
            s.append(toType(methodDef.ret, translateContext));
            if (methodDef.ret.isaVirtual() || isLink(methodDef.ret)) {
                if (isStream(methodDef.ret)) {
                    s.append("&");
                } else {
                    s.append("*");
                }
            }
            s.append(" ");
        }

        if (translateContext.isSource()) {
            s.append(type.name).append("::");
        }

        if (methodDef.isInitOrDestroy()) {
            if (methodDef.isInstanceDestroy()) {
                s.append("~");
            }
            s.append(type.name);
        } else {
            s.append(toCppLiteral(methodDef.name));
        }
        s.append("(");
        for (int i = 0; i < methodDef.params.length; i++) {
            ParamDef paramDef = methodDef.params[i];
            if (i > 0) {
                s.append(", ");
            }

            final String type = toType(paramDef.type, translateContext);
            // Pass virtuals and arrays as ref
            if (paramDef.type.isaVirtual() || paramDef.type.isArray()) {
                s.append(type);
                s.append("&");
            } else {
                s.append(type);
            }
            s.append(" ");
            s.append(toCppLiteral(paramDef.name));
        }
        s.append(")");

        if (translateContext.isHeader()) {
            if (translateContext.isOverride()) {
                s.append(" override ");
            }
        }
        if (methodDef.isConst() && !methodDef.isStatic()) {
            s.append(" const");
        }

        return s.toString();
    }

    ///////
    void translateField(IFieldSelector fieldSelector, IFieldDefWriter writer, boolean isHeader) {
        TranslateContext translateContext = makeMethodContext(isHeader);
        for (FieldDef f : type.fieldDefs()) {
            if (fieldSelector.acceptField(f)) {
                translateContext.initFromField(f);
                writer.writeField(f, translateContext);
            }
        }
    }

    /////////////////////////////
    void translateMethod(IMethodSelector methodSelector, IMethodDefWriter writer, boolean isHeader) {
        TranslateContext translateContext = makeMethodContext(isHeader);
        for (MethodDef methodDef : type.methodDefs()) {
            if (methodSelector.acceptMethod(methodDef)) {
                translateContext.initFromMethod(methodDef);
                writer.writeMethod(methodDef, translateContext);
            }
        }
    }

    void translateSlot(ISlotSelector slotSelector, IFieldWriter fW, IMethodWriter mW, boolean isHeader) {
        TranslateContext translateContext = makeFieldContext(isHeader);

        List<Slot> slots = TranslationUtil.collectSlots(this.type, slotSelector);

        for (Slot slot : slots) {
            if (slot.isField()) {
                fW.writeField((Field) slot, translateContext);
            } else if (slot.isMethod()) {
                mW.writeMethod((Method) slot, translateContext);
            } else {
                log.warn("Unknown slot type: " + slot);
            }
        }
    }

    //region Writers
    void writeMethodImpl(MethodDef methodDef, TranslateContext translateContext) {
        translateContext.initFromMethod(methodDef);
        if (methodDef.isStaticInit()) {
            w("/* Static init not supported yet */").nl();
        } else {
            // Doc header
            if (methodDef.isInitOrDestroy()) {
                String format = "Constructor for '%s (%s)'";
                if (methodDef.isInstanceDestroy()) {
                    format = "Virtual Destructor for '%s (%s)'";
                }
                writeDocBlock(methodDef, String.format(format, type.name, methodDef.name));
            } else {
                writeDocBlock(methodDef, String.format("Implementation of method '%s'", methodDef.name));
            }
            // Method signature
            w(methodSignature(methodDef, translateContext));
            // Method body (safe for null argument)
            block(methodDef.code);
            nl();
        }
    }

    public void writeFieldDeclaration(FieldDef f, TranslateContext context) {
        String fieldType = toType(f.type, context);

        indent();
        w(fieldType);
        if (fieldIsPointer(f)) {
            w("*");
        }
        w(" ");

        // Source context
        if (context.isSource() && f.isStatic()) {
            w(type.name).w(CPP_NS_SEP);
        }
        w(f.name);

        boolean withInit;
        boolean headerInitPossible = isHeaderInitPossible(f);

        if (context.isHeader()) {
            withInit = headerInitPossible;
        } else {
            withInit = !headerInitPossible;
        }

        if (withInit) {
            w(" = ");
            if (f.init != null) {
                expr(f.init);
            } else {
                w(defaultValue(f.type, f.ctorArgs));
            }
        }
        w(";").nl();
    }

    protected boolean fieldIsPointer(FieldDef f) {
        boolean isNullInit = f.init != null && f.init.isNullLiteral(f.type);
        return !f.isInline() && !f.type.isPrimitive() && !f.type.isArray() && !f.type.isStr() || isNullInit || isStdFileStream(f.type) || isLink(f.type);
    }

    protected boolean isHeaderInitPossible(FieldDef fieldDef) {
        if (fieldDef.isStatic()) {
            if (fieldDef.isDefine()) {
                // NOTE: This changed in C++17:
                // In a header file (if it is in a header file in your case)
                //class A {
                //private:
                //  inline static const string RECTANGLE = "rectangle";
                //};
                return !fieldDef.type.isArray() && !fieldDef.type.isStr();
            } else {
                return fieldDef.type.isPrimitive();
            }
        } else {
            // instance var: Init in header possible
            return true;
        }
    }

    /**
     * Writes the property declarations
     *
     * @param fieldDef         the slot def
     * @param translateContext the translation context
     */
    public void writePropertiesDecl(FieldDef fieldDef, TranslateContext translateContext) {
        final String name = fieldDef.name();
        final String slotName = toSlot(name);

        writeDocBlock(String.format("Property '%s'", name));

        // static SlotHolder<Component, int> slot1;
        String propertyType = String.format("%s<%s, %s>", CPP_SLOT_WRAPPER_TYPE, type.name, toType(fieldDef.type, translateContext));
        indent().w("static ").w(propertyType);
        w(" ");
        w(slotName);
        w(";").nl();
    }

    /**
     * Writes the property definitions (source)
     *
     * @param fieldDef         the field def
     * @param translateContext the translation context
     */
    public void writePropertiesImpl(FieldDef fieldDef, TranslateContext translateContext) {
        final String name = fieldDef.name;
        final String slotName = toSlot(name);

        //writeDocBlock(String.format("Property '%s'", name));

        /* Examples:
        SlotHolder<Component, int> Component::slot1 = SlotHolder<Component, int>("x", getX, setX, 0, 66, Slot::CONFIG);
        SlotHolder<Component, double> Component::slot2 = SlotHolder<Component, double>("dbl", dblAction, 1, 1.234, Slot::ACTION);
        SlotHolder<Component, void> Component::slot3 = SlotHolder<Component, void>("void", voidAction, 2, Slot::ACTION);
        */

        String propertyType = String.format("%s<%s, %s>", CPP_SLOT_WRAPPER_TYPE, type.name, toType(fieldDef.type, translateContext));

        indent().w(propertyType);
        w(" ");
        w(type.name);
        w(CPP_NS_SEP);
        w(slotName);
        w(" = ");
        w(propertyType);
        w("(");
        // Name
        w("\"").w(name).w("\", ");
        // Getter
        w(toGetter(name)).w(", ");
        // Setter
        if (isReadOnly(fieldDef)) {
            w(CPP_NULL_LITERAL);
        } else {
            w(toSetter(name));
        }
        // Default value
        w(", ");
        if (fieldDef.init != null) {
            expr(fieldDef.init);
        } else {
            // no explicit init: use type's default
            if ((fieldDef.rtFlags() & Slot.RT_AS_STR) > 0) {
                w("\"\"");  // special case 'buf as string' (not covered by defaultValue()
            } else {
                w(defaultValue(fieldDef.type, fieldDef.ctorArgs));
            }
        }
        // Runtime flags
        String rtFlags = getSlotRuntimeFlags(fieldDef);
        String unit = getUnit(fieldDef);
        if (rtFlags.length() > 0 || unit != null) {
            w(", ").w(rtFlags);
        }
        if (unit != null) {
            w(", ").w(UNITS_CLASS).w(CPP_NS_SEP).w(unit);
        }
        w(");").nl();
    }

    /**
     * Writes the action declarations
     *
     * @param methodDef        the method def
     * @param translateContext the translation context
     */
    public void writeActionsDecl(MethodDef methodDef, TranslateContext translateContext) {
        final String name = methodDef.name;
        final String slotName = toSlot(name);

        writeDocBlock(String.format("Action '%s'", name));

        // static SlotHolder<Component, double> slot2;
        //	static SlotHolder<Component, void> slot3;
        Type argType = ns.voidType;
        if (methodDef.params.length == 1) {
            argType = methodDef.paramTypes()[0];
        }
        String propertyType = String.format("%s<%s, %s>", CPP_SLOT_WRAPPER_TYPE, type.name, toType(argType, translateContext));
        indent().w("static ").w(propertyType);
        w(" ");
        w(slotName);
        w(";").nl();
    }

    /**
     * Writes the actions definitions (source)
     *
     * @param methodDef        the method def
     * @param translateContext the translation context
     */
    public void writeActionsImpl(MethodDef methodDef, TranslateContext translateContext) {
        final String name = methodDef.name;
        final String slotName = toSlot(name);

        writeDocBlock(String.format("Action '%s'", name));

        // SlotHolder<Component, double> Component::slot2 = SlotHolder<Component, double>("dbl", dblAction);
        //  SlotHolder<Component, void> Component::slot3 = SlotHolder<Component, void>("void", voidAction);
        Type argType = ns.voidType;
        if (methodDef.params.length == 1) {
            argType = methodDef.paramTypes()[0];
        }
        String propertyType = String.format("%s<%s, %s>", CPP_SLOT_WRAPPER_TYPE, type.name, toType(argType, translateContext));

        indent().w(propertyType);
        w(" ");
        w(type.name);
        w(CPP_NS_SEP);
        w(slotName);
        w(" = ");
        w(propertyType);
        w("(");
        w("\"").w(name).w("\", ");
        w(toGetter(name)).w(", ");
        if (isReadOnly(methodDef)) {
            w(CPP_NULL_LITERAL);
        } else {
            w(toSetter(name));
        }

        if (methodDef.params.length == 1) {
            Type paramType = methodDef.paramTypes()[0];
            w(", ").w(defaultValue(paramType, null));
        } else {
            w(", 0");
        }
        String rtFlags = getSlotRuntimeFlags(methodDef);
        if (rtFlags.length() > 0) {
            w(", ").w(rtFlags);
        }
        w(");");
        nl();
    }

    /**
     * Writes the property list declarations
     */
    public void writeSlotArrayDecl(Type type) {
        // vector<Slot*> properties = {&slot1, &slot2};
        writeDocBlock(String.format("Slot list '%s'", type.name()));

        indent();
        w("static const ").w(STD_NS).w(STD_VAR_ARRAY_TYPE).w("<").w(CPP_SLOT_TYPE).w("*> properties;").nl();
    }

    /**
     * Writes the property list impl (source)
     */
    public void writeSlotArrayImpl(TypeDef type) {
        // vector<Slot*> properties = {&slot1, &slot2};
        writeDocBlock(String.format("Slot list '%s'", type.name()));

        indent();
        w("const ").w(STD_NS).w(STD_VAR_ARRAY_TYPE).w("<").w(CPP_SLOT_TYPE).w("*> properties = {");

        List<Slot> slots = TranslationUtil.collectPropertiesAndActions(type);
        int n = 0;
        for (Slot slot : slots) {
            if (n > 0) {
                w(", ");
            }
            w("&");
            w(toType(slot.parent()));
            w(CPP_NS_SEP);
            w(toSlot(slot.name()));
            ++n;
        }
        w("};").nl();
    }

    /**
     * Writes the member access op
     */
    private void wMemberAccessOp(boolean isInline) {
        if (isInline) {
            w(".");
        } else {
            w("->");
        }
    }
    //endregion

    //region Expression handlers
    @Override
    public void block(Block block) {
        if (block != null) {
            w(" {").nl();
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

    @Override
    public void stmts(ArrayList stmts) {
        for (Object stmt : stmts) {
            //w(String.format("/* %s [%s] */", stmt.toString(), stmt.getClass().getName())).nl();
            stmt((Stmt) stmt);
        }
    }

    @Override
    public void exprStmt(Stmt.ExprStmt stmt, boolean standAlone) {
        super.exprStmt(stmt, standAlone);
    }

    @Override
    public void whileStmt(Stmt.While stmt) {
        indent().w("while (");
        expr(stmt.cond, true);
        w(") ");
        block(stmt.block);
    }

    @Override
    public void forStmt(Stmt.For stmt) {
        indent().w("for (");

        if (stmt.init != null) {
            stmt(stmt.init, false);
        }
        w(";");
        if (stmt.cond != null) {
            w(" ");
            expr(stmt.cond, true);
        }
        w(";");
        if (stmt.update != null) {
            w(" ");
            expr(stmt.update, true);
        }

        w(") ");
        block(stmt.block);
    }

    public void returnStmt(Stmt.Return stmt) {
        indent().w("return");
        if (stmt.expr != null) {
            w(" ");
            expr(stmt.expr, true);
        }
        w(";").nl();
    }

    @Override
    protected void self(Expr.This expr) {
        w("this");
    }

    @Override
    protected void callSuper(Expr.Super expr) {
        wtype(expr.type).w(CPP_NS_SEP);
    }

    @Override
    public void local(Expr.Local expr) {
        // use def b/c we might change name for C
        w(expr.def.name);
    }

    public void unary(Expr.Unary expr) {
        w(expr.op);
        expr(expr.operand);
    }

    public void increment(Expr.Unary expr) {
        if (!expr.isPostfix()) w(expr.op);
        expr(expr.operand);
        if (expr.isPostfix()) w(expr.op);
    }

    @Override
    public void ifStmt(Stmt.If stmt) {
        indent().w("if (");
        expr(stmt.cond, true);
        w(")");
        block(stmt.trueBlock);
        if (stmt.falseBlock != null) {
            indent().w(" else ");
            block(stmt.falseBlock);
        }
    }

    public void cond(Expr.Cond expr, boolean top) {
        if (!top) w("(");
        for (int i = 0; i < expr.operands.size(); ++i) {
            if (i > 0) {
                w(" ").w(expr.op).w(" ");
            }
            expr((Expr) expr.operands.get(i));
        }
        if (!top) w(")");
    }

    public void cast(Expr.Cast expr) {
        if (expr.type.isaVirtual()) {
            w("dynamic_cast<");
            wtype(expr.type);
            w("*");
        } else {
            w("static_cast<");
            wtype(expr.type);
        }
        w(">(");
        expr(expr.target);
        w(")");
    }

    @Override
    public void field(Expr.Field expr) {
        Field f = expr.field;

        boolean isThis = expr.target instanceof Expr.This;
        boolean isPtr = isThis || !f.isStatic();

        if (f.isStatic()) {
            final String qname = qname(f);
            w(qname);
        } else {
            expr(expr.target);
            w(isPtr ? "->" : ".");
            w(expr.name);
        }
    }

    @Override
    public void call(Expr.Call expr, boolean isTopLevel) {
        //super.call(expr);
        String method = expr.method.name();
        if (method.matches("set(Bool|Int|Double|Float|Byte|Short)")) {

            Expr firstArg = expr.args[0];
            if (firstArg instanceof Expr.Literal) {
                Expr.Literal literal = (Expr.Literal) expr.args[0];
                Field field = (Field) literal.value;
                w(field.name()).w(" = ");
                expr(expr.args[1], true);
            } else {
                callSimple(expr, isTopLevel);
            }
        } else {
            //noinspection StatementWithEmptyBody
            if (expr.method.isInstanceInit()) {
                // Do nothing -> done by C++
            } else if (expr.method.isStaticInit()) {
                // TODO: What to do here?
            } else {
                callSimple(expr, isTopLevel);
            }
        }
    }

    public void callSimple(Expr.Call expr, boolean isTopLevel) {
        if (expr.target != null) {
            expr(expr.target);
            if (expr.target instanceof Expr.This) {
                w("->");
            } else {
                w(".");
            }
        }

        w(expr.name);
        callArgs(expr);
    }
    @Override
    protected void interpolation(Expr.Interpolation expr) {
        for (int i = 0; i < expr.parts.size(); i++) {
            Expr partExpr = (Expr) expr.parts.get(i);
            if (i > 0) {
                w(" + ");
            }

            expr(partExpr);
        }
    }

    @Override
    protected void initVirtual(Expr.InitVirt expr) {
        w(String.format("/* Init virt %s */", expr.toString()));
    }


    @Override
    protected void initArray(Expr.InitArray expr) {
        Expr.Field f = (Expr.Field) expr.field;

        w(f.name).w(" = ").w(toType(f.type)).w("(").w(expr.length).w(")");
    }

    @Override
    public void assign(Expr.Binary expr, boolean top) {
        if (!top) w("(");
        expr(expr.lhs);
        w(" ").w(expr.op).w(" ");
        assignNarrow(expr.lhs.type, expr.rhs);
        if (!top) w(")");
    }

    @Override
    public void assignNarrow(Type lhs, Expr rhs) {
        if (lhs.isByte() || lhs.isShort()) {
            w("static_cast<").w(toType(lhs));
            if (lhs.isaVirtual()) {
                w("*");
            }
            w(">(");
            expr(rhs);
            w(")");
        } else {
            expr(rhs);
        }
    }

    public void binary(Expr.Binary expr, boolean top) {
        if (expr.id == Expr.EQ && expr.lhs.type.isFloat()) {
            w(IS_APPROXIMATELY_EQUAL);
            expr(expr.lhs, true);
            w(", ");
            expr(expr.rhs, true);
            w(")");
        } else {
            super.binary(expr, top);
        }
    }

    @Override
    public void localDefs(Block block) {

        Stmt.LocalDef[] locals = findLocalVariables(block);

        for (Stmt.LocalDef local : locals) {
            indent().w(toType(local.type, SOURCE_CONTEXT));
            if (local.type.isaVirtual() || isLink(local.type)) {
                w("*");
            }
            w(" ").w(local.name).w(";").nl();
        }
        if (locals.length > 0) nl();

    }

    public boolean isLink(Type type) {
        return (type != null && !type.isArray()) && "Link".equals(type.name());
    }

    @Override
    public void localDef(Stmt.LocalDef stmt, boolean standAlone) {
        // we only output initializers, since we declare at top of block
        if (stmt.init != null) {
            if (standAlone) indent();
            w(stmt.name).w(" = ");
            expr(stmt.init, true);
            if (standAlone) w(";").nl();
        }
    }
    //endregion

    //region Lifecycle
    @Override
    protected void initComponent(Expr.InitComp expr) {
        // nothing to do
        //w(expr);
    }

    @Override
    protected void newObject(Expr.New expr) {
        w("new " + expr.of);
    }

    @Override
    protected void deleteObject(Expr.Delete expr) {
        w("delete " + expr.target);
    }
    //endregion

    //region Literals
    @Override
    public void trueLiteral() {
        w(TRUE_LITERAL);
    }

    @Override
    public void falseLiteral() {
        w(FALSE_LITERAL);
    }

    @Override
    public void nullLiteral() {
        w(CppDefaults.CPP_NULL_LITERAL);
    }

    @Override
    public void intLiteral(int v) {
        super.intLiteral(v);
    }

    @Override
    public void longLiteral(long v) {
        super.longLiteral(v);
    }

    @Override
    public void floatLiteral(float v) {
        super.floatLiteral(v);
    }

    @Override
    public void doubleLiteral(double v) {
        w(v);
    }

    @Override
    public void stringLiteral(Expr.Literal expr) {
        super.stringLiteral(expr);
    }

    @Override
    public void timeLiteral(Expr.Literal expr) {
        w(expr.value).w(" /* TODO: Check time unit (ns) */");
    }

    @Override
    public void bufLiteral(Expr.Literal expr) {
        w("\"\\x").w(expr).w("\"");
    }

    @Override
    public void slotLiteral(Expr.Literal expr) {
        String scope = String.format("%s::%s.", type.kit.name, type.name);
        String value = expr.value.toString();

        if (value != null) {
            if (value.startsWith(scope)) {

                value = value.replace(scope, "");
            } else {
                value = value.replace(".", CPP_NS_SEP);
            }
        }

        w(value);
    }

    //endregion

    //region Doc helpers
    void writeDocBlock(SlotDef slotDef, String defaultDoc) {
        String doc = slotDef.doc != null ? slotDef.doc : (defaultDoc != null ? defaultDoc : "?");

        //writeDocBlock(doc.split("[\\\\R|\\n]"));
        if (doc.contains("automatically")) {
            w("");
        }

        doc = doc.replaceAll("[\\r|\\n]+", "\n");
        writeDocBlock(doc.split("\n"));
    }

    void writeDocBlock(String doc) {
        writeDocBlock(new String[]{doc});
    }

    void writeDocBlock(String[] doc) {
        indent().w("/**").nl();
        for (String s : doc) {
            indent().w(String.format(" * %s", s)).nl();
        }
        indent().w(" */").nl();

    }

    void lineComment(String comment) {
        indent().w(String.format("// %s", comment)).nl();
    }

    void beginRegion(String comment) {
        w("").nl();
        indent().w(String.format("//region %s", comment)).nl();
    }

    void endRegion() {
        indent().w("//endregion").nl();
    }
    //endregion

    //region TranslateContext factory methods
    public TranslateContext makeFieldContext(boolean isHeader) {
        return makeFieldContext(isHeader, 0);
    }

    public TranslateContext makeFieldContext(boolean isHeader, int modifiers) {
        return new TranslateContext(isHeader, true, modifiers);
    }

    public TranslateContext makeMethodContext(boolean isHeader) {
        return makeMethodContext(isHeader, 0);
    }

    public TranslateContext makeMethodContext(boolean isHeader, int modifiers) {
        return new TranslateContext(isHeader, false, modifiers);
    }
    //endregion


    /**
     * Gets the default value for the given type
     *
     * @param type     the type
     * @param ctorArgs optional constructor arguments
     * @return the default value (literal)
     */

    public String defaultValue(Type type, Expr[] ctorArgs) {
        if (type.isArray()) {
            return "{}";
        } else if (type.isPrimitive()) {
            if (type.isBool()) return "false";
            if (type.isByte()) return "0";
            if (type.isShort()) return "0";
            if (type.isInt()) return "0";
            if (type.isLong()) return "0L";
            if (type.isFloat()) return "0.0f";
            if (type.isDouble()) return "0.0";
            return type.signature();
        } else if (type instanceof StubType) {
            // shouldn't need this once I wrap up
            // bootstrap type resolve
            return type.name();
        } else {

            if (type.isRef()) {
                if (type.isStr()) {
                    return "\"\"";
                } else {
                    return CppDefaults.CPP_NULL_LITERAL;
                }
            } else {
                if (type.isStr()) {
                    return "\"\"";
                } else {
                    if (ctorArgs != null && ctorArgs.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(toInlineType(type)).append("(");
                        for (int i = 0; i < ctorArgs.length; i++) {
                            Expr ctorArg = ctorArgs[i];
                            if (i > 0) {
                                sb.append(", ");
                            }
                            sb.append(ctorArg.toString());
                        }
                        sb.append(")");
                        return sb.toString();
                    } else {
                        return toInlineType(type) + "()";
                    }
                }
            }
        }
    }

    /**
     * Gets the type name with modifiers and array suffixes.
     * The modifiers are added according to the context.
     * <p>
     * NOTE: This method does NOT append pointer or reference modifiers
     *
     * @param type             the type
     * @param translateContext the translation context
     * @return the simple type name
     */
    public String toType(Type type, TranslateContext translateContext) {
        StringBuilder stringBuilder = new StringBuilder();

        final boolean isConst = translateContext.isConst();
        final boolean withNamespace = translateContext.isHeader() || CppDefaults.getCppOptions().requiresNamespaceInSource();

        // Modifiers
        if (!translateContext.isMethod()) {
            if (translateContext.isHeader() && translateContext.isStatic()) {
                stringBuilder.append("static ");
            }
        }

        // Add const for member variables
        if (isConst && !translateContext.isMethod()) {
            stringBuilder.append("const ");
        }

        // Assemble type
        final String plainType = addOrRemoveStdNamespace(toType(type), withNamespace);
        if (type.isArray()) {
            stringBuilder.append(CppDefaults.getStdVarArrayType(withNamespace));
            stringBuilder.append("<");
            stringBuilder.append(plainType);
        } else {
            if (translateContext.bufferAsString()) {
                stringBuilder.append(CppDefaults.getStringType(withNamespace));
            } else {
                stringBuilder.append(plainType);
            }
        }
        if (type.isArray()) {
            stringBuilder.append(">");
        }

        return stringBuilder.toString();
    }

    /**
     * Gets the simple type name without any modifiers
     *
     * @param type the type
     * @return the simple type name
     */
    @Override
    public String toType(Type type) {
        if (type == null) {
            return "void";
        }

        if (isCppType(type.qname())) {
            return toCppType(type.qname());
        }

        return toInlineType(type);
    }

    /**
     * Gets the type when declared as inline (without any modifiers)
     *
     * @param type the type
     * @return the type name
     */
    @Override
    public String toInlineType(Type type) {
        if (type.isArray()) {
            return toType(type.arrayOf());
        } else if (type.isPrimitive()) {
            if (type instanceof EnumDef) {
                return qnameInclude(type);
            }

            if (type.isBool()) return "bool";
            if (type.isByte()) return "uint8_t";
            if (type.isShort()) return "uint16_t";
            if (type.isInt()) return "int32_t";
            if (type.isLong()) return "int64_t";
            return type.signature();
        } else if (type instanceof StubType) {
            // shouldn't need this once I wrap up
            // bootstrap type resolve
            return type.name();
        } else {
            return qname(type);
        }
    }
    //endregion

    public String qnameInclude(Type type) {
        final String name = type.name();
        if (this.type.kit.name.equals(type.kit().name())) {
            return name;
        } else {
            return "../" + type.kit().name() + "/" + name;
        }
    }

    public String qname(Type type) {
        final String name = type.name();
        if (this.type.kit.name.equals(type.kit().name())) {
            return name;
        } else {
            return type.kit().name() + CPP_NS_SEP + type.name();
        }
    }

    public String qname(Slot s) {
        if (s.isStatic()) {
            return qname(s.parent()) + CPP_NS_SEP + s.name();
        } else {
            return s.name();
        }
    }

    //region Translate context

    /**
     * Represents the context for the translation to set or omit the right keywords/literals
     * when generating type declarations and so on.
     */
    public static class TranslateContext {
        private boolean isHeader;
        private boolean isField;
        private boolean asStr;
        private int modifiers = 0;

        TranslateContext(boolean isHeader) {
            this(isHeader, true, 0);
        }

        TranslateContext(boolean isHeader, boolean isField, int modifierMask) {
            this.isHeader = isHeader;
            this.isField = isField;
            setModifiers(modifierMask);
        }

        public int getModifiers() {
            return modifiers;
        }

        private void setModifiers(int modifierMask) {
            modifiers = modifierMask;
        }

        public boolean isSource() {
            return !isHeader();
        }

        public boolean isHeader() {
            return isHeader;
        }

        public boolean isField() {
            return isField;
        }

        public boolean isMethod() {
            return !isField();
        }

        public boolean isStatic() {
            return (modifiers & (Slot.STATIC | Slot.NATIVE)) > 0;
        }

        public boolean isConst() {
            return (modifiers & (Slot.CONST | Slot.DEFINE)) > 0;
        }

        public boolean isInline() {
            return (modifiers & (Slot.INLINE)) > 0;
        }

        public boolean isProperty() {
            return (modifiers & (Slot.PROPERTY)) > 0;
        }

        public boolean isVirtual() {
            return (modifiers & (Slot.ABSTRACT | Slot.VIRTUAL)) > 0;
        }

        public boolean isOverride() {
            return (modifiers & (Slot.OVERRIDE)) > 0;
        }

        /**
         * True, if @asStr facet is set
         *
         * @return true, if @asStr facet is set
         */
        public boolean bufferAsString() {
            return asStr;
        }

        /**
         * Initializes the context with the given method, e. g. sets the modifier mask.
         *
         * @param methodDef the method definition to initialize from
         */
        public void initFromMethod(MethodDef methodDef) {
            isField = false;
            setModifiers(methodDef.flags);
        }

        /**
         * Initializes the context with the given field, e. g. sets the modifier mask and the facets.
         *
         * @param fieldDef the field definition to initialize from
         */
        public void initFromField(FieldDef fieldDef) {
            isField = true;
            setModifiers(fieldDef.flags);

            asStr = isAsStr(fieldDef);
        }

        @Override
        public String toString() {
            return "TranslateContext{" +
                    "isHeader=" + isHeader +
                    ", isField=" + isField +
                    ", modifiers=" +  TypeUtil.flagsToString(modifiers) +
                    '}';
        }

    }

    //endregion

}
