package com.github.duanyashu.easyvobus.handler.impl;

import com.github.duanyashu.easyvobus.annotation.BusRpc;
import com.github.duanyashu.easyvobus.handler.BusHandler;
import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.EasyVoBusRpcRequest;
import com.github.duanyashu.easyvobus.model.FieldMetadata;
import com.github.duanyashu.easyvobus.service.IEasyVoBusRpcService;
import com.github.duanyashu.easyvobus.utils.ListUtils;
import com.github.duanyashu.easyvobus.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BusRpc关联处理器
 *
 * @author duanyashu
 * 2026/4/8 14:34
 */
@Component
public class BusRpcHandler implements BusHandler<BusRpc> {
    @Autowired
    private Optional<IEasyVoBusRpcService> easyVoBusRpcService;
    @Override
    public Class<BusRpc> supportAnnotation() {
        return BusRpc.class;
    }

    @Override
    public List<String> getDictFieldNames(String fieldName, Annotation annotation) {
        BusRpc busMap = (BusRpc) annotation;
        String[] dictTexts = busMap.selectField();
        String[] dictFieldNames = busMap.selectFieldAlias();
        // 检查长度是否匹配
        if (dictTexts.length != dictFieldNames.length) {
            return ListUtils.list(dictTexts).stream()
                    .map(text -> fieldName + "_" + StringUtils.toCamelCase(text))
                    .collect(Collectors.toList());
        }

        return ListUtils.list(dictFieldNames);
    }

    @Override
    public Map<Object, Map<String, Object>> batchCollectData(BusContext context,List<FieldMetadata> metadataList) {
        if (!easyVoBusRpcService.isPresent()){
            return new HashMap<>();
        }
        // 按服务名+方法名组批量查询
        Map<String, List<FieldMetadata>> groupByTable = metadataList.stream()
                .collect(Collectors.groupingBy(m -> {
                    BusRpc busRpc = (BusRpc) m.getAnnotation();
                    return busRpc.service()+","+busRpc.method();
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
        BusRpc annotation = (BusRpc)metadataList.get(0) .getAnnotation();
        List<String> queryValueList = metadataList.stream().flatMap(metadata->StringUtils.split(StringUtils.toStr(metadata.getSourceValue()), ",").stream()).collect(Collectors.toList());
        List<Map<String,Object>> list = executeQuery(annotation.service(),annotation.method(),annotation.table(), ListUtils.list(annotation.selectField()), annotation.whereField(), queryValueList);
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
     * @param service           服务名
     * @param method           方法名
     * @param table           表名
     * @param textFields       回显字段
     * @param whereField      查询的字段
     * @param queryValueList  查询的值集合
     * @return 字典映射
     */
    private List<Map<String,Object>> executeQuery(String service,String method,String table,List<String> textFields,String whereField,List<String> queryValueList) {
        if (StringUtils.isEmpty(queryValueList)) {
            return new ArrayList<>();
        }
        // 将多个值拆分到集合中
        List<String> queryValueAll = queryValueList.stream()
                .flatMap(key -> StringUtils.split(key, ",").stream())
                .distinct()
                .collect(Collectors.toList());
        // 查询数据
        EasyVoBusRpcRequest voBusRpcRequest = new EasyVoBusRpcRequest();
        voBusRpcRequest.setServiceName(service);
        voBusRpcRequest.setTableName(table);
        voBusRpcRequest.setMethodName(method);
        voBusRpcRequest.setWhereField(whereField);
        voBusRpcRequest.setWhereValueList(queryValueAll);
        voBusRpcRequest.setSelectFieldList(textFields);
        return easyVoBusRpcService.get().invoke(voBusRpcRequest);
    }
}