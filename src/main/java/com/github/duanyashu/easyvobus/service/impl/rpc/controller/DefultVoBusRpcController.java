package com.github.duanyashu.easyvobus.service.impl.rpc.controller;

import com.github.duanyashu.easyvobus.utils.StringUtils;
import com.github.duanyashu.easyvobus.utils.sql.SqlUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 默认rpc 查询数据接口
 *
 * @author duanyashu
 * 2026/4/14 9:11
 */
@RestController
public class DefultVoBusRpcController {

    @PostMapping("/voBusSelectTable")  // 对应methodName参数
    public List<Map<String, Object>> findByConditions(@RequestParam("tableName") String tableName,
                                                      @RequestParam("paramName") String paramName,
                                                      @RequestParam("paramValues") String paramValues,
                                                      @RequestParam("selectFields") String selectFields) {
        // 具体实现逻辑
        return SqlUtil.queryTableByValueMap(tableName, StringUtils.split(selectFields,","),paramName,StringUtils.split(paramValues,","),null);
    }
}