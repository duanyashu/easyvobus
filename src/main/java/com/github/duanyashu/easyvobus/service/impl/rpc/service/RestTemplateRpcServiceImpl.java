package com.github.duanyashu.easyvobus.service.impl.rpc.service;

import com.github.duanyashu.easyvobus.model.EasyVoBusRpcRequest;
import com.github.duanyashu.easyvobus.service.IEasyVoBusRpcService;
import com.github.duanyashu.easyvobus.service.impl.rpc.client.DynamicRpcClientFactory;
import com.github.duanyashu.easyvobus.service.impl.rpc.client.RestClient;
import com.github.duanyashu.easyvobus.utils.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * rest RPC服务类
 *
 * @author duanyashu
 * 2026/4/13 17:28
 */
public class RestTemplateRpcServiceImpl implements IEasyVoBusRpcService {

    @Resource
    private DynamicRpcClientFactory clientFactory;

    @Override
    public List<Map<String, Object>> invoke(EasyVoBusRpcRequest easyVoBusRpcRequest) {
        // 动态获取或创建 Feign Client
        RestClient client = clientFactory.getClient(easyVoBusRpcRequest.getServiceName());
        if (client == null){
            return new ArrayList<>();
        }
        // 调用服务
        return client.invoke(easyVoBusRpcRequest.getMethodName(),
                easyVoBusRpcRequest.getTableName(),
                easyVoBusRpcRequest.getWhereField(),
                StringUtils.join(easyVoBusRpcRequest.getWhereValueList(),","),
                StringUtils.join(easyVoBusRpcRequest.getSelectFieldList(),","));
    }

}