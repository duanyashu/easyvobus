package com.github.duanyashu.easyvobus.service.impl;

import com.github.duanyashu.easyvobus.model.EasyVoBusDbRequest;
import com.github.duanyashu.easyvobus.service.IEasyVoBusDbService;
import com.github.duanyashu.easyvobus.utils.sql.SqlUtil;

import java.util.List;
import java.util.Map;

/**
 * 默认数据库查询实现类
 *
 * @author duanyashu
 * 2026/4/10 9:38
 */
public class DefaultVoBusDbServiceImpl implements IEasyVoBusDbService {

    @Override
    public List<Map<String, Object>> getTableData(EasyVoBusDbRequest easyVoBusDbRequest) {
        // 查询数据库
        return SqlUtil.queryTableByValueMap(easyVoBusDbRequest.getTable(), easyVoBusDbRequest.getSelectField(), easyVoBusDbRequest.getWhereField(), easyVoBusDbRequest.getWhereValueList(), easyVoBusDbRequest.getParam());
    }
}