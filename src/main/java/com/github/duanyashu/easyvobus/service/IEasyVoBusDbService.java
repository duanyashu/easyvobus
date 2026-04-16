package com.github.duanyashu.easyvobus.service;

import com.github.duanyashu.easyvobus.model.EasyVoBusDbRequest;

import java.util.List;
import java.util.Map;

/**
 * 查询表数据接口
 *
 * @author duanyashu
 * 2026/4/10 9:33
 */
public interface IEasyVoBusDbService {

    /**
     * 获取表数据方法
     * @param easyVoBusDbRequest  表名
     * @return list数据
     */
    List<Map<String,Object>> getTableData(EasyVoBusDbRequest easyVoBusDbRequest);
}