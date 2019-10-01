/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedona.Facets;
import sedona.util.TextUtil;
import sedonac.ast.*;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.namespace.TypeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static sedonac.ast.Expr.*;
import static sedonac.namespace.TypeUtil.isaVirtual;
import static sedonac.translate.CppDefaults.SYS_INCLUDE_PATH;

@SuppressWarnings("unchecked")
public class TranslationUtil {
    public static final FacetDef[] NO_FACETS = {};
    public static final ParamDef[] NO_PARAMETERS = {};

    public static final String SEDONA_SYS = "sys";

    private static final Stmt.LocalDef[] EMPTY_LOCAL_DEFS = new Stmt.LocalDef[0];
    public static final String SLOT_PREFIX = "Slot::";

    //region Lambda interfaces
    /**
     * Interface for filtering fields
     */
    public interface IFieldSelector {
        boolean acceptField(FieldDef fieldDef);
    }

    /**
     * Interface for writing fields
     */
    public interface IFieldDefWriter {
        void writeField(FieldDef fieldDef, CppTranslator.TranslateContext context);
    }

    /**
     * Interface for filtering methods
     */
    public interface IMethodSelector {
        boolean acceptMethod(MethodDef methodDef);
    }

    /**
     * Interface for writing methods
     */
    public interface IMethodDefWriter {
        void writeMethod(MethodDef methodDef, CppTranslator.TranslateContext context);
    }

    /**
     * Interface for filtering slots
     */
    public interface ISlotSelector {
        boolean acceptsSlot(Slot slot);
    }

    /**
     * Interface for writing slots
     */
    public interface ISlotWriter {
        void writeSlot(Slot slot, CppTranslator.TranslateContext context);
    }

    public interface IFieldWriter {
        void writeField(sedonac.namespace.Field field, CppTranslator.TranslateContext context);
    }

    public interface IMethodWriter {
        void writeMethod(sedonac.namespace.Method method, CppTranslator.TranslateContext context);
    }

    /**
     * Interface for filtering types.
     */
    public interface ITypeSelector {
        boolean acceptType(Type type);
    }
    //endregion

    //region C++ support
    private static HashMap<String, String> sedonaBaseClasses;
    private static HashMap<String, String> sedonaToCppTypes;
    private static HashMap<String, String> sedonaToCppLiterals;

    static {
        sedonaToCppTypes = new HashMap<>();
        //sedonaToCppTypes.put("sys::Buf", CppDefaults.STRING_TYPE);
        sedonaToCppTypes.put("sys::Str", CppDefaults.getStringType());
        /*
        sedonaToCppTypes.put("sys::InStream", CppDefaults.STD_NS + "istream");
        sedonaToCppTypes.put("sys::OutStream", CppDefaults.STD_NS + "ostream");
        sedonaToCppTypes.put("sys::BufInStream", CppDefaults.STD_NS + "istringstream");
        sedonaToCppTypes.put("sys::BufOutStream", CppDefaults.STD_NS + "ostringstream");
        sedonaToCppTypes.put("sys::StdOutStream", CppDefaults.STD_NS + "ostringstream");
        sedonaToCppTypes.put("sys::NullInStream", CppDefaults.STD_NS + "istream");
        sedonaToCppTypes.put("sys::NullOutStream", CppDefaults.STD_NS + "ostream");
        sedonaToCppTypes.put("sys::FileInStream", CppDefaults.STD_NS + "ifstream");
        sedonaToCppTypes.put("sys::FileOutStream", CppDefaults.STD_NS + "ofstream");
        */

        sedonaToCppTypes.put("sys::File", CppDefaults.STD_NS + "fstream");
        sedonaToCppTypes.put("sys::FileStore", CppDefaults.STD_NS + "fstream");
        sedonaToCppTypes.put("sys::MemoryFile", CppDefaults.STD_NS + "fstream");
        sedonaToCppTypes.put("sys::MemoryFileStore", CppDefaults.STD_NS + "fstream");

        // This eases up compile, but should be removed later
        sedonaToCppTypes.put("sys::Type", CppDefaults.getStringType());

        sedonaBaseClasses = new HashMap<>();
        sedonaBaseClasses.put("sys::Buf", CppDefaults.getStdVarArrayType() + "<uint8_t>");

        // FIXME: Remove fields in FilterRedundantTypes
        sedonaToCppTypes.put("sys::Obj", null);
        sedonaToCppTypes.put("sys::Units", null);
        sedonaToCppTypes.put("sys::Virtual", null);

        sedonaToCppLiterals = new HashMap<>();
        sedonaToCppLiterals.put("null", CppDefaults.CPP_NULL_LITERAL);
        sedonaToCppLiterals.put("sizeof", "sizeOf");
        sedonaToCppLiterals.put("or", "opRead");
        sedonaToCppLiterals.put("ow", "opWrite");
        sedonaToCppLiterals.put("oi", "opInvoke");
        sedonaToCppLiterals.put("ar", "admRead");
        sedonaToCppLiterals.put("aw", "admWrite");
        sedonaToCppLiterals.put("ai", "admInvoke");
    }

