package com.github.duanyashu.easyvobus.annotation;

import java.lang.annotation.*;

/**
 * 标记接口，用于识别
 *
 * @author duanyashu
 * 2026/4/8 14:16
 */
@Target(ElementType.ANNOTATION_TYPE)  // 重要：作用于注解
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusAnnotation {
}