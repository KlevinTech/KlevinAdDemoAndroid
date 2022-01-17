package com.tencent.klevinDemo.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumUtil {

    public static boolean isNumeric(String str){
        if (TextUtils.isEmpty(str)) return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

}
