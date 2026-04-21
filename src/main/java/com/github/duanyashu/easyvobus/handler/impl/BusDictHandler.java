package com.github.duanyashu.easyvobus.handler.impl;

import com.github.duanyashu.easyvobus.annotation.BusDict;
import com.github.duanyashu.easyvobus.config.EasyVoBusProperties;
import com.github.duanyashu.easyvobus.handler.BusHandler;
import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.EasyVoBusDict;
import com.github.duanyashu.easyvobus.model.FieldMetadata;
import com.github.duanyashu.easyvobus.service.IEasyVoBusDictService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BusDict字典关联处理器
 *
 * @author duanyashu
 * 2026/4/8 14:34
 */
@Component
public class BusDictHandler implements BusHandler<BusDict> {

    @Autowired
    Optional<IEasyVoBusDictService> dictServiceOptional;
    @Autowired
    private EasyVoBusProperties easyVoBusProperties;
    @Override
    public Class<BusDict> supportAnnotation() {
        return BusDict.class;
    }

    @Override
    public List<String> getDictFieldNames(String fieldName, Annotation annotation) {
        BusDict busMap = (BusDict) annotation;
        String dictFieldNames = busMap.dictAlias();
        if (StringUtils.isEmpty(dictFieldNames)){
            dictFieldNames = fieldName+ easyVoBusProperties.getDefaultSuffix();
        }
        return ListUtils.list(dictFieldNames);
    }

    @Override
    public Map<Object, Map<String, Object>> batchCollectData(BusContext context,List<FieldMetadata> metadataList) {
        Map<Object, Map<String, Object>> result = new HashMap<>();
        if (!dictServiceOptional.isPresent()){
            return result;
        }
        // 按枚举类分组
        Map<String, List<FieldMetadata>> groupByEnum = metadataList.stream()
                .collect(Collectors.groupingBy(m -> {
                    BusDict rawBusEnum = (BusDict) m.getAnnotation();
                    // 通过 Spring 合成注解，自动处理 @AliasFor
                    BusDict synthesizedBusEnum = AnnotationUtils.synthesizeAnnotation(rawBusEnum, null);
                    // 现在 value 和 enumType 已经等价，随便用哪个都行
                    return synthesizedBusEnum.value();
                }));

        for (Map.Entry<String, List<FieldMetadata>> entry : groupByEnum.entrySet()) {
            String dictType = entry.getKey();
            List<FieldMetadata> list = entry.getValue();
            List<String> sourceValueList = list.stream().flatMap(metadata->StringUtils.split(StringUtils.toStr(metadata.getSourceValue()), ",").stream()).collect(Collectors.toList());
            // 查询字典接口
            List<EasyVoBusDict> voBusDictList = dictServiceOptional.get().getDictList(dictType,sourceValueList);
            if (StringUtils.isEmpty(voBusDictList)){
                continue;
            }
            Map<String, String> dictMap = voBusDictList.stream().collect(Collectors.toMap(EasyVoBusDict::getValue, EasyVoBusDict::getLabel));
            for (FieldMetadata metadata : list) {
                Object sourceValue = metadata.getSourceValue();
                String label = StringUtils.split(StringUtils.toStr(sourceValue), ",").stream().map(value -> StringUtils.nullToDefault(dictMap.get(value),"")).collect(Collectors.joining(","));
                Map<String, Object> map = new HashMap<>();
                map.put(metadata.getTargetFieldNames().get(0),label);
                result.put(sourceValue, map);
            }
        }

        return result;
    }

}