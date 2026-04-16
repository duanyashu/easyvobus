package com.github.duanyashu.easyvobus.model;

import java.util.List;

/**
 * rpc查询数据源参数
 *
 * @author duanyashu
 * 2026/4/13 8:19
 */
public class EasyVoBusRpcRequest {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名
     */
    String methodName;

    String tableName;

    /**
     * 条件字段
     */
    String whereField;
    /**
     * 查询条件值
     */
    List<String> whereValueList;

    /**
     * 查询的字段
     */
    List<String> selectFieldList;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getWhereField() {
        return whereField;
    }

    public void setWhereField(String whereField) {
        this.whereField = whereField;
    }

    public List<String> getWhereValueList() {
        return whereValueList;
    }

    public void setWhereValueList(List<String> whereValueList) {
        this.whereValueList = whereValueList;
    }

    public List<String> getSelectFieldList() {
        return selectFieldList;
    }

    public void setSelectFieldList(List<String> selectFieldList) {
        this.selectFieldList = selectFieldList;
    }
}