package com.drumge.easy.plugin.utils

import com.android.annotations.NonNull
import com.android.annotations.Nullable

class TypeUtils {

    static class TypePackage {
        final String packageName
        final String className

        TypePackage(@NonNull String packageName, @NonNull String className) {
            this.packageName = packageName
            this.className = className
        }

        @Override
        boolean equals(Object o) {
            if (this == o) {
                return true
            } else if (o instanceof TypePackage) {
                TypePackage type = (TypePackage)o
                return packageName == type.packageName && className == type.className
            }
            return false
        }

        boolean equalsContent(String type) {
            return type == toString()
        }

        @Override
        String toString() {
            return "${packageName}.${className}"
        }
    }

    public static final String VOID = "void"
    public static final String BOOLEAN = "boolean"
    public static final String BYTE = "byte"
    public static final String SHORT = "short"
    public static final String INT = "int"
    public static final String LONG = "long"
    public static final String CHAR = "char"
    public static final String FLOAT = "float"
    public static final String DOUBLE = "double"

    public static final TypePackage OBJECT = new TypePackage("java.lang", "Object")

    private static final TypePackage BOXED_VOID = new TypePackage("java.lang", "Void")
    private static final TypePackage BOXED_BOOLEAN = new TypePackage("java.lang", "Boolean")
    private static final TypePackage BOXED_BYTE = new TypePackage("java.lang", "Byte")
    private static final TypePackage BOXED_SHORT = new TypePackage("java.lang", "Short")
    private static final TypePackage BOXED_INT = new TypePackage("java.lang", "Integer")
    private static final TypePackage BOXED_LONG = new TypePackage("java.lang", "Long")
    private static final TypePackage BOXED_CHAR = new TypePackage("java.lang", "Character")
    private static final TypePackage BOXED_FLOAT = new TypePackage("java.lang", "Float")
    private static final TypePackage BOXED_DOUBLE = new TypePackage("java.lang", "Double")

    static boolean isUnbox(String type) {
        if (type == VOID) {
            return true
        } else if (type == BOOLEAN) {
            return true
        } else if (type == BYTE) {
            return true
        } else if (type == SHORT) {
            return true
        } else if (type == INT) {
            return true
        } else if (type == LONG) {
            return true
        } else if (type == CHAR) {
            return true
        } else if (type == FLOAT) {
            return true
        } else if (type == DOUBLE) {
            return true
        } else {
            return false
        }
    }

    @Nullable
    static String box(String type) {
        if (type == VOID) {
            return BOXED_VOID.toString()
        } else if (type == BOOLEAN) {
            return BOXED_BOOLEAN.toString()
        } else if (type == BYTE) {
            return BOXED_BYTE.toString()
        } else if (type == SHORT) {
            return BOXED_SHORT.toString()
        } else if (type == INT) {
            return BOXED_INT.toString()
        } else if (type == LONG) {
            return BOXED_LONG.toString()
        } else if (type == CHAR) {
            return BOXED_CHAR.toString()
        } else if (type == FLOAT) {
            return BOXED_FLOAT.toString()
        } else if (type == DOUBLE) {
            return BOXED_DOUBLE.toString()
        } else {
            return type
        }
    }

    static String unbox(String type) {
        if (BOXED_VOID.equalsContent(type)) {
            return VOID
        } else if (BOXED_BOOLEAN.equalsContent(type)) {
            return BOOLEAN
        } else if (BOXED_BYTE.equalsContent(type)) {
            return BYTE
        } else if (BOXED_SHORT.equalsContent(type)) {
            return SHORT
        } else if (BOXED_INT.equalsContent(type)) {
            return INT
        } else if (BOXED_LONG.equalsContent(type)) {
            return LONG
        } else if (BOXED_FLOAT.equalsContent(type)) {
            return FLOAT
        } else if (BOXED_DOUBLE.equalsContent(type)) {
            return DOUBLE
        }
        return type
    }

}
