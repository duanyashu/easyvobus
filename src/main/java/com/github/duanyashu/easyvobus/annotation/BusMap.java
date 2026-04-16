package com.github.duanyashu.easyvobus.annotation;


import java.lang.annotation.*;

/**
 * 表关联注解
 * @author duanyashu
 * 2026-04-07 08:53:23
 */
@Repeatable(BusMap.Container.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@BusAnnotation
public @interface BusMap {

    /** 映射关系：{"0=男", "1=女", "2=未知"} */
    String[] value();

    /**
     * dictType映射字段
     */
    String alias() default "";

    /**
     * 执行本条注解的条件字段
     */
    String ruleField() default "";

    /**
     * 执行本条注解的条件值
     */
    String[] ruleValues() default {};


    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @BusAnnotation
    public @interface Container {
        BusMap[] value();  // 必须叫 value，必须返回 Ferry 数组
    }
}