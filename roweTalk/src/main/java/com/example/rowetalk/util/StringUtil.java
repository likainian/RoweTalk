
package com.example.rowetalk.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 字符串工具类
 * 
 * @author Administrator
 */
public class StringUtil {

    /**
     * 判断空
     * 
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        if (str.length() < 1) {
            return true;
        }
        return false;
    }

    /**
     * 判断s空
     * 
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        if (str == null) {
            return false;
        }
        if (str.equals("")) {
            return false;
        }
        if (str.length() < 1) {
            return false;
        }

        if (str.equals("null")) {
            return false;
        }
        if (str.equals("Null")) {
            return false;
        }
        if (str.equals("NULL")) {
            return false;
        }

        return true;
    }

    /**
     * 判断空或者为 字符串 "null"
     * 
     * @param str
     * @return
     */
    public static boolean isEmptyOrNull(String str) {
        if (str == null) {
            return true;
        }
        if (str.length() < 1) {
            return true;
        }
        if (str.equals("null")) {
            return true;
        }
        if (str.equals("Null")) {
            return true;
        }
        if (str.equals("NULL")) {
            return true;
        }
        return false;
    }

    /**
     * URLEnCode UTF-8
     * 
     * @param res
     * @return
     */
    public static String URLEncode2UTF8(String res) {
        try {
            return URLEncoder.encode(res, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean inStringList(List<String> list, String name){
		if(list==null || list.size()==0) return false;
		for(String s: list){
			if(s.equals(name)){
				return true;
			}
		}
		return false;
	}
}
