package com.zst.dbtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

	private static Properties properties = new Properties();

	//private static String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();	//本地测试用
	private static String path = (new File("res")).getAbsolutePath();	//现网打包用

	/**
	 * 加载properties文件
	 */
	public static String loadProperty(String fileName, String propertyName) {
		String s = null;
		try {
			properties.load(new FileInputStream(path + "/" + fileName));
			s = properties.getProperty(propertyName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static File loadFile(String fileName){
		File file = new File(path + "/" + fileName);
		return file;
	}
	
	public static void main(String args[]) {
		String s = PropertiesUtil.loadProperty("init.properties", "db.type");
		System.out.println(s);
	}
}