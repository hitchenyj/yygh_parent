package com.atguigu.yygh.hosp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-05 8:20
 */
public class HttpRequestHelper {

    public static Map<String, Object> switchMap(Map<String, String[]> parameterMap) {
        Map<String, Object> resultMap = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            resultMap.put(key, value);
        }

        return resultMap;
    }

}
