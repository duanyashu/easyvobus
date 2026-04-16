package com.github.duanyashu.easyvobus.service;

import com.github.duanyashu.easyvobus.model.EasyVoBusRpcRequest;

import java.util.List;
import java.util.Map;

/**
 * rpc服务接口
 *
 * @author duanyashu
 * 2026/4/10 14:17
 */
public interface IEasyVoBusRpcService {

    /**
     * 调用 RPC 服务
     * @param easyVoBusRpcRequest easyVoBusRpcRequest
     * @return 返回结果
     */
    List<Map<String, Object>> invoke(EasyVoBusRpcRequest easyVoBusRpcRequest);
}