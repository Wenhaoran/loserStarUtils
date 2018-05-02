package com.loserstar.utils.string;

import java.util.Collection;
import java.util.Iterator;


/**
 * 
 * author: loserStar
 * date: 2018年4月10日下午3:36:49
 * remarks:字符串处理
 */
public class LoserStarStringUtils {
	/**
	 * 判断字符串为null的话返回空字符串
	 * @param s
	 * @return
	 */
	public static String empty(String s) {
		return s==null?"":s;
	}
	
	/**
	 * 去掉所有空格
	 * @param s
	 * @return
	 */
	public static String removeSpace(String s) {
		return removeSpecifiedString(s, " ");
	}
	
	/**
	 * 把字符串中指定的字符剔除
	 * @param sourceStr 原字符串
	 * @param removeStr 需要剔除的字符串
	 * @return 剔除之后的最终结果
	 */
	public static String removeSpecifiedString(String sourceStr,String removeStr) {
		if (sourceStr!=null) {
			if (removeStr==null) {removeStr="";}
			return sourceStr.replace(removeStr, "");
		}else {
			return "";
		}
	}
	
	/**
	 * 去除字符串中开头的某个字符串
	 * @param sourceStr 原字符串
	 * @param removeStr 要剔除的第一个字符串
	 */
	public static String cutPrefix(String string,String prefix) {
		if (string.startsWith(prefix)) {
			string = string.substring(prefix.length());
		}
		return string;
	}
	/**
	 * 把一个集合以某个字符串分割输出，并且每一项添加个前缀和后缀
	 * form jodd StringUtil.join
	 * @param collection
	 * @param separator
	 * @param perfix 每一项的前缀
	 * @param suffix 每一项的后缀
	 * @return
	 */
	public static String join(final Collection collection,String separator,String perfix,String suffix) {
		if (collection == null) {
			return null;
		}

		if (collection.size() == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(collection.size() * 16);
		final Iterator it = collection.iterator();

		for (int i = 0; i < collection.size(); i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(perfix+it.next()+suffix);
		}
		return sb.toString();
	}
	
	/**
	 * 把一个数组以某个字符串分割输出，并且每一项添加个前缀和后缀
	 * form jodd StringUtil.join
	 * @param collection
	 * @param separator
	 * @param perfix 每一项的前缀
	 * @param suffix 每一项的后缀
	 * @return
	 */
	public static String join(final Object[] strings,String separator,String perfix,String suffix) {
		if (strings.length==0) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			if (i>0) {
				stringBuffer.append(separator);
			}
			stringBuffer.append(perfix+strings[i]+suffix);
		}
		return stringBuffer.toString();
	}
}