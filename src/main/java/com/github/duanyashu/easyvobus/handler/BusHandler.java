package com.github.duanyashu.easyvobus.handler;


import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.FieldMetadata;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Bus 处理器接口 - 所有数据源处理器实现此接口
 * @author duanyashu
 * 2026/4/8 14:21
 */
 public interface BusHandler<T extends Annotation> {

    /**
     * 支持的注解类型
     */
    Class<T> supportAnnotation();

    /**
     * 获取展示字段名称
     * @param fieldName
     * @param annotation
     * @return list 展示字段名
     */
    List<String> getDictFieldNames(String fieldName, Annotation annotation);

    /**
     * 批量收集数据
     * @param context 上下文
     * @param metadataList 该类型的元数据列表
     * @return Map<源值, Map<字段名, 字段值>>
     */
    Map<Object, Map<String, Object>> batchCollectData(BusContext context, List<FieldMetadata> metadataList);
}