    /**
     * Gets the C++ type for the given Sedona type
     *
     * @param typeName the type to replace
     * @return the replaced or the original type
     */
    public static boolean isCppType(String typeName) {
        return sedonaToCppTypes.containsKey(typeName);
    }


    /**
     * Gets the C++ type for the given Sedona type
     *
     * @param typeName the type to replace
     * @return the replaced or the original type
     */
    public static String toCppType(String typeName) {
        return sedonaToCppTypes.getOrDefault(typeName, typeName);
    }

    /**
     * Gets the C++ base type/class for the given Sedona type
     *
     * @param typeName the type to check
     * @return the replaced or the original type
     */
    static String toCppBaseType(String typeName, String defaultBaseType) {
        return sedonaBaseClasses.getOrDefault(typeName, defaultBaseType);
    }

    /**
     * Checks if given class has an overridden base type.
     *
     * @param typeName the type to check
     * @return true, if given shall use a different base type
     */
    static boolean hasCppBaseType(String typeName) {
        return sedonaBaseClasses.containsKey(typeName);
    }

    /**
     * Gets the C++ literal for the given Sedona literal
     *
     * @param literal the literal to replace
     * @return the replaced or the original literal
     */
    static String toCppLiteral(String literal) {
        return sedonaToCppLiterals.getOrDefault(literal, literal);
    }
    //endregion

    /**
     * Creates a sys-based include reference.
     * @param type the type (the class using the header file)
     * @param headerFileName the name of the header file without extension and path, e. g. "Property"
     * @return the include path, e. g. "../sys/Property.h" or "Property.h"
     */
    public static String makeSysInclude(TypeDef type, String headerFileName) {
        String helperInclude = SYS_INCLUDE_PATH + headerFileName + CppDefaults.EXT_SEP  + CppDefaults.getHeaderExtension();
        if ("sys".equals(type.kit.name) && !TypeUtil.isaTest(type)) {
            helperInclude = headerFileName + CppDefaults.EXT_SEP + CppDefaults.getHeaderExtension();
        }
        return helperInclude;
    }

    /**
     * Extracts all local variables in the given block
     *
     * @param block the block to examine
     * @return array containing all local variables
     */
    public static Stmt.LocalDef[] findLocalVariables(final Block block) {
        if (block == null) {
            return EMPTY_LOCAL_DEFS;
        }

        ArrayList list = new ArrayList();
        HashMap dups = new HashMap();

        for (int i = 0; i < block.stmts.size(); ++i) {
            Stmt stmt = (Stmt) block.stmts.get(i);

            // check if statement is or contains local def
            Stmt.LocalDef def = null;
            if (stmt instanceof Stmt.LocalDef) {
                def = (Stmt.LocalDef) stmt;
            } else if (stmt instanceof Stmt.For) {
                Stmt.For forStmt = (Stmt.For) stmt;
                if (forStmt.init instanceof Stmt.LocalDef)
                    def = (Stmt.LocalDef) forStmt.init;
            }
            if (def == null) continue;

            // if local def add it to our list
            for (int n = 1; dups.containsKey(def.name); ++n)
                def.name = def.name + n;
            list.add(def);
            dups.put(def.name, def);
        }

        return (Stmt.LocalDef[]) list.toArray(new Stmt.LocalDef[list.size()]);
    }

