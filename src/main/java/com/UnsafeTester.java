package com;

import com.google.common.collect.ImmutableMap;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeTester {
    
    public static class P {
        private  String s ="getgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrht" ;
        private  String s1 ="getgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrht" ;
        private  String s2 ="getgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrht" ;
        private  String s3 ="getgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrht" ;
        private  String s4 ="getgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrht" ;
        private  String s5 ="getgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrhtgetgtegtgtrgtrgtrhtrhgtrhtrhrtghtrhtrht" ;
        private  byte n1;
        private  byte n2;
        private  byte n3;
        private  byte n4;
        private  byte n5;
        private  byte n6;
        private  byte n7;
        private  byte n8;

    }
    private static final long SIZEOF_OBJECT = 16;

    private static final ImmutableMap<Class<?>, Integer> PRIM_TO_SIZE_IN_BYTES = ImmutableMap.<Class<?>, Integer>builder()
            .put(boolean.class,   1)
            .put(byte.class,   Byte.SIZE / 8)
            .put(short.class,  Short.SIZE / 8)
            .put(char.class,   Character.SIZE / 8)
            .put(int.class,    Integer.SIZE / 8)
            .put(long.class,   Long.SIZE / 8)
            .put(float.class,  Float.SIZE / 8)
            .put(double.class, Double.SIZE / 8)
            .build();


    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

        P p1 = new P();

        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);
        Field lastField = null;
        Class clazz = p1.getClass();
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

        System.out.println(ceil8(maxOffset + sizeOfField(lastField)));
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
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);
        return unsafe.arrayIndexScale(Object[].class);
    }

    private static long ceil8(long i) {
        return (i + 7) & ~7;
    }
}
