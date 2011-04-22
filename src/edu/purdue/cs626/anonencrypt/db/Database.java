package edu.purdue.cs626.anonencrypt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class Database {

	private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
	private static final String PROTOCOL = "jdbc:derby:";
	private static final String USER = "user1";
	private static final String PASSWORD = "user1";
	
	/**
	 * Singleton connection to the database.
	 */
	private static Connection conn;
	
	static {
		try {
			Class.forName(DRIVER).newInstance();
		} catch (Exception e) {
			System.out.println("FETAL ERROR: Cannot load Derby driver!");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private Database() {}
	
	public static Connection getCreateConnection(String dbPath) throws Exception {
		Properties props = new Properties(); 
        props.put("user", USER);
        props.put("password", PASSWORD);
        
        return DriverManager.getConnection(PROTOCOL + dbPath
                + ";create=true", props);
	}
	
	public static Connection getConnection(String dbPath) throws Exception {
		if(conn == null) {
			Properties props = new Properties(); 
	        props.put("user", USER);
	        props.put("password", PASSWORD);
	        
	        conn = DriverManager.getConnection(PROTOCOL + dbPath, props);
		}
		return conn;
	}
	
	public void create() throws Exception {
		Properties props = new Properties(); 
        props.put("user", USER);
        props.put("password", PASSWORD);
        String dbName = "derbyDB";
        
        Connection conn = DriverManager.getConnection(PROTOCOL + dbName
                + ";create=true", props);
//        Connection conn = DriverManager.getConnection(protocol + dbName , props);
        
        Statement s = conn.createStatement();
        
        s.execute("CREATE TABLE Contact(friendId varchar(100), " +
        				"id varchar(512), random varchar(512), privData clob)");
        
        s.execute("INSERT INTO Contact " +
        			"VALUES('Bob', '251789358979577744758182258194692528664', " +
        			"'324683896779935702435841186478040627037', '')");
        
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
