package com.github.duanyashu.easyvobus.enums;

/**
 * 枚举数据源基础类
 * @author duanyashu
 */
public interface EasyVoBusEnum {

    /**
     * 字段
     */
    String getLabel();

    /**
     * 值
     */
    String getValue();


    static EasyVoBusEnum getEnumData(Class<? extends EasyVoBusEnum> enumClass, Object code) {
        if (enumClass == null || code == null) {
            return null;
        }
        String codeStr = String.valueOf(code);
        for (EasyVoBusEnum e : enumClass.getEnumConstants()) {
            if (e.getValue().equals(codeStr)) {
                return new EasyVoBusEnum() {
                    @Override
                    public String getLabel() {
                        return e.getLabel();
                    }

                    @Override
                    public String getValue() {
                        return e.getValue();
                    }
                };
            }
        }
        return null;
    }
}