package com.example.extract.utils;


import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetSQLs {
    public static void main(String[] args) throws IOException {
        File file = new File("D:\\ref_src\\新建文件夹");
        Map<String, String> typeCodeMap = new HashMap<>();
        try {
            extractCodeMapFromSQL(file, typeCodeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileUtils.writeStringToFile(new File("C:/src/typeCodeMap.txt"), JSON.toJSONString(typeCodeMap), Charset.forName("utf-8"));
    }

    private static void extractCodeMapFromSQL(File file, Map<String, String> typeCodeMap) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                extractCodeMapFromSQL(subFile, typeCodeMap);
            }
        } else if (file.getName().endsWith(".xml")) {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            // 创建 Pattern 对象
            Pattern r = Pattern.compile("PKG_CSPG_COMMON\\.FUNC_GETVALUEBYCODE\\(T1\\.(\\w+)\\s*,\\s*'(\\w+)'\\)(?=([\\s\\S]*?([ \r\n](\\w+) T1)))");

            // 现在创建 matcher 对象
            Matcher m = r.matcher(text);
            while (m.find()) {
                System.out.println(m.group(0));
                System.out.println(m.group(1) + ":" + m.group(2) + ":" + m.group(5));
                typeCodeMap.put(m.group(5).toUpperCase() + "|::|" + m.group(1).toUpperCase(), m.group(2));
            }
        }
    }
}
