package com.github.duanyashu.easyvobus.service;

import com.github.duanyashu.easyvobus.model.EasyVoBusDict;

import java.util.List;

/**
 * 字典服务接口
 *
 * @author duanyashu
 * 2026/4/10 9:25
 */
public interface IEasyVoBusDictService {

    /**
     * 获取 字典翻译
     * @param dictType  字典类型
     * @param valueList  字典值
     * @return value->label映射的结果
     */
    List<EasyVoBusDict> getDictList(String dictType, List<String> valueList);
}