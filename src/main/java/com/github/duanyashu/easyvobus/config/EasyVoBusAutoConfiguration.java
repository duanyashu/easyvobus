package com.github.duanyashu.easyvobus.config;

import com.github.duanyashu.easyvobus.core.VoBusAdvice;
import com.github.duanyashu.easyvobus.handler.impl.*;
import com.github.duanyashu.easyvobus.service.IEasyVoBusDbService;
import com.github.duanyashu.easyvobus.service.IEasyVoBusRpcService;
import com.github.duanyashu.easyvobus.service.impl.DefaultVoBusDbServiceImpl;
import com.github.duanyashu.easyvobus.service.impl.rpc.client.DynamicRpcClientFactory;
import com.github.duanyashu.easyvobus.service.impl.rpc.controller.DefultVoBusRpcController;
import com.github.duanyashu.easyvobus.service.impl.rpc.service.RestTemplateRpcServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 全局配置类
 *
 * @author duanyashu
 * 2026/4/7 13:58
 */
@Configuration
@EnableConfigurationProperties(com.github.duanyashu.easyvobus.config.EasyVoBusProperties.class)
@ConditionalOnProperty(prefix = "easyvobus", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyVoBusAutoConfiguration {

    @Bean
    public DefultVoBusRpcController defultVoBusRpcController(){
        return new DefultVoBusRpcController();
    }


    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时：3秒
        factory.setConnectTimeout(3000);
        return new RestTemplate(factory);
    }

    @Bean
    @ConditionalOnBean(RestTemplate.class)
    public DynamicRpcClientFactory dynamicFeignClientFactory(){
        return new DynamicRpcClientFactory();
    }
    @Bean
    @ConditionalOnBean(RestTemplate.class)
    public IEasyVoBusRpcService feignVoBusRpcService(){
        return new RestTemplateRpcServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IEasyVoBusDbService.class)
    public IEasyVoBusDbService easyVoBusDbService(){
        return new DefaultVoBusDbServiceImpl();
    }

    @Bean
    public BusDbHandler busDbHandler() {
        return new BusDbHandler();
    }
    @Bean
    public BusMapHandler busMapHandler() {
        return new BusMapHandler();
    }
    @Bean
    public BusEnumHandler busEnumHandler() {
        return new BusEnumHandler();
    }
    @Bean
    public BusDictHandler busDictHandler() {
        return new BusDictHandler();
    }
    @Bean
    public BusRpcHandler busRpcHandler() {
        return new BusRpcHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    public VoBusAdvice ferryResponseAdvice() {
        return new VoBusAdvice();
    }

}