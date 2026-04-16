package com.github.duanyashu.easyvobus.service.impl.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RPC 客户端接口
 *
 * @author duanyashu
 * 2026/4/15 8:22
 */
public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private List<String> serviceUrlList;
    private RestTemplate restTemplate;
    public RestClient(List<String> serviceUrlList, RestTemplate restTemplate) {
        this.serviceUrlList = serviceUrlList;
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> invoke(String methodName,
                                            String tableName,
                                            String paramName,
                                            String paramValues,
                                            String selectFields) {
        // 构建URL和参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("tableName", tableName);
        params.add("paramName", paramName);
        params.add("paramValues", paramValues);
        params.add("selectFields", selectFields);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request =  new HttpEntity<>(params, headers);

        List<String> urlsToTry = new ArrayList<>(serviceUrlList);
        Exception lastException = null;

        for (int i = 0; i < urlsToTry.size(); i++) {
            String serviceUrl = urlsToTry.get(i);
            String url = serviceUrl + "/" + methodName;
            try {
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );
                if (response.getStatusCode().is2xxSuccessful()) {
                    // 成功：更新优先级（当前URL移到最前）
                    updateUrlPriority(serviceUrl);
                    return response.getBody();
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("RPC请求失败 [{}/{}]: {}", i + 1, urlsToTry.size(), url);
                // 将失败的URL移到末尾
                if (urlsToTry.size() > 1) {
                    urlsToTry.remove(i);
                    urlsToTry.add(serviceUrl);
                    i--; // 调整索引
                }
            }
        }
        // 全部失败
        serviceUrlList = urlsToTry;
        log.error("所有RPC地址均请求失败，共{}个地址", urlsToTry.size(), lastException);
        return new ArrayList<>();
    }

    private synchronized void updateUrlPriority(String successUrl) {
        List<String> newList = new ArrayList<>(serviceUrlList);
        newList.remove(successUrl);
        newList.add(0, successUrl);  // 成功的放到第一位
        serviceUrlList = newList;
    }
}