    /**
     * Finds all dependent types according to the types methods and fields.
     *
     * @param parentType the parent type to get the dependencies for
     * @return the array containing all used types
     */
    static Type[] findDependentTypes(TypeDef parentType) {
        return findDependentTypes(parentType, null, false);
    }

    /**
     * Finds all dependent types according to the types methods and fields.
     *
     * @param parentType the parent type to get the dependencies for
     * @param withImpl   if true, code blocks are also scanned for types
     * @return the array containing all used types
     */
    static Type[] findDependentTypes(TypeDef parentType, boolean withImpl) {
        return findDependentTypes(parentType, null, withImpl);
    }

    /**
     * Finds all dependent types according to the types methods and fields.
     *
     * @param parentType the parent type to get the dependencies for
     * @param withImpl   if true, code blocks are also scanned for types
     * @param selector   lambda expression to filter types. If the expression
     *                   returns false, the type will not be added to the list
     * @return the array containing all used types
     */
    static Type[] findDependentTypes(TypeDef parentType, ITypeSelector selector, boolean withImpl) {
        HashMap<String, Type> acc = new HashMap<>();
        SlotDef[] slotDefs = parentType.slotDefs();
        for (SlotDef slot : slotDefs) {
            if (slot.isField()) {
                findDependentTypes(acc, parentType, (FieldDef) slot, selector);
            } else {
                findDependentTypes(acc, parentType, (MethodDef) slot, selector, withImpl);
            }
        }

        if (parentType.base != null) {
            addTypeDependency(acc, parentType, parentType.base, selector);
        }

        if (withImpl) {
            addTypeDependency(acc, parentType, parentType, selector);
        }

        return acc.values().toArray(new Type[acc.size()]);
    }

    /**
     * Collect field dependencies
     *
     * @param acc      the list
     * @param parent   the parent type
     * @param f        the field
     * @param selector the lambda expression to filter types
     */
    private static void findDependentTypes(HashMap acc, TypeDef parent, FieldDef f, ITypeSelector selector) {
        addTypeDependency(acc, parent, f.type, selector);
    }

    private static void findDependentTypes(HashMap acc, TypeDef parent, MethodDef m, ITypeSelector selector, boolean withImpl) {
        addTypeDependency(acc, parent, m.ret, selector);
        for (int i = 0; i < m.params.length; ++i) {
            addTypeDependency(acc, parent, m.params[i].type, selector);
        }

        if (m.code != null && withImpl) {
            Stmt.LocalDef[] localDefs = findLocalVariables(m.code);
            for (Stmt.LocalDef localDef : localDefs) {
                addTypeDependency(acc, parent, localDef.type, selector);
            }

            findDependentTypes(acc, m.code);
        }
    }

    private static void addTypeDependency(HashMap acc, TypeDef parent, Type type, ITypeSelector selector) {
        if (type.qname().equals(parent.qname)) return;

        if (selector == null || selector.acceptType(type)) {
            addTypeDependency(acc, type);
        }
    }


