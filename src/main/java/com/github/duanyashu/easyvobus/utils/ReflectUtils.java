package com.github.duanyashu.easyvobus.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.WeakHashMap;

/**
 * 反射工具类
 */
public class ReflectUtils {

    /**
     * 替代 hutool WeakConcurrentMap，使用 JDK 自带组合实现
     */
    private static final WeakHashMap<Class<?>, Field[]> FIELDS_CACHE = new WeakHashMap<>();

    /**
     * 自定义别名注解（替代Hutool Alias）
     */
    public @interface Alias {
        String value();
    }

    /**
     * 自定义工具异常（替代Hutool UtilException）
     */
    public static class UtilException extends RuntimeException {
        public UtilException(Throwable cause, String message, Object... params) {
            super(String.format(message, params), cause);
        }

        public UtilException(String message) {
            super(message);
        }
    }

    /**
     * 获取字段值
     *
     * @param obj       对象，如果static字段，此处为类
     * @param fieldName 字段名
     * @return 字段值
     * @throws UtilException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, String fieldName) throws UtilException {
        if (obj == null || StringUtils.isEmpty(fieldName)) {
            return null;
        }
        return getFieldValue(obj, getField(obj instanceof Class ? (Class<?>) obj : obj.getClass(), fieldName));
    }

    /**
     * 获取静态字段值
     *
     * @param field 字段
     * @return 字段值
     * @throws UtilException 包装IllegalAccessException异常
     */
    public static Object getStaticFieldValue(Field field) throws UtilException {
        return getFieldValue(null, field);
    }

    /**
     * 查找指定类中的指定name的字段（包括非public字段），也包括父类和Object类的字段， 字段不存在则返回null
     *
     * @param beanClass 被查找字段的类,不能为null
     * @param name      字段名
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field getField(Class<?> beanClass, String name) throws SecurityException {
        final Field[] fields = getFields(beanClass);
        if (fields == null || fields.length == 0) {
            return null;
        }
        // 遍历匹配字段名（支持@Alias别名）
        for (Field field : fields) {
            if (name.equals(getFieldName(field))) {
                return field;
            }
        }
        return null;
    }

    /**
     * 获取字段名，如果存在@Alias注解，读取注解的值作为名称
     *
     * @param field 字段
     * @return 字段名
     */
    public static String getFieldName(Field field) {
        if (field == null) {
            return null;
        }
        final Alias alias = field.getAnnotation(Alias.class);
        if (alias != null) {
            return alias.value();
        }
        return field.getName();
    }

    /**
     * 获取字段值
     *
     * @param obj   对象，static字段则此字段为null
     * @param field 字段
     * @return 字段值
     * @throws UtilException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, Field field) throws UtilException {
        if (field == null) {
            return null;
        }
        if (obj instanceof Class) {
            // 静态字段获取时对象为null
            obj = null;
        }

        setAccessible(field);
        Object result;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            throw new UtilException(e, "IllegalAccess for %s.%s", field.getDeclaringClass(), field.getName());
        }
        return result;
    }

    /**
     * 设置方法为可访问（私有方法可以被外部调用）
     *
     * @param accessibleObject 可设置访问权限的对象，比如Field、Method等
     * @return 被设置可访问的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T accessibleObject) {
        if (accessibleObject != null && !accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }

    /**
     * 获取类的所有字段（包含父类，带缓存）
     */
    public static Field[] getFields(Class<?> beanClass) throws SecurityException {
        notNull(beanClass, "beanClass must not be null");
        // 双重检查锁保证线程安全，替代computeIfAbsent
        Field[] fields = FIELDS_CACHE.get(beanClass);
        if (fields == null) {
            synchronized (FIELDS_CACHE) {
                fields = FIELDS_CACHE.get(beanClass);
                if (fields == null) {
                    fields = getFieldsDirectly(beanClass);
                    FIELDS_CACHE.put(beanClass, fields);
                }
            }
        }
        return fields;
    }

    /**
     * 直接获取类及其所有父类的字段（不含缓存）
     */
    public static Field[] getFieldsDirectly(Class<?> beanClass) throws SecurityException {
        notNull(beanClass, "beanClass must not be null");

        Field[] allFields = null;
        Class<?> searchType = beanClass;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (allFields == null) {
                allFields = declaredFields;
            } else {
                allFields = append(allFields, declaredFields);
            }
            searchType = searchType.getSuperclass();
        }
        return allFields == null ? new Field[0] : allFields;
    }

    // ========================== 以下为内部工具方法 ==========================


    /**
     * 非空断言
     */
    private static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new UtilException(message);
        }
    }

    /**
     * 数组合并（替代Hutool ArrayUtil.append）
     */
    private static Field[] append(Field[] array1, Field[] array2) {
        if (array1 == null || array1.length == 0) {
            return array2 == null ? new Field[0] : array2.clone();
        }
        if (array2 == null || array2.length == 0) {
            return array1.clone();
        }
        Field[] result = new Field[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}