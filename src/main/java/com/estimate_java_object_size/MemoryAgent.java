/*
 * Copyright, 2007, salesforce.com
 * All Rights Reserved
 * Company Confidential
 */
package com.estimate_java_object_size;

import com.force.commons.util.CachedReflection;
import com.force.commons.util.concurrent.Once;
import com.force.commons.util.misc.UnsafeAccess;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author fhossain
 * @since 148
 */
public class MemoryAgent {

    private static final int MAX_CACHE_SIZE = 10000;

    @IgnoreInGetObjectSize  // ignore our sizer in case we call size on ourselves, to avoid infinite recursion
    private final ObjectSizer sizer;

    private static final Cache<Class<?>,Boolean> SINGLETON_CACHE = CacheBuilder.newBuilder().weakKeys().maximumSize(MAX_CACHE_SIZE).build();
    private static final CachedReflection REFLECTION_CACHE = new CachedReflection(MAX_CACHE_SIZE);

    static abstract class ObjectSizer {

        /**
         * Get the shallow size of an object.
         * @param obj the object to return the shallow size for.
         * @return the shallow size, in bytes, of the passed object
         */
        abstract long getObjectSize(Object obj);

        /**
         * @return true if the instrumented is enabled and ready for work (may log once in the case it is not)
         */
        abstract boolean isInit();

        abstract Cache<Class<?>, Long> getSizeCache();
    }

    private static class InstrumentationSizer extends ObjectSizer {
        private final Cache<Class<?>,Long> sizeCache = CacheBuilder.newBuilder().weakKeys().maximumSize(MAX_CACHE_SIZE).build();

        private static final String NOT_INIT_ERROR_MESSAGE = "MemoryAgent instrumentation mode not initialized!\n" 
                + "Please run your jvm with: -javaagent:<dev directory>/app/<ver>/core/platform/test/unit/java/classes/memoryagent.jar option.";
        private static Instrumentation instrumentation;
        private static Once warnOnce = new Once();

        @Override
        public long getObjectSize(Object obj) {
            return instrumentation.getObjectSize(obj);
        }

        @Override
        public boolean isInit() {
            if (instrumentation == null && warnOnce.get()) {
            }
            return instrumentation != null;
        }

        @Override
        Cache<Class<?>, Long> getSizeCache() {
            return sizeCache;
        }  
    }

    public static final ObjectSizer INSTRUMENTATION_SIZER = new InstrumentationSizer();

    /**
     * Sizer that uses sun.misc.Unsafe to determine the object size.
     * <p>
     * The implementation is not actually unsafe, per se, since it only uses
     * safe support methods from unsafe, and not the arbitrary read/write methods.
     * <p>
     * The basic idea is to use unsafe to get all the fields in the object, and then
     * the size is the same as the field with the largest offset, plus the size of the
     * field itself, aligned appropiately for the JVM.
     *
     * @author tdowns
     * @since 190
     */
    private static class UnsafeSizer extends ObjectSizer {
        private final Cache<Class<?>,Long> sizeCache = CacheBuilder.newBuilder().weakKeys().maximumSize(MAX_CACHE_SIZE).build();
        private static final UnsafeAccess unsafe;
        private static final long SIZEOF_OBJECT = 16;
        private static final int SIZEOF_REF;
        private static final ImmutableMap<Class<?>, OffsetScale> ARRAY_OFFSET_SCALE;
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

        static {
            UnsafeAccess temp;
            try {
                temp = UnsafeAccess.get();
            } catch (Throwable t) {
                temp = null;
            }
            unsafe = temp;

            Builder<Class<?>, OffsetScale> builder = ImmutableMap.builder();
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

        private static void putOffsetScale(Class<?> clazz, Builder<Class<?>, OffsetScale> builder) {
            if (unsafe != null) {
                builder.put(clazz, new OffsetScale(unsafe.arrayBaseOffset(clazz), unsafe.arrayIndexScale(clazz)));
            }
        }

        @Override
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
        private static int getSizeOfRef() {
            return SIZEOF_REF;
        }

        /**
         * Round up to the next power of 8, since that's what the 
         * JVM does for object sizes.
         */
        private static long ceil8(long i) {
            return (i + 7) & ~7;
        }

        @Override
        public boolean isInit() {
            return unsafe != null;
        }

        @Override
        Cache<Class<?>, Long> getSizeCache() {
            return sizeCache;
        }
    }

