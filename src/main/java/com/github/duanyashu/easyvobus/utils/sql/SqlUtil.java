package com.github.duanyashu.easyvobus.utils.sql;

import com.github.duanyashu.easyvobus.config.EasyVoBusSpringUtil;
import com.github.duanyashu.easyvobus.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * sql操作工具类
 *
 * @author ruoyi
 */
public class SqlUtil {

    private static final Log log = LogFactory.getLog(SqlUtil.class);
    private static JdbcTemplate JDBC_TEMPLATE = EasyVoBusSpringUtil.getBean(JdbcTemplate.class);

    /**
     * 根据值查询数据库表
     * @param table   表名
     * @param fieldsName  回显字段
     * @param queryField   查询字段
     * @param queryValue  查询值
     * @param param    附带参数
     * @return list 数据
     * @throws SQLException
     */
    public static List<Map<String,Object>> queryTableByValueMap(String table, List<String> fieldsName, String queryField, List<?> queryValue, String param){
        SqlQueryBuilder builder = generateSql(table, fieldsName, queryField, queryValue, param);

        // Spring 官方推荐 不过时写法（永久有效）
        return JDBC_TEMPLATE.query(
                builder.getSql(),
                // 新版写法：直接把参数放这里，不再用 Object[]
                ps -> {
                    Object[] params = builder.getParameters().toArray();
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }
                },
                (rs, rowNum) -> {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnLabel = metaData.getColumnLabel(i);
                        String fieldName = StringUtils.toCamelCase(columnLabel);
                        Object value = rs.getObject(i);
                        map.put(fieldName, value);
                    }
                    return map;
                }
        );
    }


    private static SqlQueryBuilder generateSql(String table, List<String> fieldsName, String queryField, List<?> queryValue, String param) {
        SqlQueryBuilder builder = new SqlQueryBuilder(table);

        if (StringUtils.isNotEmpty(fieldsName)) {
            builder.select(fieldsName);
        }

        // 1. 处理查询条件
        if (StringUtils.isNotEmpty(queryField) && queryValue != null && !queryValue.isEmpty()) {
            builder.select(queryField);
            builder.where(queryField,queryValue);
        }

        // 2. 处理附加参数（param直接作为条件）
        if (StringUtils.isNotEmpty(param)) {
            builder.whereParam(param);  // 例如："status = 'active'"
        }
        log.info("====> Preparing:"+builder.getSql());
        if (StringUtils.isNotEmpty(builder.getParameters())){
            StringJoiner sj = new StringJoiner(",");
            for (Object parameter : builder.getParameters()) {
                sj.add(parameter + "("+parameter.getClass().getSimpleName()+")");
            }
            log.info("====> Parameters:"+sj);
        }
        return builder;
    }

}