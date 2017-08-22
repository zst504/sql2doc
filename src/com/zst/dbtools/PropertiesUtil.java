package com.zst.dbtools;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class PropertiesUtil {
    private static Properties pro = null;

    static {
        InputStream in = PropertiesUtil.class.getResourceAsStream("/init.properties");
        try {
            pro = new Properties();
            pro.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ��ϵͳ�����ж�ȡ��Ӧ������
     *
     * @param name//������
     * @param defaultValue//Ĭ��ֵ
     * @return �����Զ�Ӧ��ֵ
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public static String get(String name, String defaultValue) {
        if (pro == null) return defaultValue;
        else if (!pro.containsKey(name)) return defaultValue;
        else {
            try {
                return new String(pro.getProperty(name).getBytes("iso_8859_1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                return defaultValue;
			}
		}
	}    
}