    public static ObjectSizer SUN_MISC_SIZER = new UnsafeSizer();

    /**
     * Use this SFDCSingleton annotation to identify classes that are used as singleton within out code and needs to be skipped in size calculation.
     *
     * @author fhossain
     * @since 148
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SFDCSingleton {
    }

    /**
     * Use this IgnoreInGetObjectSize annotation to identify member variables to be ignored in size calculation.
     *
     * @author fhossain
     * @since 148
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IgnoreInGetObjectSize {
    }

    /**
     * Create a new memory agent, using the "best available" object sizer.
     * This is currently the instrumentation object sizer, if available, otherwise
     * the sun.misc.Unsafe sizer (which may also fail to load if we cannot access
     * Unsafe).
     */
    public MemoryAgent() {
        this(null);
    }

    private ObjectSizer getBestImplementation() {
        ObjectSizer impl = getInstrumentationSizer();

        if (impl == null) {
            impl = getUnsafeSizer();

            if (impl == null) {
                // ok neither of our two real sizers can be loaded, so just use a dummy sizer
                impl = new ObjectSizer() {

                    @Override
                    public boolean isInit() {
                        return true;
                    }

                    @Override
                    public long getObjectSize(Object obj) {
                        return -1;
                    }

                    @Override
                    Cache<Class<?>, Long> getSizeCache() {
                        return null;
                    }
                };
            }
        }

        return impl;
    }


    /** overridden by tests */
    ObjectSizer getInstrumentationSizer() {
        return INSTRUMENTATION_SIZER.isInit() ? INSTRUMENTATION_SIZER : null;
    }

    /** overridden by tests */
    ObjectSizer getUnsafeSizer() {
        return SUN_MISC_SIZER.isInit() ? SUN_MISC_SIZER : null;
    }

    public MemoryAgent(ObjectSizer sizer) {
        this.sizer = sizer == null ? getBestImplementation() : sizer;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        InstrumentationSizer.instrumentation = inst;
    }

    /**
     * Return the size of the passed object, counting transient fields.
     * 
     */
    public long getObjectSize(Object me) {
        return getObjectSize(me, false);
    }


    /**
     * Return the size in bytes of the passed object.
     *
     * @param me - Object you want to measure the size of
     * @param countTransient - Look inside transient values and count them
     * @return - Size in bytes of the object tree passed
     */
    public long getObjectSize(Object me, boolean countTransient) {
        checkState(sizer.isInit());  // must be true (since dummy impl is always init) unless someone explicitly used an uninitialized implementation
        try {
            // Now determine the size of the whole tree
            return new SizerContext(countTransient).getObjectSize(me);
        } catch (ExecutionException ex) {  // EEs come from our use of caches
            throw Throwables.propagate(ex);
        }
    }


    /**
     * Throw an {@link IllegalStateException} if the agent hasn't been initialized.  Useful for fail-fast in tests.
     */
    public void ensureInit() {
        if (!sizer.isInit()) {
            // probably because you are using the instrumentation sizer and the agent is not on the command line
            throw new IllegalStateException("The requested sizer is not initialized: " + sizer.getClass());
        }
    }

    /** Common interface for objects stored on our stack */
    private interface StackRecord {
        void execute();
    }
    
    /**
     * Holds the state for a single object which has been seen and needs to be sized.
     */
    private static class ObjectRecord implements StackRecord {
        final Object object;
        final Class<?> fullClass; // non-null if we are examining a base class of the given "full" class
        final Class<?> ownerClass; // non-null if we are examining the fully derived class - points to containing class
        final String name, modifier;
        final int depth;
        final ObjectRecord parent;  // if not null, the containing object, so we can update its aggregate size
        long size;  // aggregate size including children

