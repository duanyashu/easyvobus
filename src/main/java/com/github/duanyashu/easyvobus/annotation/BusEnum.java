package com.github.duanyashu.easyvobus.annotation;


import com.github.duanyashu.easyvobus.enums.EasyVoBusEnum;

import java.lang.annotation.*;

/**
 * 表关联注解
 * @author duanyashu
 * 2026-04-07 08:53:23
 */
@Repeatable(BusEnum.Container.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@BusAnnotation
public @interface BusEnum {

    /**
     * 枚举类型
     * <p>
     * 必须继承 EasyVoBusEnum 接口
     */
    Class<? extends EasyVoBusEnum> value();
    /**
     * 枚举label映射字段
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
        BusEnum[] value();  // 必须叫 value，必须返回 Ferry 数组
    }
}