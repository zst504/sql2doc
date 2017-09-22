package com.zst.dbtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.Connection;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ReportManager {
	private BaseConnection db = new BaseConnection();
	private JdbcUtil jdbc = new JdbcUtil();
	
	private Connection con = null;
	
	private String dbName = "";
	
	/**
	 * ���ݿ����Ϣ������word�ĵ�
	 * @return
	 * @throws Exception
	 */
	public String exportReport() throws Exception{
		dbName = PropertiesUtil.get("database.name", null);
		//��ѯsql����Ҫ�����ı���Ϣ
		String sql = "SELECT"
				  + " column_name,"
				  + " column_type,"
				  + " is_nullable,"
				  + " column_default,"
				  + " ordinal_position,"
				  + " column_comment,"
				  + " table_name"
				+ " FROM"
				  + " INFORMATION_SCHEMA.Columns"
				+ " WHERE table_schema = '" + dbName + "'";
		con = db.getConnection();
		List[] rm = jdbc.getDataListBySQL(con, sql, null);
		List<Map> columns = null;
		String tableName = null;
		List<Map> dataList = new ArrayList<Map>();
		//�����ѯ�����Ϊ����ʼ����word
		if(rm!=null){
			//���ݲ�ѯ�����֯����WORD�ĵ���Ҫ�����ݸ�ʽ
			for(int i=0;i<rm[1].size();i++){
				Map res = (Map)rm[1].get(i);
				if(tableName!=null&&tableName.equals(res.get("table_name"))){
					tableName = res.get("table_name").toString();
					columns.add(res);
				}else{
					if(tableName!=null){
						Map<String,Object> tableMap=new HashMap<String,Object>();
						tableMap.put("tableName", tableName);
						tableMap.put("columns", columns);
						dataList.add(tableMap);
					}
					columns = new ArrayList<Map>();
					tableName = res.get("table_name").toString();
					columns.add(res);
				}
			}
			//���һ��ѭ��������û�зŵ�list/map�У������ݷ���list/map
			Map<String,Object> tableMap=new HashMap<String,Object>();
			tableMap.put("tableName", tableName);
			tableMap.put("columns", columns);
			dataList.add(tableMap);
			//����word�ĵ��д���ģ��ı�����map��ʽ��
			Map map = new HashMap();
			map.put("dList", dataList);
			//ģ������
			Configuration configuration = new Configuration();  
			configuration.setDefaultEncoding("UTF-8");
			Template t=null;  
			String path = (new File("res")).getAbsolutePath();
			try {  
				configuration.setDirectoryForTemplateLoading(new File(path));//FTL�ļ������ڵ�λ��  
				t = configuration.getTemplate("���ݿ��ṹ.ftl","UTF-8"); //����·�����ļ�������ģ��  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
			File outFile = new File(path+"/���ݿ��ṹ"+Math.random()*10000+".doc");  //����word�ĵ����Ƽ����·��
			Writer out = null;  
			try {  
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));  
			} catch (Exception e1) {  
				e1.printStackTrace();  
			}  
			
			try {  
				t.process(map, out);  
			} catch (TemplateException e) {  
				e.printStackTrace();  
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {
				try {
					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return outFile.getAbsolutePath();
		}else{
			return null;
		}
        
	}
	
	public static void main(String[] args){
		ReportManager rm = new ReportManager();
		try{
			rm.exportReport();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