        public ObjectRecord(Object object, Class<?> fullClass, Class<?> ownerClass, String name, String modifier, int depth, ObjectRecord parent) {
            this.object = object;
            this.fullClass = fullClass;
            this.ownerClass = ownerClass;
            this.name = name;
            this.modifier = modifier;
            this.depth = depth;
            this.parent = parent;
        }
        
        @Override
        public void execute() {
            // do nothing, object record is handled explicitly below
        }
    }

    /**
     * Holds the state for a single request to size an object graph. Members are things that are constant
     * (whose reference value is constant) throughout one request.
     */
    private class SizerContext {
        private final boolean countTransient;

        public SizerContext( boolean countTransient) {
            this.countTransient = countTransient;
        }

        long getObjectSize(Object me) throws ExecutionException {
            final IdentityHashMap<Object, Object> alreadyCounted = new IdentityHashMap<Object, Object>();
            final Deque<StackRecord> stack = Queues.newArrayDeque(Collections.<StackRecord>singleton(new ObjectRecord(me, null, null, "", null, 0, null)));
            final List<StackRecord> newRecords = Lists.newArrayList();  // every iteration we add 0 or more new records, collected in this list
            long ret = 0;

            while (!stack.isEmpty()) {

                StackRecord stackRecord = stack.removeFirst();
                
                if (!(stackRecord instanceof ObjectRecord)) {
                    stackRecord.execute();  // stuff like the debug output at the tail
                    continue;
                }
                
                final ObjectRecord r = (ObjectRecord)stackRecord;
                
                final Object o = r.object;
                Class<?> fullClass = r.fullClass;

                /*
                 * if countTransient is false do not count them
                 */
                if (o != null && !isSharedFlyweight(o) && (countTransient || !"TRANSIENT".equals(r.modifier))) {
                    // fullClass is not null when superclass member are being traversed for an object that has already been counted
                    boolean doIt = false;
                    if (fullClass == null) {

                        final Class<?> clazz = o.getClass();
                        // If this class is marked a singleton don't count them
//                        boolean isSingleton = SINGLETON_CACHE.get(clazz, new Callable<Boolean>() {
//                            @Override
//                            public Boolean call() throws Exception {
//                                return clazz.getAnnotation(SFDCSingleton.class) != null;
//                            }
//                        });

                        boolean isSingleton = false;

                        if (isSingleton) {
                        } else if (!alreadyCounted.containsKey(o)) {
                            alreadyCounted.put(o, o);
                            doIt = true;
                        } else {
                        }
                    } else {
                        doIt = true;
                    }
                    
                    if (!doIt) {
                        continue;
                    }
                    
                    boolean isSummaryFormat = true;

                    final long shallowSize;

                    if (fullClass == null) {
                        fullClass = o.getClass();

                        if (fullClass.isArray()) {
                            // array shallow class sizes can't be cached, because they have variable length 
                            shallowSize = sizer.getObjectSize(o);
                        } else {
                            Cache<Class<?>, Long> sizeCache = sizer.getSizeCache();
                            shallowSize = sizeCache == null ? sizer.getObjectSize(o) : sizeCache.get(fullClass,
                                    () -> sizer.getObjectSize(o));
//                            shallowSize = sizer.getObjectSize(o);
                        }
                        
                        ret += shallowSize;
                        r.size = shallowSize;


                        // Suppress detail logging for string classes
                        if (fullClass.equals(String.class)) {
                            isSummaryFormat = false;
                        }
                    } else {
                        shallowSize = 0;  // we are looking at a base class, whose size is already included in the parent
                    }


                    newRecords.clear();

                    if (fullClass.isArray()) {
                        // Primitive types array size is already included in the getObjectSize
                        if (!fullClass.getComponentType().isPrimitive()) {
                            Object[] array = (Object [])o;
                            for (int i = 0; i < array.length; i++) {
                                newRecords.add(new ObjectRecord(array[i], null, fullClass, Integer.toString(i), null, r.depth+1, r));
                            }
                        }
                    } else {
                        for (Field f : REFLECTION_CACHE.getDeclaredFields(fullClass)) {
                            /* Look at all non-primitive and non static fields */
                            if (!f.getType().isPrimitive() && !Modifier.isStatic(f.getModifiers())) {
                                boolean isAccessible = f.isAccessible();
                                try {
                                    if (!isAccessible) {
                                        f.setAccessible(true);
                                    }
                                    if (f.getAnnotation(IgnoreInGetObjectSize.class) != null) {
                                        // Field ignored so just log that
                                    } else if (f.get(o) == null && !fullClass.getName().startsWith("java")) {
                                        // Field null so just log that
                                    } else if (is3rdPartyIgnoreable(o, fullClass, f)) {
                                        // Field is considered ignoreable only show if not null
                                        if (f.get(o) != null) {
                                        }
                                    }
                                    else {
                                        newRecords.add(new ObjectRecord(f.get(o), null, fullClass, f.getName(), getMessageForModifier(fullClass, f.getModifiers()), r.depth + 1, r));
                                    }
                                } catch (IllegalAccessException ie) {
                                } finally {
                                    if (!isAccessible) {
                                        f.setAccessible(false);
                                    }
                                }
                            }
                        }
                    }

                    // Add the superclass members too except for base object class
                    if (fullClass.getSuperclass() != null && !fullClass.getSuperclass().equals(Object.class)) {
                        newRecords.add(new ObjectRecord(o, fullClass.getSuperclass(), null, "", r.modifier, r.depth + 1, r));
                    }
                    // add any new records in reverse order (since we want to process the first things first, just like the recursive version)
                    for (int i=newRecords.size()-1; i >=0; i--) {
                        stack.addFirst(newRecords.get(i));
                    }
                }
            }
            
            return ret;
        }

    }

