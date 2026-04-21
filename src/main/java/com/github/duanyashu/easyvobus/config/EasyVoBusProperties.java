package com.github.duanyashu.easyvobus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * EasyVoBus 自动配置类
 * <p>
 * 用于绑定 application.yml 或 application.properties 中以 "easy-vobus" 为前缀的配置项。
 * 支持以下配置项：
 * <ul>
 *     <li>enabled - 全局开关，控制整个 EasyVoBus 功能是否启用</li>
 *     <li>enable-global - 全局拦截开关，是否拦截所有 SpringMVC 的 @ResponseBody 返回结果</li>
 *     <li>null-value-show - 空值显示开关，控制值为 null 时是否显示</li>
 *     <li>default-suffix - 默认后缀，用于 VO 字段转换时的默认后缀名称</li>
 * </ul>
 * </p>
 * @author duanyashu
 * 2026/4/7 14:02
 */
@ConfigurationProperties(prefix = "easyvobus")
public class EasyVoBusProperties {

    /**
     * EasyVoBus 全局开关
     * <p>
     * 控制整个 EasyVoBus 功能是否启用。当设置为 false 时，所有 EasyVoBus 相关功能都将失效。
     * </p>
     * <p>
     * <b>默认值：</b>true
     * </p>
     * <p>
     * <b>配置方式：</b> easy-vobus.enabled=false
     * </p>
     */
    private Boolean enabled = true;

    /**
     * 全局拦截开关（拦截所有 SpringMVC 的 @ResponseBody 返回结果）
     * <p>
     * 当设置为 true 时，会自动拦截所有 Controller 中带有 @ResponseBody 注解或 @RestController 的返回结果，
     * 统一进行 VoBus 转换处理，无需在每个方法上单独添加 @EnableEasyVoBus 注解。
     * </p>
     * <p>
     * <b>默认值：</b>false
     * </p>
     * <p>
     * <b>配置方式：</b> easy-vobus.enable-global=true
     * </p>
     * <p>
     * <b>注意：</b> 仅在 enabled = true 时此配置才生效
     * </p>
     */
    private Boolean enableGlobal = false;

    /**
     * 要扫描的 Vo 的包名列表
     * 如果不设置，默认扫描项目 @ComponentScan 所覆盖的包路径。
     * <p>
     * 框架只会执行配置包下的注解。
     * </p>
     * <p>
     * <b>配置方式：</b> easy-vobus.scan-packages=com.example.converter,com.example.vo
     * </p>
     */
    private List<String> voPackages;

    /**
     * 空值显示开关
     * <p>
     * 控制当转换的字段值为 null 时，是否在结果中显示该字段。
     * </p>
     * <ul>
     *     <li>true - 显示 null 值字段</li>
     *     <li>false - 不显示 null 值字段（推荐，可减少响应体积）</li>
     * </ul>
     * <p>
     * <b>默认值：</b>false
     * </p>
     * <p>
     * <b>配置方式：</b> easy-vobus.null-value-show=true
     * </p>
     * <p>
     * <b>示例：</b>
     * <pre>
     * // nullValueShow = false 时，返回结果中不包含 null 字段
     * {"userId": null}  // age 字段为 null，不返回
     *
     * // nullValueShow = true 时，返回结果中包含 null 字段
     * {"userId": null, "userId_text": ""}
     * </pre>
     * </p>
     */
    private Boolean nullValueShow = false;

    /**
     * 默认后缀名称
     * <p>
     * 当使用BusEnum,BusMap,BusDict注解时，如果未指定自定义后缀，则使用此默认后缀。
     * 例如：sex 关联字段名为 sex_text
     * </p>
     * <p>
     * <b>默认值：</b>_text
     * </p>
     * <p>
     * <b>配置方式：</b> easy-vobus.default-suffix=_label
     * </p>
     */
    private String defaultSuffix = "_text";

    private Map<String, EasyVoBusRpcProperties> rpc;

    public Map<String, EasyVoBusRpcProperties> getRpc() {
        return rpc;
    }

    public void setRpc(Map<String, EasyVoBusRpcProperties> rpc) {
        this.rpc = rpc;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnableGlobal() {
        return enableGlobal;
    }

    public void setEnableGlobal(Boolean enableGlobal) {
        this.enableGlobal = enableGlobal;
    }

    public List<String> getVoPackages() {
        return voPackages;
    }

    public void setVoPackages(List<String> voPackages) {
        this.voPackages = voPackages;
    }

    public Boolean getNullValueShow() {
        return nullValueShow;
    }

    public void setNullValueShow(boolean nullValueShow) {
        this.nullValueShow = nullValueShow;
    }

    public String getDefaultSuffix() {
        return defaultSuffix;
    }

    public void setDefaultSuffix(String defaultSuffix) {
        this.defaultSuffix = defaultSuffix;
    }
}