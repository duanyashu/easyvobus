package com.github.duanyashu.easyvobus.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.duanyashu.easyvobus.annotation.BusAnnotation;
import com.github.duanyashu.easyvobus.annotation.BusRun;
import com.github.duanyashu.easyvobus.annotation.BusStop;
import com.github.duanyashu.easyvobus.config.EasyVoBusProperties;
import com.github.duanyashu.easyvobus.config.SpringContextUtil;
import com.github.duanyashu.easyvobus.handler.BusHandler;
import com.github.duanyashu.easyvobus.model.BusContext;
import com.github.duanyashu.easyvobus.model.FieldMetadata;
import com.github.duanyashu.easyvobus.utils.ReflectUtils;
import com.github.duanyashu.easyvobus.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字典翻译处理类
 *
 * @author duanyashu
 * 2026/3/19 13:40
 */
@ControllerAdvice
public class VoBusAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(VoBusAdvice.class);

    private static Map<Class<?>, BusHandler<?>> handlerMap;

    @Resource
    List<BusHandler<?>> busHandlerList;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private EasyVoBusProperties easyVoBusProperties;

    public VoBusAdvice() {
    }

    @PostConstruct
    public void init() {
        if (busHandlerList != null && !busHandlerList.isEmpty()) {
            handlerMap = initHandleMap(busHandlerList);
        }
    }
    /**
     * 判断是否支持处理当前的返回值。
     * 返回true，则会执行beforeBodyWrite方法；返回false则跳过。
     *
     * @param returnType    返回类型
     * @param converterType 使用的消息转换器类型
     * @return 是否支持
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (handlerMap == null || handlerMap.isEmpty()) {
            return false;
        }
        if(returnType.hasMethodAnnotation(BusStop.class) || returnType.getContainingClass().isAnnotationPresent(BusStop.class)){
            return false;
        }
        return returnType.hasMethodAnnotation(BusRun.class) || returnType.getContainingClass().isAnnotationPresent(BusRun.class) || easyVoBusProperties.getEnableGlobal();
    }
    public Map<Class<?>, BusHandler<?>> initHandleMap(List<BusHandler<?>> handlers) {
        return handlers.stream().collect(Collectors.toMap( BusHandler::supportAnnotation,Function.identity()));
    }

    /**
     * 在响应体被写入之前执行。可以在此修改响应对象。
     *
     * @param body        控制器方法返回的对象（经过@JsonView处理后的对象）
     * @param returnType    返回类型
     * @param selectedContentType 客户端请求的MediaType
     * @param selectedConverterType 选中的消息转换器
     * @param request       当前请求
     * @param response      当前响应
     * @return 修改后的响应体对象，最终被序列化并返回给客户端
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null || !isProjectClass(body.getClass())){
            return body;
        }
        // 1. 初始化上下文
        BusContext context = new BusContext();
        context.setSourceObject(body);
        context.setCache(new HashMap<>());
        context.setProcessedObjects(new HashSet<>());
        context.setMetadataMap(new HashMap<>());

        if (body instanceof Map<?,?> ){
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>)body;
            return enrichMapWithDictText(context, bodyMap);
        }
        handleObj(body, context);
        return context.getResultMap();
    }

    private void handleObj(Object body, BusContext context) {
        // 使用 Jackson 将对象转换为 Map
        Map<String, Object> resultMap = objectMapper.convertValue(body, new TypeReference<Map<String, Object>>() {});
        context.setResultMap(resultMap);
        // 2. 递归扫描并收集 Bus 注解元数据
        scanAndCollect(context, body, resultMap, "");
        // 3. 按类型批量查询数据
        Map<Class<?>, Map<Object, Map<String, Object>>> allData = batchCollectData(context);
        // 4. 将查询结果填充到 resultMap 中
        fillResultMap(context, allData);
    }

    /**
     * 对象为自定义map处理
     * @param body
     * @return
     */
    private Map enrichMapWithDictText(BusContext context,Map<String,Object> body) {
        Map<String, Object> bodyMap = body;
        Iterator<Map.Entry<String, Object>> iterator = bodyMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                List<?>list = collection instanceof List ? (List<?>) collection : new ArrayList<>(collection);
                if (list.size() > 0 && isProjectClass(getNonNullClass(list))) {
                    List<Object> resultMapList = new ArrayList<>();
                    for (Object o : list) {
                        handleObj(o,context);
                        resultMapList.add(context.getResultMap());
                    }
                    bodyMap.put(key,resultMapList);
                }
            } else if (!ObjectUtils.isEmpty(value) && isProjectClass(value.getClass())) {
                handleObj(value,context);
                bodyMap.put(key, context.getResultMap());
            }
        }
        return bodyMap;
    }
    /**
     * 批量收集数据
     */
    private Map<Class<?>, Map<Object, Map<String, Object>>> batchCollectData(BusContext context) {
        Map<Class<?>, Map<Object, Map<String, Object>>> allData = new HashMap<>();
        for (Map.Entry<Class<?>, List<FieldMetadata>> entry : context.getMetadataMap().entrySet()) {
            Class<?> annotationType = entry.getKey();
            List<FieldMetadata> metadataList = entry.getValue();
            BusHandler<?> handler = handlerMap.get(annotationType);
            if (handler != null) {
                Map<Object, Map<String, Object>> data = handler.batchCollectData(context, metadataList);
                allData.put(annotationType, data);
            }
        }
        return allData;
    }

    /**
     * 填充结果 Map
     */
    private void fillResultMap(BusContext context, Map<Class<?>, Map<Object, Map<String, Object>>> allData) {
        for (Map.Entry<Class<?>, List<FieldMetadata>> entry : context.getMetadataMap().entrySet()) {
            Class<?> annotationType = entry.getKey();
            List<FieldMetadata> metadataList = entry.getValue();
            Map<Object, Map<String, Object>> dataMap = allData.get(annotationType);
            if (dataMap == null) {
                continue;
            }
            for (FieldMetadata metadata : metadataList) {
                Map<String, Object> data = dataMap.get(metadata.getSourceValue());
                if (data == null) {
                    continue;
                }
                // 获取当前字段所在的 Map
                Map<String, Object> targetMap = getTargetMap(context, metadata);
                if (targetMap == null) {
                    continue;
                }

                // 将查询结果添加到 Map 中
                for (String targetField : metadata.getTargetFieldNames()) {
                    Object value = data.get(targetField);
                    if (value != null) {
                        targetMap.put(targetField, value);
                    }
                }
            }
        }
    }

    /**
     * 获取目标 Map（根据字段路径）
     */
    private Map<String, Object> getTargetMap(BusContext context, FieldMetadata metadata) {
        String fieldPath = metadata.getFieldPath();
        if (fieldPath == null || fieldPath.isEmpty()) {
            return context.getResultMap();
        }

        // 解析字段路径，如 "items[0].userId" -> 找到 items[0] 对应的 Map
        String[] parts = fieldPath.split("\\.");
        Map<String, Object> currentMap = context.getResultMap();

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            // 处理数组索引，如 "items[0]"
            if (part.contains("[")) {
                String arrayName = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));

                Object arrayObj = currentMap.get(arrayName);
                if (arrayObj instanceof List) {
                    List<?> list = (List<?>) arrayObj;
                    if (index < list.size()) {
                        Object item = list.get(index);
                        if (item instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> itemMap = (Map<String, Object>) item;
                            currentMap = itemMap;
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                Object obj = currentMap.get(part);
                if (obj instanceof Map<?,?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> objMap = (Map<String, Object>) obj;
                    currentMap = objMap;
                } else {
                    return null;
                }
            }
        }

        return currentMap;
    }

    /**
     * 提取目标字段名
     */
    private List<String> extractTargetFields(String fieldName,BusHandler<?> handler,Annotation annotation) {
        return handler.getDictFieldNames(fieldName,annotation);
    }

    /**
     * 递归扫描收集 Bus 注解元数据
     * @param context 上下文
     * @param sourceObj 源对象
     * @param targetMap 目标 Map
     * @param pathPrefix 字段路径前缀
     */
    private void scanAndCollect(BusContext context, Object sourceObj,
                                Map<String, Object> targetMap, String pathPrefix) {
        if (sourceObj == null) {
            return;
        }
        // 避免循环引用
        if (context.getProcessedObjects().contains(sourceObj)) {
            return;
        }
        context.getProcessedObjects().add(sourceObj);
        Class<?> clazz = sourceObj.getClass();
        Field[] fields = ReflectUtils.getFields(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldPath = buildPath(pathPrefix, fieldName);

            try {
                Object fieldValue = field.get(sourceObj);
                // 1. 处理字段上的 Bus 注解
                processFieldAnnotations(context, sourceObj, field, fieldValue, targetMap, fieldPath);
                // 2. 递归处理嵌套对象
                if (fieldValue != null && (isProjectClass(field.getClass()) || Object.class == field.getType())) {
                    Object nestedTarget = targetMap.get(fieldName);
                    if (nestedTarget instanceof Map<?,?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> nestedTargetMap = (Map<String, Object>) nestedTarget;
                        scanAndCollect(context, fieldValue, nestedTargetMap, fieldPath);
                    }
                }
                // 3. 递归处理 List
                else if (fieldValue instanceof List) {
                    List<?> sourceList = (List<?>) fieldValue;
                    Object targetListObj = targetMap.get(fieldName);
                    if (targetListObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> targetList = (List<Map<String, Object>>) targetListObj;
                        for (int i = 0; i < sourceList.size() && i < targetList.size(); i++) {
                            Object item = sourceList.get(i);
                            Map<String, Object> itemMap = targetList.get(i);
                            if (item != null && isProjectClass(item.getClass())) {
                                String itemPath = fieldPath + "[" + i + "]";
                                scanAndCollect(context, item, itemMap, itemPath);
                            }
                        }
                    }
                }

            } catch (IllegalAccessException e) {
                log.debug("字段访问失败: {}", fieldName, e);
            }
        }
    }
    private String buildPath(String prefix, String fieldName) {
        return prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
    }
    /**
     * 处理字段上的 Bus 注解
     */
    private void processFieldAnnotations(BusContext context, Object sourceObj, Field field,
                                         Object fieldValue, Map<String, Object> targetMap,
                                         String fieldPath) {
        for (Annotation annotationAll : field.getDeclaredAnnotations()) {
            if (fieldValue == null && !easyVoBusProperties.getNullValueShow()) {
                continue;
            }
            if (!annotationAll.annotationType().isAnnotationPresent(BusAnnotation.class)){
                continue;
            }
            Class<? extends Annotation> annotationType = getRealRepeatableAnnotation(annotationAll.annotationType());
            BusHandler<?> handler = handlerMap.get(annotationType);
            if (handler == null) {
                continue;
            }
            //获取所有注解
            Annotation[] annotationArr = unwrapContainer(annotationAll);
            for (Annotation annotation : annotationArr) {
                if (!matchesRule(annotation, sourceObj, field.getName())){
                    continue;
                }
                // 提取目标字段名
                List<String> targetFields = extractTargetFields(field.getName(),handler,annotation);
                // 构建元数据
                FieldMetadata metadata = FieldMetadata.builder()
                        .fieldPath(fieldPath)
                        .sourceFieldName(field.getName())
                        .sourceValue(fieldValue)
                        .targetFieldNames(targetFields)
                        .annotation(annotation)
                        .targetMap(targetMap)  // 目标 Map，用于直接填充
                        .currentObj(sourceObj)
                        .build();
                context.getMetadataMap()
                        .computeIfAbsent(annotationType, k -> new ArrayList<>())
                        .add(metadata);
            }

        }
    }

    private Annotation[] unwrapContainer(Annotation annotation) {
        Class<?> annotationType = annotation.annotationType();
        if (!annotationType.isAnnotationPresent(BusAnnotation.class)){
            return new Annotation[]{};
        }
        try {
            Method valueMethod = annotationType.getMethod("value");
            Object value = valueMethod.invoke(annotation);

            if (value instanceof Annotation[] && ((Annotation[]) value).length > 0) {
                return (Annotation[]) value;
            }
        } catch (Exception e) {
        }
        return new Annotation[]{annotation};
    }
    /**
     * 带容器注解，获取真实注解
     */
    public static Class<? extends Annotation> getRealRepeatableAnnotation(Class<? extends Annotation> annoClass) {
        // 空值直接返回
        if (annoClass == null) {
            return null;
        }
        // 1. 有 @Repeatable → 本身就是真实注解
        if (annoClass.isAnnotationPresent(Repeatable.class)) {
            return annoClass;
        }
        // 2. 是容器注解 → 获取数组里的真实类型
        try {
            Method valueMethod = annoClass.getMethod("value");
            Class<?> returnType = valueMethod.getReturnType();
            if (returnType.isArray()&& Annotation.class.isAssignableFrom(returnType.getComponentType())) {
                return returnType.getComponentType().asSubclass(Annotation.class);
            }
        } catch (NoSuchMethodException e) {
            // 忽略，不是容器
        }
        // 3. 都不是 → 返回自身
        return annoClass;
    }

    /**
     * 匹配规则
     * @param annotation  注解
     * @param currentObj  当前对象
     * @param sourceFieldName  原字段名
     * @return
     */
    private boolean matchesRule(Annotation annotation,Object currentObj,String sourceFieldName) {
        if (annotation == null){
            return false;
        }
        String[] ruleValues = getAnnotationAttribute(annotation, "ruleValues", String[].class);
        if (ruleValues == null || ruleValues.length == 0) {return true;}

        String ruleField = getAnnotationAttribute(annotation, "ruleField", String.class);
        if (ruleField == null || ruleField.isEmpty()) {
            ruleField = sourceFieldName;
        }
        if (currentObj == null) {
            return false;
        }
        Object fieldValue = ReflectUtils.getFieldValue(currentObj, ruleField);
        return Arrays.asList(ruleValues).contains(fieldValue);
    }


    // 通用的获取注解属性方法
    private static <T> T getAnnotationAttribute(Annotation annotation, String attributeName, Class<T> attributeType) {
        try {
            Method method = annotation.annotationType().getMethod(attributeName);
            Object value = method.invoke(annotation);
            return attributeType.cast(value);
        } catch (Exception e) {
            // 返回默认值
            if (attributeType == String.class) {
                return attributeType.cast("");
            } else if (attributeType == String[].class) {
                return attributeType.cast(new String[0]);
            }
            return null;
        }
    }
    /**
     * 获取集合中不为空的对象
     * @param rows
     * @return class
     */
    public static Class<?> getNonNullClass(List<?> rows) {
        return rows.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(Object::getClass)
                .orElse(null);
    }
    /**
     * 是否项目对象
     */
    private boolean isProjectClass(Class<?> clz) {
        Set<String> componentScanPackages = SpringContextUtil.componentScanPackages;
        List<String> voPackages = easyVoBusProperties.getVoPackages();
        if (!StringUtils.isEmpty(voPackages)){
            componentScanPackages.addAll(voPackages);
        }
        String name = clz.getName();
        return componentScanPackages.stream().filter(scanPack -> name.startsWith(scanPack)).count() > 0 && !Enum.class.isAssignableFrom(clz) ;
    }

}