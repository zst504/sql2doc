package com.zst.dbtools;


import org.apache.log4j.Logger;

import java.sql.*;

public class BaseConnection{
	private final Logger log = Logger.getLogger(this.getClass());
    public Connection conn;




	public Connection getConnection() throws Exception{
        try {
        	Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/roam?characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&zeroDateTimeBehavior=convertToNull","root","123456");   
            return conn;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



    public void colseConnection(){
        try {
            if(conn!=null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Statement getStatement() throws Exception{
        try {            
            Statement statement = getConnection().createStatement();
            return statement;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String ExStatement(String className,String connect,String user,String psw,String sql){
        try{
            Connection con = getConnection();
            Statement statement = con.createStatement();
            boolean flag = statement.execute(sql);
            statement.close();
            con.close();
            if(flag)return"执行成功！";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "执行失败！";
    }

    public ResultSet getResultSetBySql(String sql){
        try{
            
            
            ResultSet rs = getStatement().executeQuery(sql);
            return rs;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

   

}