    /**
     * Scan given block for required types and recurse to nested blocks, if necessary.
     *
     * @param acc   the map containing the current type list
     * @param block the block to scan
     */
    private static void findDependentTypes(HashMap acc, Block block) {
        if (block == null || block.stmts == null) return;
        for (int i = 0; i < block.stmts.size(); ++i) {
            Stmt stmt = (Stmt) block.stmts.get(i);

            if (stmt instanceof Stmt.LocalDef) {
                findDependentTypes(acc, ((Stmt.LocalDef) stmt).init);
            } else if (stmt instanceof Stmt.ExprStmt) {
                findDependentTypes(acc, ((Stmt.ExprStmt) stmt).expr);
            } else if (stmt instanceof Stmt.If) {
                Stmt.If ifStmt = (Stmt.If) stmt;
                findDependentTypes(acc, ifStmt.falseBlock);
                findDependentTypes(acc, ifStmt.trueBlock);
            } else if (stmt instanceof Stmt.For) {
                Stmt.For forStmt = (Stmt.For) stmt;
                findDependentTypes(acc, forStmt.block);
            } else if (stmt instanceof Stmt.Foreach) {
                Stmt.Foreach forStmt = (Stmt.Foreach) stmt;
                findDependentTypes(acc, forStmt.block);
            } else if (stmt instanceof Stmt.While) {
                Stmt.While whileStmt = (Stmt.While) stmt;
                findDependentTypes(acc, whileStmt.block);
            } else if (stmt instanceof Stmt.DoWhile) {
                Stmt.DoWhile whileStmt = (Stmt.DoWhile) stmt;
                findDependentTypes(acc, whileStmt.block);
            } else if (stmt instanceof Stmt.Switch) {
                Stmt.Switch switchStmt = (Stmt.Switch) stmt;
                findDependentTypes(acc, switchStmt.cond);
                for (int j = 0; j < switchStmt.cases.length; j++) {
                    Stmt.Case theCase = switchStmt.cases[j];
                    findDependentTypes(acc, theCase.block);
                }
            } else if (stmt instanceof Stmt.Return) {
                Stmt.Return returnStmt = (Stmt.Return) stmt;
                findDependentTypes(acc, returnStmt.expr);
            } else if (stmt instanceof Stmt.Break) {
            } else if (stmt instanceof Stmt.Goto) {
            } else if (stmt instanceof Stmt.Continue) {
            } else if (stmt instanceof Stmt.Assert) {
            } else {
                throw new IllegalArgumentException("Not implemented " + stmt);
            }
        }
    }

    private static void findDependentTypes(HashMap acc, Expr expr) {
        if (expr == null) return;

        Type type = null;
        if (expr instanceof Call) {
            final Call call = (Call) expr;
            addTypeDependency(acc, type);
            // Target component
            if (call.target != null) {
                addTypeDependency(acc, call.target.type);
            }
            // return type
            addTypeDependency(acc, call.method.returnType());
            // parameter types
            for (int i = 0; i < call.method.paramTypes().length; i++) {
                Type paramType = call.method.paramTypes()[i];
                addTypeDependency(acc, paramType);
            }
            // argument expressions
            for (int i = 0; i < call.args.length; i++) {
                findDependentTypes(acc, call.args[i]);
            }

        }

        if (expr instanceof Binary) {
            Binary binary = (Binary) expr;
            addTypeDependency(acc, expr.type);
            findDependentTypes(acc, binary.rhs);
            findDependentTypes(acc, binary.lhs);
        }

        if (expr instanceof Cond) {
            Cond cond = (Cond) expr;
            for (int i = 0; i < cond.operands.size(); i++) {
                Expr operand = (Expr) cond.operands.get(i);
                findDependentTypes(acc, operand);
            }
        }

    }

    private static void addTypeDependency(HashMap acc, Type type) {
        if (type == null) return;

        if (type.isArray()) {
            addTypeDependency(acc, type.arrayOf());
        } else {
            if (!type.isPrimitive() && !TranslationUtil.isCppType(type.qname())) {
                acc.put(type.qname(), type);
            }
        }
    }

    /**
     * Checks if given facet node is marked as 'read only' (facet 'readonly' is present)
     *
     * @param facetsNode the node to check
     * @return true, if node is read-only
     */
    public static boolean isReadOnly(FacetsNode facetsNode) {
        Facets facets = facetsNode.facets();
        return facets.getb("readonly");
    }

    /**
     * Checks if given facet node is marked as 'persistent' (facet 'config' is present)
     *
     * @param facetsNode the node to check
     * @return true, if node is persistent
     */
    public static boolean isPersistent(FacetsNode facetsNode) {
        Facets facets = facetsNode.facets();
        return facets.getb("config");
    }

