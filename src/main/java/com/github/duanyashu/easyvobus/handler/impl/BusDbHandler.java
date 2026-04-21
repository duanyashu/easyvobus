package com.github.duanyashu.easyvobus.handler.impl;

import com.github.duanyashu.easyvobus.annotation.BusDb;
import com.github.duanyashu.easyvobus.handler.BusHandler;
import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.EasyVoBusDbRequest;
import com.github.duanyashu.easyvobus.model.FieldMetadata;
import com.github.duanyashu.easyvobus.service.IEasyVoBusDbService;
import com.github.duanyashu.easyvobus.utils.ListUtils;
import com.github.duanyashu.easyvobus.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BusDb表关联处理器
 * @author duanyashu
 *  2026/4/8 14:25
 */
public class BusDbHandler implements BusHandler<BusDb> {

    @Autowired
    IEasyVoBusDbService dbService;

    @Override
    public Class<BusDb> supportAnnotation() {
        return BusDb.class;
    }

    @Override
    public List<String> getDictFieldNames(String fieldName, Annotation annotation) {
        BusDb busDb = (BusDb) annotation;
        String[] dictTexts = busDb.selectField();
        String[] dictFieldNames = busDb.selectFieldAlias();
        // 检查长度是否匹配
        if (dictTexts.length != dictFieldNames.length) {
            return ListUtils.list(dictTexts).stream()
                    .map(text -> fieldName + "_" + StringUtils.toCamelCase(text))
                    .collect(Collectors.toList());
        }

        return ListUtils.list(dictFieldNames);
    }

    @Override
    public Map<Object, Map<String, Object>> batchCollectData(BusContext context, List<FieldMetadata> metadataList) {
        // 按表分组批量查询
        Map<String, List<FieldMetadata>> groupByTable = metadataList.stream()
                .collect(Collectors.groupingBy(m -> {
                    BusDb busDb = (BusDb) m.getAnnotation();
                    return busDb.table()+","+busDb.whereField();
                }));

        Map<Object, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<String, List<FieldMetadata>> entry : groupByTable.entrySet()) {
            List<FieldMetadata> list = entry.getValue();
            // 批量查询
            Map<Object, Map<String, Object>> tableResult = generateData(list);
            for (Map.Entry<Object, Map<String, Object>> entry1 : tableResult.entrySet()) {
                Object key = entry1.getKey();
                Map<String, Object> newInnerMap = entry1.getValue();
                if (result.containsKey(key)){
                    Map<String, Object> existingInnerMap = result.get(key);
                    existingInnerMap.putAll(newInnerMap);
                }else {
                    result.put(key,newInnerMap);
                }
            }
        }
        return result;
    }


    private Map<Object, Map<String, Object>> generateData(List<FieldMetadata> metadataList) {
        BusDb annotation = (BusDb)metadataList.get(0) .getAnnotation();
        List<String> queryValueList = metadataList.stream().flatMap(metadata->StringUtils.split(StringUtils.toStr(metadata.getSourceValue()), ",").stream()).collect(Collectors.toList());
        List<Map<String,Object>> list = executeQuery(annotation.table(), ListUtils.list(annotation.selectField()), annotation.whereField(), queryValueList, annotation.param());
        Map<Object,Map<String,Object>> rest = new HashMap<>();
        for (FieldMetadata metadata : metadataList) {
            List<String> valueList = StringUtils.split(StringUtils.toStr(metadata.getSourceValue()), ",");
            List<Map<String,Object>> dataList = list.stream().filter(map ->{
                        String fieldName = annotation.whereField();
                        Object value = map.getOrDefault(fieldName, map.get(StringUtils.toCamelCase(fieldName)));
                        return valueList.contains(StringUtils.toStr(value));
                    })
                    .map(map -> map.entrySet().stream().collect(Collectors.toMap(entry -> StringUtils.toCamelCase(entry.getKey()), Map.Entry::getValue))).collect(Collectors.toList());
            Map<String,Object> targetNameMap = new HashMap<>();
            List<String> targetFieldNames = metadata.getTargetFieldNames();
            for (int i = 0; i < targetFieldNames.size(); i++) {
                String fieldName = targetFieldNames.get(i);
                String selectFieldName = StringUtils.toCamelCase(annotation.selectField()[i]);
                String values = dataList.stream().filter(data -> data.containsKey(selectFieldName)).map(data -> StringUtils.nullToDefault(StringUtils.toStr(data.get(selectFieldName)),"")).collect(Collectors.joining(","));
                targetNameMap.put(fieldName,values);
            }
            rest.put(metadata.getSourceValue(),targetNameMap);
        }
        return rest;
    }

    /**
     * 查询数据库生成字典
     *
     * @param table           表
     * @param selectFields       回显字段
     * @param whereField      查询的字段
     * @param whereValueList  查询的值集合
     * @param param           查询的附加条件
     * @return 字典映射
     */
    private List<Map<String,Object>> executeQuery(String table,List<String> selectFields,String whereField,
                                                                   List<String> whereValueList, String param) {
        if (StringUtils.isEmpty(whereValueList)) {
            return new ArrayList<>();
        }
        // 将多个值拆分到集合中
        List<String> queryValueAll = whereValueList.stream()
                .flatMap(key -> StringUtils.split(key, ",").stream())
                .distinct()
                .collect(Collectors.toList());
        // 查询数据
        EasyVoBusDbRequest voBusDb = new EasyVoBusDbRequest();
        voBusDb.setTable(table);
        voBusDb.setSelectField(selectFields);
        voBusDb.setWhereField(whereField);
        voBusDb.setWhereValueList(queryValueAll);
        voBusDb.setParam(param);
        return dbService.getTableData(voBusDb);
    }
}