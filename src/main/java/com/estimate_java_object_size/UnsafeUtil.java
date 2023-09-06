package com.estimate_java_object_size;

import com.google.common.collect.ImmutableMap;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeUtil {

    private static final long SIZEOF_OBJECT = 16;
    private static int SIZE_OF_REF = 16;
    private static Unsafe unsafe;

    private static final ImmutableMap<Class<?>, Integer> PRIM_TO_SIZE_IN_BYTES = ImmutableMap.<Class<?>, Integer>builder()
            .put(boolean.class, 1).put(byte.class, Byte.SIZE / 8).put(short.class, Short.SIZE / 8)
            .put(char.class, Character.SIZE / 8).put(int.class, Integer.SIZE / 8).put(long.class, Long.SIZE / 8)
            .put(float.class, Float.SIZE / 8).put(double.class, Double.SIZE / 8).build();

    static {
        Field f = null;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
        } catch (NoSuchFieldException e) {
            // TODO
        }
        f.setAccessible(true);
        try {
            unsafe = (Unsafe) f.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        SIZE_OF_REF = unsafe == null ? 0 : unsafe.arrayIndexScale(Object[].class) / Byte.BYTES;
    }

    public static long size(Object o) {

        Field lastField = null;
        Class clazz = o.getClass();
        long maxOffset = SIZEOF_OBJECT;
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    long offset = unsafe.objectFieldOffset(field);
                    if (offset >= maxOffset) {
                        maxOffset = offset;
                        lastField = field;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);  // stop at the first class with fields

        try {
            return ceil8(maxOffset + sizeOfField(lastField));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int sizeOfField(Field lastField) throws NoSuchFieldException, IllegalAccessException {
        if (lastField == null) {
            return 0;  // object has no fields, I guess
        }
        Class<?> type = lastField.getType();
        if (type.isPrimitive()) {
            return PRIM_TO_SIZE_IN_BYTES.get(type);
        }
        return getSizeOfRef();  // must be object reference
    }

    private static int getSizeOfRef() throws NoSuchFieldException, IllegalAccessException {
        return SIZE_OF_REF;
    }

    private static long ceil8(long i) {
        return (i + 7) & ~7;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        System.out.println(UnsafeUtil.size("test"));
    }
}
