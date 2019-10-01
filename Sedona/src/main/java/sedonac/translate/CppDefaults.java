/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedonac.namespace.Type;

@SuppressWarnings("WeakerAccess")
public class CppDefaults {
    static final String IS_APPROXIMATELY_EQUAL = "::isApproximatelyEqual(";

    /**
     * Namespace separator for C++
     */
    static final String CPP_NS_SEP = "::";

    /**
     * Standard namespace name
     */
    private static final String STD_NS_NAME = "std";

    /**
     * Standard namespace
     */
    public static final String STD_NS = STD_NS_NAME + CPP_NS_SEP;


    public static final String STD_VAR_ARRAY_TYPE =  "vector";
    public static final String STRING_TYPE = "string";

    public static final String CPP_NULL_LITERAL = "nullptr";

    public static final String SLOT_CLASS = "SlotWrapper";
    public static final String TYPE_CLASS = "TypeWrapper";

    public static final String DEFAULT_HEADER_EXTENSION = "cc";
    public static final String DEFAULT_SOURCE_EXTENSION = "h";
    public static final String DEFAULT_OBJECT_EXTENSION = "o";
    public static final String CPP_STATIC_LIB_EXTENSION = "a";

    private static final String[] STD_TYPES = new String[]{STD_VAR_ARRAY_TYPE, STRING_TYPE};
    public static final String SYS_APPROX = "FPHelper";

    public static final String SYS_KIT = "sys";
    public static final String SYS_INCLUDE_PATH = "../" + SYS_KIT + "/";
    public static final String EXT_SEP = ".";

    public static final String TEST_METHOD_NAME = "test";
    public static final String UNITS_CLASS = SYS_KIT + CPP_NS_SEP + "Units";
    public static final String DEFAULT_UNIT = UNITS_CLASS + CPP_NS_SEP + "none";
    public static final String CPP_SLOT_TYPE = SYS_KIT + CPP_NS_SEP + "Slot";
    public static final String CPP_SLOT_WRAPPER_TYPE = SYS_KIT + CPP_NS_SEP + SLOT_CLASS;
    public static final String COPY_RIGHT = "(C) My Company 2019";

    //region Options
    private static CppOptions cppOptions;

    public static CppOptions getCppOptions() {
        return cppOptions;
    }

    public static void setCppOptions(CppOptions cppOptions) {
        CppDefaults.cppOptions = cppOptions;
    }
    //endregion

    /**
     * Gets the default array type with variable size (e. g. 'std::vector').
     * The namespace is added according to the C++ options ({@link CppOptions}.
     * @return the array type
     */
    static String getStdVarArrayType() {
        return getStdVarArrayType(cppOptions.requiresNamespaceInSource());
    }

    /**
     * Adds the standard namespace to (or removes it from) the type name.
     * @param typeName the type to check
     * @param withNameSpace if true the namespace is added, if necessary. Otherwise it will be removed
     * @return the modified type name
     */
    static String addOrRemoveStdNamespace(String typeName, boolean withNameSpace) {
        if (typeName == null) {
            return null;
        }

        if (withNameSpace) {
            for (String stdType : STD_TYPES) {
                if (stdType.equals(typeName)) {
                    return STD_NS + typeName;
                }
            }
        } else {
            if (typeName.startsWith(STD_NS)) {
                return typeName.substring(STD_NS.length());
            }
        }

        // return 'as-is'
        return typeName;
    }

    /**
     * Gets the default array type with variable size (e. g. 'std::vector').
     * @param withNamespace prepend the namespace.
     * @return the array type
     */
    public static String getStdVarArrayType(boolean withNamespace) {
        return withNamespace ?  STD_NS + STD_VAR_ARRAY_TYPE : STD_VAR_ARRAY_TYPE;
    }

    /**
     * Gets the default string type  (e. g. 'std::string').
     * The namespace is added according to the C++ options ({@link CppOptions}.
     * @return the string type
     */
    public static String getStringType() {
        return getStringType(cppOptions.requiresNamespaceInSource());
    }

    /**
     * Gets the default string type (e. g. 'std::string').
     * @param withNamespace prepend the namespace.
     * @return the string type
     */
    public static String getStringType(boolean withNamespace) {
        return withNamespace ?  STD_NS + STRING_TYPE : STRING_TYPE;
    }

    /**
     * Gets the file extension for C++ source files.
     * @return the file extension
     */
    static String getSourceExtension() {
        return cppOptions.getSourceExt();
    }

    /**
     * Gets the file extension for C++ header files.
     * @return the file extension
     */
    static String getHeaderExtension() {
        return cppOptions.getHeaderExt();
    }

    /**
     * Gets the file extension for C++ object files.
     * @return the file extension
     */
    static String getObjectFileExtension() {
        return cppOptions.getObjectExt();
    }

    private CppDefaults() {

    }

    /**
     * Checks if given type is a C++ file stream type.
     *
     * @param type the type to check.
     * @return true, if type is a file stream type
     */
    static boolean isStdFileStream(Type type) {
        return typeContains(type, "fstream");
    }

    /**
     * Checks if given type is a C++ file stream type.
     *
     * @param type the type to check.
     * @return true, if type is a file stream type
     */
    static boolean isStream(Type type) {
        return typeContains(type, "stream");
    }

    /**
     * Checks if given type contains the given string (case insensitive).
     *
     * @param type the type to check.
     * @param text the text to search for
     * @return true, if type name contains the given text
     */
    static boolean typeContains(Type type, String text) {
        String name = TranslationUtil.toCppType(type.qname());
        return name != null && name.toLowerCase().contains(text);
    }


}