    /**
     * Gets the unit id of the property (facet 'unit' is present)
     *
     * @param facetsNode the node to check
     * @return the unit id or <code>null</code>, if unit facet is not present.
     */
    public static String getUnit(FacetsNode facetsNode) {
        Facets facets = facetsNode.facets();
        String unit = facets.gets("unit");
        if (unit != null) {
            return unit.replace(".", CppDefaults.CPP_NS_SEP);
        } else {
            return null;
        }
    }

    /**
     * Checks if given facet node is marked as 'as string' (facet 'asStr' is present)
     *
     * @param facetsNode the node to check
     * @return true, if node is a buffer which should be interpreted as string
     */
    public static boolean isAsStr(FacetsNode facetsNode) {
        Facets facets = facetsNode.facets();
        return facets.getb("asStr");
    }

    /**
     * Gets a verbose representation of the slot's runtime flags.
     * @param slot the slot
     * @return the runtime flags, e. g. 'Slot::CONFIG|Slot::AS_STR'
     */
    public static String getSlotRuntimeFlags(Slot slot) {
        StringBuilder sb = new StringBuilder();

        final int rtFlags = slot.rtFlags();

        if ((rtFlags & Slot.RT_ACTION) > 0) {
            sb.append(SLOT_PREFIX).append("ACTION");
        }
        if ((rtFlags & Slot.RT_CONFIG) > 0) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(SLOT_PREFIX).append("CONFIG");
        }

