package com.zhengsr.socketlib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author by  zhengshaorui on 2019/9/25
 * Describe:
 */
public class StringUtils {
    public static String removeBlank(String str){
        String dest = "";
        if (str!=null) {
            Pattern pattern = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = pattern.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
