package com.github.duanyashu.easyvobus.config;

import java.util.List;

/**
 * rpc 配置类
 *
 * @author duanyashu
 * 2026/4/14 15:54
 */
public class EasyVoBusRpcProperties {

    private String serviceName;

    private List<String> url;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getUrl() {
        return url;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }
}