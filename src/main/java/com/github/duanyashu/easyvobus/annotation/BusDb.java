package com.github.duanyashu.easyvobus.annotation;

import java.lang.annotation.*;
/**
 * 表关联注解
 * @author duanyashu
 * 2026-04-07 08:53:23
 */
@Repeatable(BusDb.Container.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@BusAnnotation
public @interface BusDb {

    /**
     * 方法描述: 查询数据表
     */
    String table();

    /**
     * 条件字段
     */
    String whereField();

    /**
     * 需要查询的字段
     */

    String[] selectField();

    /**
     * selectFiled映射字段
     */
    String[] selectFieldAlias() default {};


    /**
     * 执行本条注解的条件字段
     */
    String ruleField() default "";

    /**
     * 执行本条注解的条件值
     */
    String[] ruleValues() default {};

    /**
     * 执行sql查询的条件 （sql语句）
     */
    String param() default "";



    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @BusAnnotation
    public @interface Container {
        BusDb[] value();  // 必须叫 value，必须返回 Ferry 数组
    }
}