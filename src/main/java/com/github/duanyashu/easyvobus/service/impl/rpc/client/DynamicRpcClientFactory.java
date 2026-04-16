package com.github.duanyashu.easyvobus.service.impl.rpc.client;

import com.github.duanyashu.easyvobus.config.EasyVoBusProperties;
import com.github.duanyashu.easyvobus.config.RpcProperties;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
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
    @Resource
    private Optional<RestTemplate> restTemplate;
    @Resource
    private EasyVoBusProperties easyVoBusProperties;

    public RestClient getClient(String serviceName) {
        if (!restTemplate.isPresent()){
            return null;
        }
        return clientCache.computeIfAbsent(serviceName, name -> {
            RpcProperties rpcProperties = getRpcProperties(name);
            List<String> serviceUrlList = rpcProperties.getUrl();
            return new RestClient(serviceUrlList, restTemplate.get());
        });
    }

    private RpcProperties getRpcProperties(String serviceName) {
        Map<String, RpcProperties> rpcMap = easyVoBusProperties.getRpc();
        if (rpcMap == null || !rpcMap.containsKey(serviceName)) {
            throw new RuntimeException("Service URL not configured for: " + serviceName +
                    ". Please configure easy-vobus.rpc." + serviceName + ".url");
        }

        RpcProperties properties = rpcMap.get(serviceName);
        if (properties.getUrl() == null || properties.getUrl().isEmpty()) {
            throw new RuntimeException("URL not configured for service: " + serviceName);
        }

        return properties;
    }
}