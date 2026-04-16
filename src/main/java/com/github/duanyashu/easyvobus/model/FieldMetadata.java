package com.github.duanyashu.easyvobus.model;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * 字段元数据
 *
 * @author duanyashu
 * 2026/4/8 14:23
 */
public class FieldMetadata {

    /** 字段路径 */
    private String fieldPath;

    /** 源字段名 */
    private String sourceFieldName;

    /** 源字段值 */
    private Object sourceValue;

    /** 目标字段名列表 */
    private List<String> targetFieldNames;

    /** 原始注解 */
    private Annotation annotation;

    /** 目标 Map（直接填充） */
    private Map<String, Object> targetMap;


    public FieldMetadata() {
    }

    public FieldMetadata(String fieldPath, String sourceFieldName, Object sourceValue, List<String> targetFieldNames, Annotation annotation, Map<String, Object> targetMap, Object currentObj) {
        this.fieldPath = fieldPath;
        this.sourceFieldName = sourceFieldName;
        this.sourceValue = sourceValue;
        this.targetFieldNames = targetFieldNames;
        this.annotation = annotation;
        this.targetMap = targetMap;
    }
    public String getFieldPath() {
        return this.fieldPath;
    }

    public String getSourceFieldName() {
        return this.sourceFieldName;
    }

    public Object getSourceValue() {
        return this.sourceValue;
    }

    public List<String> getTargetFieldNames() {
        return this.targetFieldNames;
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    public Map<String, Object> getTargetMap() {
        return this.targetMap;
    }


    public void setFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
    }

    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public void setSourceValue(Object sourceValue) {
        this.sourceValue = sourceValue;
    }

    public void setTargetFieldNames(List<String> targetFieldNames) {
        this.targetFieldNames = targetFieldNames;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setTargetMap(Map<String, Object> targetMap) {
        this.targetMap = targetMap;
    }


    public String toString() {
        return "FieldMetadata(fieldPath=" + this.getFieldPath() + ", sourceFieldName=" + this.getSourceFieldName() + ", sourceValue=" + this.getSourceValue() + ", targetFieldNames=" + this.getTargetFieldNames() + ", annotation=" + this.getAnnotation() + ", targetMap=" + this.getTargetMap() + ", currentObj=" + ")";
    }

    public static FieldMetadataBuilder builder() {
        return new FieldMetadataBuilder();
    }
    public static class FieldMetadataBuilder {
        private String fieldPath;
        private String sourceFieldName;
        private Object sourceValue;
        private List<String> targetFieldNames;
        private Annotation annotation;
        private Map<String, Object> targetMap;
        private Object currentObj;

        FieldMetadataBuilder() {
        }

        public FieldMetadataBuilder fieldPath(String fieldPath) {
            this.fieldPath = fieldPath;
            return this;
        }

        public FieldMetadataBuilder sourceFieldName(String sourceFieldName) {
            this.sourceFieldName = sourceFieldName;
            return this;
        }

        public FieldMetadataBuilder sourceValue(Object sourceValue) {
            this.sourceValue = sourceValue;
            return this;
        }

        public FieldMetadataBuilder targetFieldNames(List<String> targetFieldNames) {
            this.targetFieldNames = targetFieldNames;
            return this;
        }

        public FieldMetadataBuilder annotation(Annotation annotation) {
            this.annotation = annotation;
            return this;
        }

        public FieldMetadataBuilder targetMap(Map<String, Object> targetMap) {
            this.targetMap = targetMap;
            return this;
        }

        public FieldMetadataBuilder currentObj(Object currentObj) {
            this.currentObj = currentObj;
            return this;
        }

        public FieldMetadata build() {
            return new FieldMetadata(this.fieldPath, this.sourceFieldName, this.sourceValue, this.targetFieldNames, this.annotation, this.targetMap, this.currentObj);
        }

        public String toString() {
            return "FieldMetadata.FieldMetadataBuilder(fieldPath=" + this.fieldPath + ", sourceFieldName=" + this.sourceFieldName + ", sourceValue=" + this.sourceValue + ", targetFieldNames=" + this.targetFieldNames + ", annotation=" + this.annotation + ", targetMap=" + this.targetMap + ", currentObj=" + this.currentObj + ")";
        }
    }
}