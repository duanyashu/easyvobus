
# EasyVoBus   数据摆渡车  零侵入 VO 自动填充框架

## 简介
EasyVoBus 是一款轻量级、易集成的关联字段自动填充框架，通过极简注解配置，即可根据字段值自动填充关联业务数据，支持数据库、字典、枚举、Map、RPC 远程服务五大数据源，专注解决 Controller 层接口响应数据的字段扩展、翻译、关联查询问题，大幅减少冗余代码开发。

![JDK8-21](https://img.shields.io/badge/JDK-8%2B~21-blue "JDK8-21")  ![Spring Boot 2.x / 3.x](https://img.shields.io/badge/Spring%20Boot-2.x%20%7C%203.x-green "Spring Boot 2.x / 3.x")  ![license apache 2.0](https://img.shields.io/badge/License-Apache%202.0-red "license apache 2.0")  ![release](https://img.shields.io/badge/Release-1.1.0-brightgreen "release")

## 🔥 最佳实践（推荐）
1. VO 层专用，不污染 DO 层
2. 列表查询统一使用 EasyVoBus 填充，不再手写关联 SQL
3. 高频字典使用 @BusDict，固定状态使用 @BusMap
4. 微服务跨服务使用 @BusRpc
5. 复杂业务继承 BusHandler 实现自定义填充

## 核心特性
- 零侵入：基于注解驱动，无需修改业务核心逻辑
- 多数据源：兼容数据库、字典、枚举、静态 Map、微服务 RPC
- 灵活控制：支持全局开启 / 关闭、单接口开启 / 关闭
- 自动填充：根据注解设置，批量填充关联字段
- 可扩展：核心业务逻辑支持自定义实现，适配不同项目架构
- 高性能：内置批量查询机制，避免循环查询数据库

## 快速上手（使用示例）
### 添加 maven 依赖
```xml
 <dependency>
    <groupId>com.github.duanyashu</groupId>
    <artifactId>easyvobus-spring-boot-starter</artifactId>
		<version>1.1.0</version>
</dependency>
```
### Controller 层使用
```java
@BusRun 未开启全局填充时，使用@BusRun开启数据填充
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 未开启全局填充时，使用@BusRun开启数据填充
     */
    //@BusRun 支持加到类上和方法上
    @GetMapping("/list")
    public List<UserVO> list() {
        // 业务查询数据
        return userService.listUser();
    }

    /**
     * 已开启全局填充时，使用@BusStop关闭填充
     */
    @BusStop
    @GetMapping("/simple")
    public List<UserVO> simpleList() {
        return userService.simpleList();
    }
}
```
### VO 注解写法
```java
@Data
public class UserVO {
    // 用户ID
    private Long id;

    // 用户名
    private String username;

    // 性别字典翻译 → 自动生成 sex_text
    @BusDict(value = "sys_user_sex")
    private Integer sex;

    // 状态枚举翻译 → 自动生成 status_text
    @BusEnum(StatusEnum.class)
    private Integer status;

    // 外键关联查询用户名称
    @BusDb(table = "sys_user", whereField = "id", selectField = "username")
    private Long createBy;

    // 静态Map翻译
    @BusMap(value = {"0=在职", "1=离职"})
    private Integer jobStatus;
}
```
### 最终返回 JSON 效果
```json
{
  "id": 1001,
  "username": "zhangsan",
  "sex": 1,
  "sex_text": "女",
  "status": 0,
  "status_text": "正常",
  "createBy": 1001,
  "createBy_username": "zhangsan",
  "jobStatus": 0,
  "jobStatus_text": "在职"
}
```

## 业务注解详解（@BusDb / @BusDict 等）
#### 所有字段注解支持组合使用、多注解联合使用，可同时配置多个数据源

### @BusDb 数据库表查询注解
`默认无需配置；也可自己实现IEasyVoBusDbService接口，实现自定义查询,具体操作看高级介绍`
#### 作用：直接根据字段查询数据库表，自动填充关联字段
```java
@BusDb(
        table = "sys_user",               // 数据库表名
        whereField = "user_id",                // 条件字段（外键）
        selectField = {"username", "nick_name"}, // 查询字段
        selectFieldAlias = {"userName", "realName"}, // 非必须 别名（与查询字段一一对应）
        param = "del_flag = 0",            //非必须 SQL附加条件（逻辑删除等）
        ruleField = "userType",     //非必须  配置了ruleValues，没有配置ruleField 默认当前字段 userId
        ruleValues = 1              //非必须 当前对象userType=1执行
)
private Long userId;
```

#### 注解属性：

| 属性  | 类型  | 说明  |
| ------------ | ------------ | ------------ |
| table  | String  | 数据库表名  |
| whereField  | String  | 查询条件字段  |
| selectField  | String[]  |  需要查询的字段数组 |
| selectFieldAlias	  | String[]  | 查询字段别名，不配置则自动生成：当前字段_查询字段  |
|  ruleField | String  | 条件执行字段（满足才执行填充）  |
| ruleValues  | String[]  |  条件执行值 |
| param  | String  |  SQL 自定义条件（如 del_flag=0） |

### @BusDict  系统字典注解
`需要实现IEasyVoBusDictService接口`
#### 字典查询实现
```java
@Component
public class EasyVoBusDictService implements IEasyVoBusDictService {
    @Override
    public List<EasyVoBusDict> getDictList(String dictType, List<String> list) {
        List<SysDictData> dictCache = DictUtils.getDictCache(dictType);
        return dictCache.stream().map(dict->{
            EasyVoBusDict voBusDict = new EasyVoBusDict();
            voBusDict.setLabel(dict.getDictLabel());
            voBusDict.setValue(dict.getDictValue());
            return voBusDict;
        }).collect(Collectors.toList());
    }
}

// 自动生成 sex_text 字段存储字典名称
@BusDict(value = "sys_user_sex")
private Integer sex;
```
#### 注解属性：
| 属性  | 类型  | 说明  |
| ------------ | ------------ | ------------ |
| value  | String  | 字典类型  |
| dictAlias	  | String  | 结果展示名称，默认：当前字段_text  |
|  ruleField | String  | 条件执行字段（满足才执行填充）  |
| ruleValues  | String[]  |  条件执行值 |


### @BusEnum  枚举数据源注解

#### 作用：根据枚举值翻译枚举文本，枚举需实现 EasyVoBusEnum 接口
```java
// 枚举实现接口
public enum StatusEnum implements EasyVoBusEnum {
    NORMAL(0, "正常"),
    DISABLE(1, "停用");

    private final Integer code;
    private final String desc;

    @Override
    public String getLabel() {
        return desc;
    }

    @Override
    public String getValue() {
        return code;
    }
}

    // 使用注解
    @BusEnum(StatusEnum.class)
    private Integer status;
```
#### 注解属性：
| 属性  | 类型  | 说明  |
| ------------ | ------------ | ------------ |
| value  | String  | 枚举类（必填）  |
| alias	  | String  | 展示名称，默认：当前字段_text  |
|  ruleField | String  | 条件执行字段  |
| ruleValues  | String[]  |  条件执行值 |


### @BusMap map数据源注解
`无需配置`
#### 作用：硬编码键值对翻译，适合固定不变的状态值
```java
@BusMap(value = {"0=男", "1=女", "2=未知"})
private Integer gender;
```
#### 注解属性
| 属性  | 类型  | 说明  |
| ------------ | ------------ | ------------ |
| value  | String  | 键值对数组（格式：key=value）  |
| alias	  | String  | 结果展示名称，默认：当前字段_text  |
|  ruleField | String  | 条件执行字段  |
| ruleValues  | String[]  |  条件执行值 |

### @BusRpc 微服务 RPC 调用注解
`需要配置 application.yml`
#### 作用：跨服务远程调用查询数据
```java
@BusRpc(
        service = "user-service",        // yml中配置的服务名
        table = "sys_user",              // 表名
        whereField = "id",               // 条件字段
        selectField = {"username"}       // 查询字段
)
private Long userId;
```
#### 注解属性：
| 属性  | 类型  | 说明  |
| ------------ | ------------ | ------------ |
| service  | String  | 服务名（与 yml 对应，必填）  |
| method  | String  | 远程方法名，默认 voBusSelectTable  |
| table  | String  | 数据库表名  |
| whereField  | String  | 查询条件字段  |
| selectField  | String[]  |  需要查询的字段数组 |
| selectFieldAlias	  | String[]  | 展示字段别名，需要和查询字段一一对应；默认名为：当前字段_查询字段  |
|  ruleField | String  | 条件执行字段（满足才执行填充）  |
| ruleValues  | String[]  |  条件执行值 |



## 执行控制注解

### @BusRun 执行注解
单独开启数据填充（未开启全局填充时使用，标注在 Controller类或方法上）
### @BusStop 停止执行
单独关闭数据填充（已开启全局填充时使用，标注在 Controller类或方法上）


## 高级用法（配置文件，多注解、自定义）
### 配置文件介绍
**核心配置（`application.yml`）全配置项均为可选，框架提供默认值**

支持自定义开关、后缀、RPC 服务地址
```yaml
easyvobus:
  enabled: true  #是否开启 默认true
  enable-global: true  #是否全局接口开启 默认false 接口单独添加注解@BusRun,开启之后接口上不用添加@BusRun
  vo-packages: com.xxx  #默认执行启动项目 @ComponentScan 所覆盖的包路径。
  null-value-show: true #为null的数据是否显示关联字段 默认false 例如：sex:null  开启之后会添加 sex_text:""
  default-suffix: _text # dict和enum 默认翻译后缀
  rpc: #rpc 注解的配置
    user-service:  #服务名
      #      url: [http://192.168.1.1:8081,http://192.168.1.2:8081]
      url: #服务地址
        - http://192.168.1.1:8081
        - http://192.168.1.2:8081
```
### 自定义数据源
EasyVoBus 支持高度自定义扩展，你可以通过实现系统提供的标准接口，替换默认数据源行为，适配项目自身的 ORM 框架、权限体系、微服务规范、缓存策略等。
支持自定义的数据源：
- BusDb 数据库查询（替换默认 SQL 执行逻辑）
- BusRpc 远程调用（自定义微服务接口规范）
#### BusDb数据库查询实现
适用场景：项目使用 MyBatis/MyBatis-Plus/JPA/Hibernate/ShardingSphere 或使用缓存等，不希望使用框架自带的 JDBC 查询。
实现方式
实现 IEasyVoBusDbService 接口，并重写数据查询方法。
``` java
public class DefaultVoBusDbServiceImpl implements IEasyVoBusDbService {

    @Override
    public List<Map<String, Object>> getTableData(EasyVoBusDbRequest easyVoBusDbRequest) {
        // 查询数据库
        return SqlUtil.queryTableByValueMap(easyVoBusDbRequest.getTable(), easyVoBusDbRequest.getSelectField(), easyVoBusDbRequest.getWhereField(), easyVoBusDbRequest.getWhereValueList(), easyVoBusDbRequest.getParam());
    }
}
```
#### BusRpc自定义查询的方法
适用场景：微服务架构下，提供标准的跨服务批量关联查询接口。

**服务提供方（被调用方）**
```java
@PostMapping("/voBusSelectTable")  // 对应methodName参数
    public List<Map<String, Object>> findByConditions(@RequestParam("tableName") String tableName,
                                                      @RequestParam("paramName") String paramName,
                                                      @RequestParam("paramValues") String paramValues,
                                                      @RequestParam("selectFields") String selectFields) {
        // 具体实现逻辑
        return SqlUtil.queryTableByValueMap(tableName, StringUtils.split(selectFields,","),paramName,StringUtils.split(paramValues,","),null);
    }
```
**配置文件**
```yml
easyvobus:
  rpc:
    user-service:
      url: http://localhost:8080/rpc
```
**使用**
```java
@BusRpc(service = "user-service", table = "sys_user", whereField = "id", selectField = "username")
private Long createBy;
```

### 多注解
EasyVoBus 所有业务注解均支持 **重复使用、多条件组合、多数据源联合**。
你可以在同一个字段上叠加多个 @BusDb / @BusDict / @BusRpc 等注解，实现动态数据源、多表关联、条件分支填充等高级业务场景。

#### 1.同一个字段使用多个 @BusDb（最常用）
适用场景：
根据不同业务条件，从不同表查询数据
根据 userType/status 等字段动态切换关联表
一个字段需要同时查询多张表的多个结果
```java
@BusDb(
        table = "sys_user",               // 数据库表名
        whereField = "user_id",                // 条件字段（外键）
        selectField = {"username", "nick_name"}, // 查询字段
        selectFieldAlias = {"userName", "realName"}, // 非必须 别名（与查询字段一一对应）
        param = "del_flag = 0",            //非必须 SQL附加条件（逻辑删除等）
        ruleField = "userType",     //非必须  配置了ruleValues，没有配置ruleField 默认当前字段 userId
        ruleValues = 1              //非必须 当前对象userType=1执行
)
@BusDb(
        table = "sys_user1",               // 数据库表名
        whereField = "user_id",                // 条件字段（外键）
        selectField = {"username", "nick_name"}, // 查询字段
        selectFieldAlias = {"userName", "realName"}, // 别名（与查询字段一一对应）
        param = "del_flag = 0",            // SQL附加条件（逻辑删除等）
        ruleField = "user_type",         //配置了ruleValues，没有配置ruleField 默认当前字段 userId
        ruleValues = 2               //当前对象userType=2执行
)
@BusDb(table = "sys_result",               // 数据库表名
        whereField = "user_id",
        selectField = "score")// selectFieldAlias不设置，默认名称：userId_sore
private Long userId;
```
#### 2,多注解混合使用（超强组合）
同一个字段，可同时使用 @BusDb + @BusDict + @BusRpc + ... 任意组合，一次性填充多个扩展字段。
```java
/**
 * 混合注解：一次查询，自动填充 用户名 + 部门名称 + 状态文本
 */
@BusDb(table = "sys_user", whereField = "id", selectField = "username")
@BusDb(table = "sys_dept", whereField = "id", selectField = "dept_name")
@BusDict(value = "user_status")
@BusRpc(service = "order-service", table = "order_info", selectField = "order_no")
private Long userId;
```

### 自定义注解 + 自定义数据源（高级扩展）
如果内置 5 种数据源不够用，你可以：

自定义注解（如 @BusDept、@BusUser）

实现 BusHandler 接口

完全自定义数据源逻辑（Redis/ES/Mongo/HTTP 等）
#### 1.先定义一个注解  示例@BusUser
```java

@Repeatable(BusUser.Container.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@BusAnnotation
public @interface BusUser {

    /**
     * 需要查询的字段
     */

    String[] selectField();

    /**
     * selectFiled映射字段
     */
    String[] selectFieldAlias() default {};


    /**
     * 执行本条注解的条件字段
     */
    String ruleField() default "";

    /**
     * 执行本条注解的条件值
     */
    String[] ruleValues() default {};
  
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @BusAnnotation
    public @interface Container {
        BusUser[] value();  // 必须叫 value，必须返回 Ferry 数组
    }
}
```
#### 2. 实现处理器BusHandler
```java
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
     * @param metadataList 需要查询的元数据列表
     * @return Map<源值, Map<字段名, 字段值>> 示例：1，nickname->张三
     */
    Map<Object, Map<String, Object>> batchCollectData(BusContext context, List<FieldMetadata> metadataList);
}


@Component
public class BusUserHandler implements BusHandler<BusUser> {


    @Override
    public Class<BusUser> supportAnnotation() {
        return BusUser.class;
    }

    @Override
    public List<String> getDictFieldNames(String fieldName, Annotation annotation) {
        BusUser busDb = (BusUser) annotation;
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
        Map<Object, Map<String, Object>> result = new HashMap<>();
            // 批量查询
            Map<Object, Map<String, Object>> tableResult = 自定义查询数据(metadataList);
            for (Map.Entry<Object, Map<String, Object>> entry : tableResult.entrySet()) {
                Object key = entry.getKey();
                Map<String, Object> newInnerMap = entry.getValue();
                if (result.containsKey(key)){
                    Map<String, Object> existingInnerMap = result.get(key);
                    existingInnerMap.putAll(newInnerMap);
                }else {
                    result.put(key,newInnerMap);
                }
            }
        return result;
		}
	}
```
## 常见问题
#### 1. 是否支持多值查询 / 关联？
>支持，支持英文逗号拼接的多值格式，可直接传入多个参数值实现批量关联 / 筛选，无需拆分多次请求。

#### 2. 是否支持多级嵌套查询？
>支持多级嵌套查询，层级适配规则：
>
>第一层：支持 List、Map、项目路径下对象 三种数据结构
第二层及后续层级：仅支持 项目下对象 和 List 集合

#### 3. 是否支持一对多查询？
>支持一对多数据查询，但当前一对多结果会以逗号拼接合并返回，暂不支持以多条list格式返回。

#### 4. 查询是每个字段数据查一次吗？
>不是，会先统一收集对象中所有带注解的查询需求，按查询表和查询条件分类聚合，相同表 + 相同条件仅执行一次查询，大幅减少数据库查询次数。

#### 5. 是否支持自定义注解？
>支持，可通过自定义注解实现功能扩展，具体实现方式请查阅「自定义注解」文档。

#### 6.  功能未生效 / 没起作用怎么办？
>请按以下步骤排查：
>
>    1.检查启动类路径或 scanBasePackages 是否包含需要处理的对象；
>
>    2.若未包含，需在 yml 配置文件中添加：easyvobus.vo-packages 扫描包配置；
>
>    3.使用 @BusDict 和 @BusEnum 注解时，必须手动实现 IEasyVoBusDictService 和 IEasyVoBusEnumService 接口，否则注解无法正常生效。

## 版本更新
- 1.1.0 支持 JDK 8 - 21
- 1.0.0 只支持 JDK 8