    /**
     * Returns true if this is a well-known shared flyweight.
     * For example, interned Strings, Booleans and Number objects
     */
    private static boolean isSharedFlyweight(Object obj) {

        // optimization - all of our flyweights are Comparable
        if (obj instanceof Comparable) {
            if (obj instanceof Enum) {
                return true;
            } else if (obj instanceof String) {
                return ((String) obj).length() < 1024 ? (obj == ((String) obj).intern()) : false;
            } else if (obj instanceof Boolean) {
                return (obj == Boolean.TRUE || obj == Boolean.FALSE);
            } else if (obj instanceof Integer) {
                return (obj == Integer.valueOf((Integer) obj));
            } else if (obj instanceof Short) {
                return (obj == Short.valueOf((Short) obj));
            } else if (obj instanceof Byte) {
                return (obj == Byte.valueOf((Byte) obj));
            } else if (obj instanceof Long) {
                return (obj == Long.valueOf((Long) obj));
            } else if (obj instanceof Character) {
                return (obj == Character.valueOf((Character) obj));
            }
        }
        return false;
    }

    /**
     * There are objects and fields in the JDK and 3rd party library that are known to be either singleton or allocated lazily as facades
     * and should be ignored. Since we cannot put @IgnoreInGetObjectSize there we will just have to simulate it.
     * @param o
     * @param myClass
     * @param field
     * @return
     */
    private static final Set<String> IGNORABLE_ELEMENTS;
    static {
        IGNORABLE_ELEMENTS = new HashSet<String>();
        IGNORABLE_ELEMENTS.add("java.util.AbstractMap.values");
        IGNORABLE_ELEMENTS.add("java.util.AbstractMap.keySet");
        IGNORABLE_ELEMENTS.add("java.util.HashMap.entrySet");
        IGNORABLE_ELEMENTS.add("java.util.TreeMap.entrySet");
    }
    private static boolean is3rdPartyIgnoreable(Object o, Class<?> myClass, Field f) {
        if (myClass.getName().startsWith("java.util.")) {
            if (IGNORABLE_ELEMENTS.contains(myClass.getName() + "." + f.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String getMessageForModifier(Class<?> ownerClass, int mod) {
        if (ownerClass != null && !ownerClass.getName().startsWith("java") && Modifier.isTransient(mod)) {
            return Modifier.toString(Modifier.TRANSIENT).toUpperCase();
        }
        return null;
    }

    @Override
    public String toString() {
        return "MemoryAgent." + this.sizer.getClass().getSimpleName();
    }
}