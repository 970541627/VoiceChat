package com.voice.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ReflectUtils {
    public static Object registerReflectClass(Object target) {
        try {
            Class unsafeClass = null;
            unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = (Unsafe) field.get(null);
            Module baseModule = target.getClass().getModule();
            Class currentClass = target.getClass();
            long addr = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
            unsafe.getAndSetObject(currentClass, addr, baseModule);
            return target;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
