package com.example.extract.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetSQLs {
    public static void main(String[] args) {
        File file = new File("D:\\ref_src\\plan_question");
        Map<String, String> codeMap = new HashMap<>();
        try {
            extractCodeMapFromSQL(file, codeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileUtils.writeStringToFile(JSON);
    }

    private static void extractCodeMapFromSQL(File file, Map<String, String> codeMap) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                extractCodeMapFromSQL(subFile, codeMap);
            }
        } else if (file.getName().endsWith(".xml")) {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            // 创建 Pattern 对象
            Pattern r = Pattern.compile("PKG_CSPG_COMMON\\.FUNC_GETVALUEBYCODE\\(T1\\.(\\w+),'(\\w+)'\\)");

            // 现在创建 matcher 对象
            Matcher m = r.matcher(text);
            if (m.find( )) {
                System.out.println(m.group(0)+":"+m.group(1)+":"+m.group(2));
            }
        }
    }
}
