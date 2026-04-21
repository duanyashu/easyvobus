package com.github.duanyashu.easyvobus.service.impl.rpc.client;

import com.github.duanyashu.easyvobus.config.EasyVoBusProperties;
import com.github.duanyashu.easyvobus.config.EasyVoBusRpcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC客户端工厂类
 *
 * @author duanyashu
 * 2026/4/13 17:33
 */
public class DynamicRpcClientFactory {

    private final Map<String, RestClient> clientCache = new ConcurrentHashMap<>();
    @Autowired
    private Optional<RestTemplate> restTemplate;
    @Autowired
    private EasyVoBusProperties easyVoBusProperties;

    public RestClient getClient(String serviceName) {
        if (!restTemplate.isPresent()){
            return null;
        }
        return clientCache.computeIfAbsent(serviceName, name -> {
            EasyVoBusRpcProperties easyVoBusRpcProperties = getRpcProperties(name);
            List<String> serviceUrlList = easyVoBusRpcProperties.getUrl();
            return new RestClient(serviceUrlList, restTemplate.get());
        });
    }

    private EasyVoBusRpcProperties getRpcProperties(String serviceName) {
        Map<String, EasyVoBusRpcProperties> rpcMap = easyVoBusProperties.getRpc();
        if (rpcMap == null || !rpcMap.containsKey(serviceName)) {
            throw new RuntimeException("Service URL not configured for: " + serviceName +
                    ". Please configure easy-vobus.rpc." + serviceName + ".url");
        }

        EasyVoBusRpcProperties properties = rpcMap.get(serviceName);
        if (properties.getUrl() == null || properties.getUrl().isEmpty()) {
            throw new RuntimeException("URL not configured for service: " + serviceName);
        }

        return properties;
    }
}