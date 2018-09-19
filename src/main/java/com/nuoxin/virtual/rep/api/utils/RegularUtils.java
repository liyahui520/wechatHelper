package com.nuoxin.virtual.rep.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 *
 * @author tiancun
 */
public class RegularUtils {

    //excel文件扩展名校验
    public static final String EXTENSION_XLS = "xls";

    public static final String EXTENSION_XLSX = "xlsx";

    //手机号验证
    public static final String MATCH_TELEPHONE = "^1\\d{10}$";

    public static final String MATCH_ELEVEN_NUM = "(?<!\\d)(?:(?:1\\d{10}))";

    /**
     * @param regEx 正则表达式
     * @param text  要匹配的文本
     * @return
     */
    public static Matcher getMatcher(String regEx, String text) {
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(text);
        return matcher;
    }


    /**
     * @param regEx 正则表达式
     * @param text  要匹配的文本
     * @return
     */
    public static boolean isMatcher(String regEx, String text) {
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(text);
        boolean matches = matcher.matches();
        return matches;
    }



    public static void main(String[] args) {

        String s = "白小明18121278141";


        //Pattern p = Pattern.compile("^.*\\d{11}.*$");
        //Pattern pattern = Pattern.compile("(?<!\\d)(?:(?:1\\d{10}))");
        Pattern p = Pattern.compile("(?<!\\d)(?:(?:1\\d{10}))");
        Matcher m = p.matcher(s);
        while (m.find()){
            System.out.println(m.group());
        }


    }



//    public static void main(String[] args) {
//
//        String s = "我的手机号是18837112195，曾经用过18888888888，还用过18812345678";
//        String regex = "1[35789]\\d{9}";
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(s);
//
//        while (m.find()) { //一定需要先查找再调用group获取电话号码
//            System.out.println(m.group());
//        }
//    }



}
