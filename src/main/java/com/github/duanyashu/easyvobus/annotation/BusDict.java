package com.github.duanyashu.easyvobus.annotation;


import java.lang.annotation.*;

/**
 * 数据字典注解
 * @author duanyashu
 * 2026-04-07 08:53:23
 */
@Repeatable(BusDict.Container.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@BusAnnotation
public @interface BusDict {

    /**
     * 字典类型
     */

    String value();

    /**
     * dictType映射字段
     */
    String dictAlias() default "";


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
        BusDict[] value();  // 必须叫 value，必须返回 Ferry 数组
    }
}