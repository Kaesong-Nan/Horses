package com.forgenz.horses.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassUtil {
    public static Field getField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }

            return getField(superClass, fieldName);
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] params)
            throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }

            return getMethod(superClass, methodName, params);
        }
    }
}