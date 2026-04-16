package com.github.duanyashu.easyvobus.utils.sql;


import java.util.*;

/**
 * sql构建工具类
 *
 * @author duanyashu
 * 2026/2/3 11:41
 */
public class SqlQueryBuilder {
    private final String table;
    private final List<String> selectFields = new ArrayList<>();
    private final Set<String> whereClauses = new HashSet<>();
    private final List<Object> parameters = new ArrayList<>();

    public SqlQueryBuilder(String table) {
        this.table = table;
    }

    /**
     * 设置查询字段
     */
    public SqlQueryBuilder select(String... fields) {
        this.selectFields.addAll(Arrays.asList(fields));
        return this;
    }

    public SqlQueryBuilder select(List<String> fields) {
        this.selectFields.addAll(fields);
        return this;
    }

    /**
     * 添加WHERE条件（等值条件）
     * @param field 字段名
     * @param value 字段值
     */
    public SqlQueryBuilder where(String field, Object value) {
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (!collection.isEmpty()) {
                if (collection.size()==1){
                    whereClauses.add(field + " = ?");
                }else {
                    String placeholders = String.join(",",
                            Collections.nCopies(collection.size(), "?"));
                    whereClauses.add(field + " IN (" + placeholders + ")");
                }
                parameters.addAll(collection);
            }
        } else {
            whereClauses.add(field + " = ?");
            parameters.add(value);
        }
        return this;
    }
    /**
     * 添加WHERE附带条件
     * @param param 附带条件sql
     */
    public SqlQueryBuilder whereParam(String param) {
        whereClauses.add(param);
        return this;
    }


    public String getSql() {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (selectFields.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(",", selectFields));
        }

        sql.append(" FROM ").append(table);

        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ")
                    .append(String.join(" AND ", whereClauses));
        }

        return sql.toString();
    }

    public List<Object> getParameters() {
        return parameters;
    }
}