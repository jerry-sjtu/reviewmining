/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date 2012-9-28 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.dataAccessLayer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.utility.Const;

public class RDBConnection {
	public static String dbDriver;
	public static String url;
	public static String login;
	public static String password;
	public Connection connection = null;
	public Statement st = null;
	private static Logger logger = Logger.getLogger(MongoDB.class.getName());
	static
	{
		Properties properties = new Properties();
		try {

			String path = Const.MYSQL_CONFIG_FILE;
			InputStream in = new FileInputStream(path);
			properties.load(in);
			dbDriver = properties.getProperty("dbDriver") ;
			System.out.println(dbDriver);
			assert dbDriver != null && !dbDriver.trim().isEmpty() : "dbDriver is null";
			
			url = properties.getProperty("url");
			System.out.println(url);
			assert url != null && !url.trim().isEmpty() : "url is null";
			
			login = properties.getProperty("login");
			System.out.println(login);
			assert login != null && !login.trim().isEmpty() : "login is null";
			
			password = properties.getProperty("password");
			System.out.println(password);
			assert password != null && !password.trim().isEmpty() : "password is null";	
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.fatal("could not find mongo property file");
		}
	}
	
	public RDBConnection(){
		initialize();
	}
	
	
	public RDBConnection(String dbDriver, String url, String login, String password){
		this.dbDriver = dbDriver;
		this.url = url;
		this.login = login;
		this.password = password;
		if(!initialize())
//			throw new RuntimeException("DB connection init error");
			System.err.println("DB connection init error");
	}

	public static void main(String[] args)
	{
		System.out.println(dbDriver);
		System.out.println(url);
		System.out.println(login);
		System.out.println(password);
	}
	
	public boolean initialize(){
		try{
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(url, login, password);
			
			st = connection.createStatement();
		}
		catch(java.lang.ClassNotFoundException e){
			System.err.println("initialize error!"+e.toString());
			return false;
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		return true;
	}

	public void sqlclose(){
		try{
			if(st != null)	st.close();
			if(connection != null)	connection.close();
		}
		catch(SQLException ex){
			System.err.println("sqlclose :  "+ ex.getMessage());
		}
	}
	
	
	public ResultSet executeQuery(String sql){
		ResultSet rs = null;
		try{
			rs = st.executeQuery(sql);
		}
		catch (Exception ex){
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	    return rs;
	}
	
	public boolean executeUpdate(String sql){
		try{
			if(st.executeUpdate(sql) > 0){
				return true;
			}
			else{
				return false;
	         }
		}
		catch (SQLException ex){
			ex.printStackTrace();
			return false;
		}
	}
}
