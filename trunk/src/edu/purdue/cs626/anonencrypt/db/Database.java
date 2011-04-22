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
	
	/**
	 * Create a connection to create the database.
	 * @param dbPath Path including the database directory.
	 * @return A connection to the database.
	 * @throws Exception
	 */
	public static Connection getCreateConnection(String dbPath) throws Exception {
		Properties props = new Properties(); 
        props.put("user", USER);
        props.put("password", PASSWORD);
        
        return DriverManager.getConnection(PROTOCOL + dbPath
                + ";create=true", props);
	}
	
	/**
	 * Standard connection to the database.
	 * This is a singleton connection that is returned.
	 * @param dbPath Path including the database directory.
	 * @return A connection to the database.
	 * @throws Exception
	 */
	public static Connection getConnection(String dbPath) throws Exception {
		
		if(conn == null || conn.isClosed()) {
			Properties props = new Properties(); 
	        props.put("user", USER);
	        props.put("password", PASSWORD);
	        
	        conn = DriverManager.getConnection(PROTOCOL + dbPath, props);
		}
		return conn;
	}

}
