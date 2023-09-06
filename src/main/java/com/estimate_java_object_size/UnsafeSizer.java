package com.estimate_java_object_size;

import com.force.commons.util.misc.UnsafeAccess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

public class UnsafeSizer {
    private final Cache<Class<?>, Long> sizeCache = CacheBuilder.newBuilder().weakKeys().maximumSize(1000).build();
    private static final UnsafeAccess unsafe;
    private static final long SIZEOF_OBJECT = 16;
    private static final int SIZEOF_REF;
    private static final Cache<Class<?>, Field[]> fieldsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    private static Field[] getFieldsForClass(Class<?> clazz) {
        try {
            return fieldsCache.get(clazz, clazz::getDeclaredFields);
        } catch (ExecutionException e) {
            // TODO
        }
        return new Field[0];
    }
    private static final ImmutableMap<Class<?>, OffsetScale> ARRAY_OFFSET_SCALE;
    private static final ImmutableMap<Class<?>, Integer> PRIM_TO_SIZE_IN_BYTES = ImmutableMap.<Class<?>, Integer>builder()
            .put(boolean.class, 1)
            .put(byte.class, Byte.SIZE / 8)
            .put(short.class, Short.SIZE / 8)
            .put(char.class, Character.SIZE / 8)
            .put(int.class, Integer.SIZE / 8)
            .put(long.class, Long.SIZE / 8)
            .put(float.class, Float.SIZE / 8)
            .put(double.class, Double.SIZE / 8)
            .build();

    static {
        UnsafeAccess temp;
        try {
            temp = UnsafeAccess.get();
        } catch (Throwable t) {
            temp = null;
        }
        unsafe = temp;

        ImmutableMap.Builder<Class<?>, OffsetScale> builder = ImmutableMap.builder();
        putOffsetScale(boolean[].class, builder);
        putOffsetScale(byte[].class, builder);
        putOffsetScale(short[].class, builder);
        putOffsetScale(char[].class, builder);
        putOffsetScale(int[].class, builder);
        putOffsetScale(long[].class, builder);
        putOffsetScale(float[].class, builder);
        putOffsetScale(double[].class, builder);
        putOffsetScale(Object[].class, builder);  // representative of all object arrays

        ARRAY_OFFSET_SCALE = builder.build();

        SIZEOF_REF = unsafe == null ? 0 : unsafe.arrayIndexScale(Object[].class);  // reference size calculated based on object array size
    }

    private static class OffsetScale {
        final int offset, scale;

        public OffsetScale(int offset, int scale) {
            this.offset = offset;
            this.scale = scale;
        }
    }

    private static void putOffsetScale(Class<?> clazz, ImmutableMap.Builder<Class<?>, OffsetScale> builder) {
        if (unsafe != null) {
            builder.put(clazz, new OffsetScale(unsafe.arrayBaseOffset(clazz), unsafe.arrayIndexScale(clazz)));
        }
    }


    public long getObjectSize(Object obj) {
        Class<?> clazz = obj.getClass();

        if (clazz == Object.class) {
            return SIZEOF_OBJECT;  // nothing we can do here, just hardcode this case
        }

        if (clazz.isArray()) {
            // it is an array - look up the offset and per-element size in the static map
            OffsetScale offsetScale = ARRAY_OFFSET_SCALE.get(clazz.getComponentType().isPrimitive() ? clazz : Object[].class);
            return ceil8(offsetScale.offset + offsetScale.scale * Array.getLength(obj));
        }

        long maxOffset = SIZEOF_OBJECT;
        Field lastField = null;
        do {
            for (Field field : getFieldsForClass(clazz)) {
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

        return ceil8(maxOffset + sizeOfField(lastField));
    }


    private int sizeOfField(Field lastField) {
        if (lastField == null) {
            return 0;  // object has no fields, I guess
        }
        Class<?> type = lastField.getType();
        if (type.isPrimitive()) {
            return PRIM_TO_SIZE_IN_BYTES.get(type);
        }
        return getSizeOfRef();  // must be object reference
    }

    /**
     * @return the size of a reference - may be overriden by tests who want to have results invariant of CompressedOops setting
     */
    public static int getSizeOfRef() {
        return SIZEOF_REF;
    }

    /**
     * Round up to the next power of 8, since that's what the
     * JVM does for object sizes.
     */
    private static long ceil8(long i) {
        return (i + 7) & ~7;
    }


    public boolean isInit() {
        return unsafe != null;
    }

    Cache<Class<?>, Long> getSizeCache() {
        return sizeCache;
    }
}
