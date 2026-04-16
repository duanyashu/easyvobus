package com.github.duanyashu.easyvobus.handler.impl;

import com.github.duanyashu.easyvobus.annotation.BusMap;
import com.github.duanyashu.easyvobus.config.EasyVoBusProperties;
import com.github.duanyashu.easyvobus.handler.BusHandler;
import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.FieldMetadata;
import com.github.duanyashu.easyvobus.utils.ListUtils;
import com.github.duanyashu.easyvobus.utils.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BusMap数值映射表达式关联处理器
 *
 * @author duanyashu
 * 2026/4/8 14:34
 */
@Component
public class BusMapHandler implements BusHandler<BusMap> {
    @Resource
    private EasyVoBusProperties easyVoBusProperties;
    @Override
    public Class<BusMap> supportAnnotation() {
        return BusMap.class;
    }

    @Override
    public List<String> getDictFieldNames(String fieldName, Annotation annotation) {
        BusMap busMap = (BusMap) annotation;
        String dictFieldNames = busMap.alias();
        if (StringUtils.isEmpty(dictFieldNames)){
            dictFieldNames = fieldName+ easyVoBusProperties.getDefaultSuffix();
        }
        return ListUtils.list(dictFieldNames);
    }

    @Override
    public Map<Object, Map<String, Object>> batchCollectData(BusContext context,
                                                             List<FieldMetadata> metadataList) {
        Map<Object, Map<String, Object>> result = new HashMap<>();

        for (FieldMetadata metadata : metadataList) {
            BusMap annotation = (BusMap) metadata.getAnnotation();
            BusMap synthesizedBusEnum = AnnotationUtils.synthesizeAnnotation(annotation, null);
            String value = transExprValue(synthesizedBusEnum, (String) metadata.getSourceValue());
            Map<String, Object> map = new HashMap<>();
            map.put(metadata.getTargetFieldNames().get(0),value);
            result.put(metadata.getSourceValue(), map);
        }
        return result;
    }


    /**
     * 翻译字典值
     */
    private String transExprValue(BusMap annotation, String fieldValues) {
        String[] convertSource = annotation.value();
        // 2. 表达式翻译
        if (convertSource.length==0) {
            return "";
        }
        List<String> translatedValues = new ArrayList<>();
        for (String value : fieldValues.split(",")) {
            boolean translated = false;
            for (String itemExp : convertSource) {
                String[] itemArray = itemExp.split("=");
                if (itemArray.length == 2 && itemArray[0].equals(value)) {
                    translatedValues.add(itemArray[1]);
                    translated = true;
                    break;
                }
            }
            if (!translated) {
                translatedValues.add("");
            }
        }
        return StringUtils.join(translatedValues,",");
    }
}