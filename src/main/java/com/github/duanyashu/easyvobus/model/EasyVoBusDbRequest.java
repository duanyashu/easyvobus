package com.github.duanyashu.easyvobus.model;

import java.util.List;

/**
 * 表查询数据源参数
 *
 * @author duanyashu
 * 2026/4/13 8:19
 */
public class EasyVoBusDbRequest {
    /**
     * 表名
     */
    private String table;

    /**
     * 查询字段
     */
    private List<String> selectField;

    /**
     * 条件字段
     */
    String whereField;

    /**
     * 查询的条件值
     */
    List<String> whereValueList;

    /**
     * 附带查询条件sql语句
     */
    String param;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getSelectField() {
        return selectField;
    }

    public void setSelectField(List<String> selectField) {
        this.selectField = selectField;
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

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}