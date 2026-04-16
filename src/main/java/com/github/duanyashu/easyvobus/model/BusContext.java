package com.github.duanyashu.easyvobus.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据源上下文
 *
 * @author duanyashu
 * 2026/4/8 14:23
 */
public class BusContext {
    /** 原始对象 */
    private Object sourceObject;

    /** 最终返回的 Map */
    private Map<String, Object> resultMap;

    /** 按注解类型分组的元数据 */
    private Map<Class<?>, List<FieldMetadata>> metadataMap;

    /** 全局缓存 */
    private Map<String, Object> cache;

    /** 已处理的对象（避免循环引用） */
    private Set<Object> processedObjects;

    public Object getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(Object sourceObject) {
        this.sourceObject = sourceObject;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    public Map<Class<?>, List<FieldMetadata>> getMetadataMap() {
        return metadataMap;
    }

    public void setMetadataMap(Map<Class<?>, List<FieldMetadata>> metadataMap) {
        this.metadataMap = metadataMap;
    }

    public Map<String, Object> getCache() {
        return cache;
    }

    public void setCache(Map<String, Object> cache) {
        this.cache = cache;
    }

    public Set<Object> getProcessedObjects() {
        return processedObjects;
    }

    public void setProcessedObjects(Set<Object> processedObjects) {
        this.processedObjects = processedObjects;
    }
}