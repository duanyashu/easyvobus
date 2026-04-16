package com.github.duanyashu.easyvobus.model;

/**
 * 字典查询数据源参数
 *
 * @author duanyashu
 * 2026/4/13 8:19
 */
public class EasyVoBusDict {
    /**
     * 标签
     */
    private String label;

    /**
     * 值
     */
    String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}