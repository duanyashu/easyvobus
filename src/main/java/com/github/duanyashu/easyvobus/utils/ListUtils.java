package com.github.duanyashu.easyvobus.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * list 工具类
 *
 * @author duanyashu
 * 2026/4/10 17:23
 */
public class ListUtils {


    @SafeVarargs
    public static <T> List<T> list(T... values) {
        if (values == null || values.length == 0) {
            return new ArrayList<>();
        }
        final List<T> arrayList = new ArrayList<>(values.length);
        Collections.addAll(arrayList, values);
        return arrayList;
    }
}