package edu.purdue.cs626.anonencrypt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class Database {

	private String driver = "org.apache.derby.jdbc.ClientDriver";
	private String protocol = "jdbc:derby:";
	
	
	public Database() throws Exception {
        Class.forName(driver).newInstance();
	}
	
	public void create() throws Exception {
		Properties props = new Properties(); 
        props.put("user", "user1");
        props.put("password", "user1");
        String dbName = "derbyDB";
        
        Connection conn = DriverManager.getConnection(protocol + dbName
                + ";create=true", props);
//        Connection conn = DriverManager.getConnection(protocol + dbName , props);
        
        Statement s = conn.createStatement();
        
        s.execute("CREATE TABLE Contact(id varchar(512), random varchar(512))");
        
        s.execute("INSERT INTO Contact VALUES('251789358979577744758182258194692528664', '324683896779935702435841186478040627037')");
        
        conn.commit();
        
        ResultSet rs = s.executeQuery("SELECT * FROM Contact");
        while(rs.next()) {
        	System.out.println(rs.getString(1));
        	System.out.println(rs.getString(2));
        }

	}
	
	public static void main(String[] args) throws Exception {
		Database db = new Database();
		db.create();
		
	}
}
