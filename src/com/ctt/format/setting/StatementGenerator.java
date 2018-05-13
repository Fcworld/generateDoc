package com.ctt.format.setting;

/**
 * Created by Administrator on 2018/5/11 0011.
 */
public class StatementGenerator {
    public static String defaultGetFormat = "/**\n" +
            " * 获取 #{bare_field_comment}\n" +
            " * \n" +
            " * @return ${field.name} #{bare_field_comment}  \n" +
            " */ ";
    public static String defaultSetFormat = "/**\n" +
            " * 设置 #{bare_field_comment}\n" +
            " * \n" +
            " * @param ${field.name} #{bare_field_comment}  \n" +
            " */ ";
}
