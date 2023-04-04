package com.github.kingschan1204.easycrawl.core.variable;

import com.github.kingschan1204.easycrawl.core.variable.impl.TimeStampExpression;
import com.github.kingschan1204.easycrawl.helper.regex.RegexHelper;
import com.github.kingschan1204.easycrawl.helper.map.MapUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanVariable {

    private static Map<String, Expression> elMap;

    static {
        elMap = new HashMap<>();
        elMap.put("timestamp", new TimeStampExpression());
    }

    /**
     * 扫描并解析变量
     * @param text 要扫描的文本
     * @param map 传入的参数
     * @return
     */
    public static String parser(String text, Map<String, Object> map) {
        List<String> exps = RegexHelper.find(text, "\\$\\{(\\w|\\s|\\=)+\\}");
        for (String el : exps) {
            String[] els = el.replaceAll("[${}]", "").split("\\s+");
            String tag = els[0];
            Map<String, String> argsMap = new HashMap<>();
            if (els.length > 1) {
                for (int i = 1; i < els.length; i++) {
                    String[] token = els[i].split("=");
                    argsMap.put(token[0], token[1]);
                }
            }
//            System.out.println(String.format("tag:%s 参数:%s", tag, argsMap));
            if (elMap.containsKey(tag)) {
                text = text.replace(el, elMap.get(tag).execute(argsMap));
            }
            if (null!= map && map.containsKey(tag)) {
                text = text.replace(el, String.valueOf(map.get(tag)));
            }
        }
        return text;
    }

    public static void main(String[] args) {
        String text = "https://xueqiu.com/service/screener/screen?category=CN&exchange=sh_sz&areacode=&indcode=&order_by=symbol&order=desc&page=${page}&size=${pageSize}&only_count=0&current=&pct=&mc=&volume=&_=${timestamp}";
        System.out.println(ScanVariable.parser(text,
                new MapUtil<String,Object>()
                        .put("page","1")
                        .put("pageSize","300")
                        .getMap()));
    }

}