        if ((rtFlags & Slot.RT_AS_STR) > 0) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(SLOT_PREFIX).append("AS_STR");
        }

        return sb.toString();
    }

    /**
     * Checks if given method is a test method.
     * @param m the method def to check
     * @return true, if method def is a test method
     */
    public static boolean isTestMethod(MethodDef m) {
        return m.name.startsWith(CppDefaults.TEST_METHOD_NAME);
    }

    /**
     * Checks if given facet node is marked as 'enum' (facet 'range' is present), e. g.
     * <code>@range=logLevels</code>
     *
     * @param facetsNode the node to check
     * @return the enum reference
     */
    public static String getEnumValue(FacetsNode facetsNode) {
        FacetDef[] facets = facetsNode.facetDefs;
        for (int i = 0; i < facets.length; i++) {
            FacetDef facet = facets[i];
            if ("range".equals(facet.name)) {
                if (facet.val instanceof Expr.Field) {
                    return ((Expr.Field) facet.val).name;
                } else {
                    return facet.val.toString();
                }
            }
        }
        return null;
    }

    /**
     * Checks if given field can be an enum
     *
     * @param fieldDef the field to check
     * @return true, if field can be an enum value (defined string array)
     */
    public static boolean canBeEnum(FieldDef fieldDef) {
        return fieldDef != null && fieldDef.type.isStr() && fieldDef.isDefine() && isEnumInit(fieldDef.init);
    }

    public static boolean isEnumInit(Expr expr) {
        if (expr == null) return false;

        if (expr instanceof Expr.Field) {
            Expr.Field field = (Expr.Field) expr;
        }

        if (expr instanceof Expr.Literal) {
            Expr.Literal field = (Expr.Literal) expr;
            if (expr.id == STR_LITERAL) {
                if (field.value instanceof String) {
                    return ((String) field.value).contains(",");
                }
            }
        }

        return false;
    }

    /**
     * Checks if given field is a public member, but not a property.
     * @param field the field to check
     * @return true, if field is public, but not a property
     */
    public static boolean isPublicField(sedonac.namespace.Field field) {
        return (field.isPublic() ||
                field.isProperty()) &&
                !field.isStatic() &&
                !field.isInline() &&
                !field.isDefine()  && // constant -> no getter required
                field.parent().isaComponent() &&    // omit virtuals and helper classes
                !TypeUtil.isaTest(field.parent()) && // omit tests
                !isCppType(field.type().qname());
    }

    public static String toGetter(String fieldName) {
        return "get" + TextUtil.capitalize(fieldName);
    }

    public static String toSetter(String fieldName) {
        return "set" + TextUtil.capitalize(fieldName);
    }

    public static String toSlot(String fieldName) {
        return "slot" + TextUtil.capitalize(fieldName);
    }

    /**
     * Collects all properties from the type hierarchy. The first slot of the root type
     * becomes the first slot in the list and the last slot of the given type will be the last one.
     *
     * Convenience as <code>collectSlots(type, Slot::isProperty)</code>
     * @param type the type to search the slots for
     * @return the list containing all slots in the type hierarchy or an empty list, if given type is
     * not a component.
     */
    public static List<Slot> collectProperties(Type type) {
        return collectSlots(type, Slot::isProperty);
    }

    /**
     * Collects all actions from the type hierarchy. The first slot of the root type
     * becomes the first slot in the list and the last slot of the given type will be the last one.
     *
     * Convenience as <code>collectSlots(type, Slot::isAction)</code>
     * @param type the type to search the slots for
     * @return the list containing all slots in the type hierarchy or an empty list, if given type is
     * not a component.
     */
    public static List<Slot> collectActions(Type type) {
        return collectSlots(type, Slot::isAction);
    }

    /**
     * Collects all properties and actions from the type hierarchy. The first slot of the root type
     * becomes the first slot in the list and the last slot of the given type will be the last one.
     *
     * Convenience as <code>collectSlots(type, s -> s.isProperty() || s.isAction())</code>
     * @param type the type to search the slots for
     * @return the list containing all slots in the type hierarchy or an empty list, if given type is
     * not a component.
     */
    public static List<Slot> collectPropertiesAndActions(Type type) {
        return collectSlots(type, s -> s.isProperty() || s.isAction());
    }

    /**
     * Collects all slots matching the given selector from the type hierarchy. The first slot of the root type
     * becomes the first slot in the list and the last slot of the given type will be the last one.
     * @param type the type to search the slots for
     * @param slotSelector the slot selector
     * @return the list containing all slots in the type hierarchy or an empty list, if given type is
     * not a component.
     */
    public static List<Slot> collectSlots(Type type, ISlotSelector slotSelector) {
        List<Slot> slots = new ArrayList<>();

        if (!type.isaComponent()) {
            return slots;
        }

        Type curType = type;
        do {
            for (int i = 0, n = 0; i < curType.slots().length; i++) {
                Slot slot = curType.slots()[i];

                if (slotSelector.acceptsSlot(slot)) {
                    slots.add(n++, slot);
                }
            }
            curType = curType.base();
        } while (curType != null && curType.isaComponent());

        return slots;
    }

    /**
     * Checks if given type has at least one virtual method.
     * @param t the type to check
     * @return true, if type has one or more virtual methods
     */
    public static boolean hasVirtualMethods(Type t) {
        return slotExists(t, s -> s.isVirtual() || s.isAbstract());
    }

    /**
     * Checks if given type has at least one virtual method.
     * @param t the type to check
     * @return true, if type has one or more virtual methods
     */
    public static int countSlots(Type t, TranslationUtil.ISlotSelector slotSelector) {
        int count = 0;
        for (Slot slot : t.slots()) {
            if (slotSelector.acceptsSlot(slot)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if given type has at least one slot matching the given slot selector.
     * @param t the type to check
     * @return true, if type has one slot matching the selector
     */
    public static boolean slotExists(Type t, TranslationUtil.ISlotSelector slotSelector) {
        for (Slot slot : t.slots()) {
            if (slotSelector.acceptsSlot(slot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the (first) slot matching the given slot selector.
     * @param t the type to search the slot in
     * @return the matching slot or null, if no such slot exists
     */
    public static Slot firstSlot(Type t, TranslationUtil.ISlotSelector slotSelector) {
        for (Slot slot : t.slots()) {
            if (slotSelector.acceptsSlot(slot)) {
                return slot;
            }
        }
        return null;
    }
}
