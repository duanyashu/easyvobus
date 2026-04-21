package com.github.duanyashu.easyvobus.handler.impl;

import com.github.duanyashu.easyvobus.annotation.BusEnum;
import com.github.duanyashu.easyvobus.config.EasyVoBusProperties;
import com.github.duanyashu.easyvobus.enums.EasyVoBusEnum;
import com.github.duanyashu.easyvobus.handler.BusHandler;
import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.FieldMetadata;
import com.github.duanyashu.easyvobus.utils.ListUtils;
import com.github.duanyashu.easyvobus.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BusEnum枚举类关联处理器
 *
 * @author duanyashu
 * 2026/4/8 14:34
 */
@Component
public class BusEnumHandler implements BusHandler<BusEnum> {
    @Autowired
    private EasyVoBusProperties easyVoBusProperties;
    @Override
    public Class<BusEnum> supportAnnotation() {
        return BusEnum.class;
    }

    @Override
    public List<String> getDictFieldNames(String fieldName, Annotation annotation) {
        BusEnum busMap = (BusEnum) annotation;
        String dictFieldNames = busMap.alias();
        if (StringUtils.isEmpty(dictFieldNames)){
            dictFieldNames = fieldName+ easyVoBusProperties.getDefaultSuffix();
        }
        return ListUtils.list(dictFieldNames);
    }

    @Override
    public Map<Object, Map<String, Object>> batchCollectData(BusContext context,List<FieldMetadata> metadataList) {
        Map<Object, Map<String, Object>> result = new HashMap<>();
        // 按枚举类分组
        Map<Class<? extends EasyVoBusEnum>, List<FieldMetadata>> groupByEnum = metadataList.stream()
                .collect(Collectors.groupingBy(m -> {
                    BusEnum rawBusEnum = (BusEnum) m.getAnnotation();
                    // 通过 Spring 合成注解，自动处理 @AliasFor
                    BusEnum synthesizedBusEnum = AnnotationUtils.synthesizeAnnotation(rawBusEnum, null);
                    // 现在 value 和 enumType 已经等价，随便用哪个都行
                    return synthesizedBusEnum.value();
                }));

        for (Map.Entry<Class<? extends EasyVoBusEnum>, List<FieldMetadata>> entry : groupByEnum.entrySet()) {
            Class<? extends EasyVoBusEnum> enumClass = entry.getKey();
            List<FieldMetadata> list = entry.getValue();
            for (FieldMetadata metadata : list) {
                Object sourceValue = metadata.getSourceValue();
                result.put(sourceValue, getEnumData(enumClass, metadata));
            }
        }

        return result;
    }

    private Map<String, Object> getEnumData(Class<? extends EasyVoBusEnum> enumClass, FieldMetadata metadata) {
        Object sourceValue = metadata.getSourceValue();
        // 实现枚举 code -> text 映射
        EasyVoBusEnum enumData = EasyVoBusEnum.getEnumData(enumClass, sourceValue);
        Map<String, Object> map = new HashMap<>();
        map.put(metadata.getTargetFieldNames().get(0),enumData == null ? "" : enumData.getLabel());
        return map;
    }
}