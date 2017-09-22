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
	 * 数据库表信息导出成word文档
	 * @return
	 * @throws Exception
	 */
	public String exportReport() throws Exception{
		dbName = PropertiesUtil.get("database.name", null);
		//查询sql，需要导出的表信息
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
		//如果查询结果不为空则开始导出word
		if(rm!=null){
			//根据查询结果组织生成WORD文档需要的数据格式
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
			//最后一次循环的数据没有放到list/map中，把数据放入list/map
			Map<String,Object> tableMap=new HashMap<String,Object>();
			tableMap.put("tableName", tableName);
			tableMap.put("columns", columns);
			dataList.add(tableMap);
			//导出word文档中传入模板的必须是map格式的
			Map map = new HashMap();
			map.put("dList", dataList);
			//模板配置
			Configuration configuration = new Configuration();  
			configuration.setDefaultEncoding("UTF-8");
			Template t=null;  
			String path = (new File("res")).getAbsolutePath();
			try {  
				configuration.setDirectoryForTemplateLoading(new File(path));//FTL文件所存在的位置  
				t = configuration.getTemplate("数据库表结构.ftl","UTF-8"); //根据路径和文件名加载模板  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
			File outFile = new File(path+"/数据库表结构"+Math.random()*10000+".doc");  //生成word文档名称及存放路径
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
