package com.sci.valerie.type;

import java.util.*;

import com.sci.valerie.parser.tree.expr.type.*;

public final class Types {
    private static final Set<Integer> primitives = new HashSet<>();
    private static final Map<String, Integer> types = new HashMap<>();
    private static final Map<Integer, String> names = new HashMap<>();
    private static final Map<Integer, TypeST> typeSTs = new HashMap<>();
    private static int current;

    public static String getName(final int type) {
        if(type == -1) {
            return "unknown";
        }

        if(!Types.names.containsKey(type)) {
            return "invalid";
        }

        return Types.names.get(type);
    }

    public static int createType(final String name, final TypeST typeST) {
        if(Types.types.containsKey(name)) {
            throw new IllegalArgumentException("Type '" + name + "' already exists!");
        } 

        final int type = Types.current;
        Types.current++;
        Types.types.put(name, type);
        Types.names.put(type, name);
        Types.typeSTs.put(type, typeST);

        return type;
    }

    public static int getType(final String name) {
        if(Types.types.containsKey(name)) {
            return Types.types.get(name);
        } 

        return Types.UNKNOWN;
    }

    public static TypeST getTypeST(final int type) {
        return Types.typeSTs.get(type);
    }

    public static boolean isPrimitive(final int type) {
        return Types.primitives.contains(type);
    }

    public static boolean areCompatible(final int a, final int b) {
        if(a == UNKNOWN || b == UNKNOWN) {
            return false;
        }

        if(a == ANY || b == ANY) {
            return true;
        }

        final TypeST typeA = Types.getTypeST(a);
        final TypeST typeB = Types.getTypeST(b);

        if(typeA instanceof ArrayTypeST && typeB instanceof ArrayTypeST) {
            return Types.areCompatible(((ArrayTypeST) typeA).type.type(), ((ArrayTypeST) typeB).type.type());
        } 
        if(typeA instanceof ArrayTypeST) {
            return Types.areCompatible(((ArrayTypeST) typeA).type.type(), b);
        } 
        if(typeB instanceof ArrayTypeST) {
            return Types.areCompatible(((ArrayTypeST) typeB).type.type(), a);
        } 

        return a == b;
    }

    public static final int UNKNOWN = -1;
    public static final int ANY = Types.createType("any", null);
    public static final int INT = Types.createType("int", null);
    public static final int FLOAT = Types.createType("float", null);
    public static final int BOOL = Types.createType("bool", null);
    public static final int STRING = Types.createType("string", null);

    static {
        Types.primitives.add(Types.ANY);
        Types.primitives.add(Types.INT);
        Types.primitives.add(Types.FLOAT);
        Types.primitives.add(Types.BOOL);
        Types.primitives.add(Types.STRING);
    }

    private Types() {
    }
}