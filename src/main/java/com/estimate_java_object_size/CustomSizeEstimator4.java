//package com.estimate_java_object_size;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import org.apache.commons.lang3.tuple.Pair;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//import java.util.*;
//import java.util.concurrent.ExecutionException;
//
//public class CustomSizeEstimator4 {
//
//    private CustomSizeEstimator4() {
//    }
//
//    private static final int SIZE_OF_REF;
//
//    private static final Map<Class<?>, Integer> primitiveTypeToSizeMap = new HashMap<>();
//    private static final Map<Class<?>, Integer> wrapperTypeToSizeMap = new HashMap<>();
//    private static final Cache<Class<?>, Field[]> fieldsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
//    private static final UnsafeSizer unsafeSizer = new UnsafeSizer();
//
//    private static Field[] getFieldsForClass(Class<?> clazz) {
//        try {
//            return fieldsCache.get(clazz, clazz::getDeclaredFields);
//        } catch (ExecutionException e) {
//            // TODO
//        }
//        return new Field[0];
//    }
//
//    static {
//        primitiveTypeToSizeMap.put(boolean.class, Byte.BYTES);
//        primitiveTypeToSizeMap.put(char.class, Character.BYTES);
//        primitiveTypeToSizeMap.put(byte.class, Byte.BYTES);
//        primitiveTypeToSizeMap.put(short.class, Byte.BYTES);
//        primitiveTypeToSizeMap.put(int.class, Integer.BYTES);
//        primitiveTypeToSizeMap.put(long.class, Long.BYTES);
//        primitiveTypeToSizeMap.put(float.class, Float.BYTES);
//        primitiveTypeToSizeMap.put(double.class, Double.BYTES);
//        primitiveTypeToSizeMap.put(void.class, 0);
//
//        wrapperTypeToSizeMap.put(Boolean.class, Byte.BYTES);
//        wrapperTypeToSizeMap.put(Character.class, Character.BYTES);
//        wrapperTypeToSizeMap.put(Byte.class, Byte.BYTES);
//        wrapperTypeToSizeMap.put(Short.class, Byte.BYTES);
//        wrapperTypeToSizeMap.put(Integer.class, Integer.BYTES);
//        wrapperTypeToSizeMap.put(Long.class, Long.BYTES);
//        wrapperTypeToSizeMap.put(Float.class, Float.BYTES);
//        wrapperTypeToSizeMap.put(Double.class, Double.BYTES);
//        wrapperTypeToSizeMap.put(Void.class, 0);
//
//        SIZE_OF_REF = UnsafeSizer.getSizeOfRef();
//    }
//
//    public static int sizeOf(Object obj) {
//        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
//        Stack<Object> stack = new Stack<>();
//        int size = 0;
//        stack.push(obj);
//        while (!stack.empty()) {
//            Object o = stack.pop();
//            size += sizeOf(o, visited, stack);
//        }
//        return size;
//    }
//
//    private static int sizeOf(Object obj, IdentityHashMap<Object, Boolean> visited, Stack<Object> stack) {
//        if (visited.containsKey(obj)) {
//            return 0;
//        } else {
//            visited.put(obj, null);
//        }
//
//        if (obj == null)
//            return 0;
//
//        int size = 0;
//        Class<?> clazz = obj.getClass();
//
//        size += SizeOfAgent.sizeOf(obj);
//
//        while (clazz != Object.class) {
//            for (Field field : getFieldsForClass(clazz)) {
//
//                if (Modifier.isStatic(field.getModifiers()))
//                    continue;
//
//                boolean isAccessible = field.isAccessible();
//                try {
//                    if (!isAccessible) {
//                        field.setAccessible(true);
//                    }
//
//                    if (isPrimitive(field.getType())) {
//                        continue;
//                    }
//
//                    Object fieldValue = getFieldValue(obj, field);
//                    if (fieldValue == null)
//                        continue;
//
//                    Pair<Integer, Boolean> sizePair = checkKnownTypesAndGetSize(fieldValue, field.getType(), visited);
//                    if (sizePair.getRight()) {
//                        size += sizePair.getLeft();
//                        continue;
//                    }
//                    stack.push(fieldValue);
//
//                } finally {
//                    if (!isAccessible) {
//                        field.setAccessible(false);
//                    }
//                }
//            }
//            clazz = clazz.getSuperclass();
//        }
//
//        return size;
//    }
//
//    private static Object getFieldValue(Object o, Field f) {
//        try {
//            return f.get(o);
//        } catch (IllegalAccessException e) {
//            return null;
//        }
//    }
//
//    private static int getArraySize(Object obj, IdentityHashMap<Object, Boolean> visited) {
//
//        if (obj instanceof boolean[]) {
//            boolean[] arr = (boolean[]) obj;
//            return arr.length;
//        }
//        if (obj instanceof byte[]) {
//            byte[] arr = (byte[]) obj;
//            return arr.length * Byte.BYTES;
//        }
//        if (obj instanceof short[]) {
//            short[] arr = (short[]) obj;
//            return arr.length * Short.BYTES;
//        }
//        if (obj instanceof char[]) {
//            char[] arr = (char[]) obj;
//            return arr.length * Character.BYTES;
//        }
//        if (obj instanceof int[]) {
//            int[] arr = (int[]) obj;
//            return arr.length * Integer.BYTES;
//        }
//        if (obj instanceof float[]) {
//            float[] arr = (float[]) obj;
//            return arr.length * Float.BYTES;
//        }
//        if (obj instanceof long[]) {
//            long[] arr = (long[]) obj;
//            return arr.length * Long.BYTES;
//        }
//        if (obj instanceof double[]) {
//            double[] arr = (double[]) obj;
//            return arr.length * Double.BYTES;
//        }
//        Object[] arr = (Object[]) obj;
//        int sum = 0;
//        for (Object ob : arr) {
//            sum += sizeOf(ob, visited, null);
//        }
//        return sum;
//    }
//
//    private static Pair<Integer, Boolean> checkKnownTypesAndGetSize(Object obj, Class<?> clazz, IdentityHashMap<Object, Boolean> visited) {
//
//        if (isSharedFlyweight(obj)) {
//            return Pair.of(0, true);
//        }
//
//        int size = 0;
//        boolean knownType = false;
//
//        if (obj == null) {
//            return Pair.of(0, true);
//        }
//
//        if (obj instanceof Enum) {
//            return Pair.of(0, true);
//        }
//
//        if (obj instanceof String) {
//            knownType = true;
//            size += ((String) obj).length();
//        } else if (isWrapperClass(clazz)) {
//            knownType = true;
//            size += getSizeOfWrapper(clazz);
//        } else if (isArray(clazz)) {
//            knownType = true;
//            size += getArraySize(obj, visited);
//        } else if (obj instanceof Collection<?>) {
//            knownType = true;
//            for (Object e : (Collection<?>) obj) {
//                size += sizeOf(e, visited, null);
//            }
//        } else if (obj instanceof Map<?, ?>) {
//            knownType = true;
//            for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
//                size += sizeOf(e.getKey(), visited, null);
//                size += sizeOf(e.getValue(), visited, null);
//            }
//        }
//        return Pair.of(size, knownType);
//    }
//
//    private static boolean isPrimitive(Class<?> clazz) {
//        return primitiveTypeToSizeMap.containsKey(clazz);
//    }
//
//    private static int getSizeOfPrimitive(Class<?> wrapper) {
//        return primitiveTypeToSizeMap.get(wrapper);
//    }
//
//    private static boolean isWrapperClass(Class<?> clazz) {
//        return wrapperTypeToSizeMap.containsKey(clazz);
//    }
//
//    private static int getSizeOfWrapper(Class<?> wrapper) {
//        return wrapperTypeToSizeMap.get(wrapper);
//    }
//
//    private static boolean isArray(Class<?> clazz) {
//        return clazz.isArray();
//    }
//
//
//    private static boolean isSharedFlyweight(Object obj) {
//
//        if (obj instanceof Comparable) {
//            if (obj instanceof Enum) {
//                return true;
//            } else if (obj instanceof String) {
//                return ((String) obj).length() < 1024 ? (obj == ((String) obj).intern()) : false;
//            } else if (obj instanceof Boolean) {
//                return (obj == Boolean.TRUE || obj == Boolean.FALSE);
//            } else if (obj instanceof Integer) {
//                return (obj == Integer.valueOf((Integer) obj));
//            } else if (obj instanceof Short) {
//                return (obj == Short.valueOf((Short) obj));
//            } else if (obj instanceof Byte) {
//                return (obj == Byte.valueOf((Byte) obj));
//            } else if (obj instanceof Long) {
//                return (obj == Long.valueOf((Long) obj));
//            } else if (obj instanceof Character) {
//                return (obj == Character.valueOf((Character) obj));
//            }
//        }
//        return false;
//    }
//
//}