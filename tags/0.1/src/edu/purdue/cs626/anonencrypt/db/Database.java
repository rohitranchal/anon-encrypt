package edu.purdue.cs626.anonencrypt.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import edu.purdue.cs626.anonencrypt.app.Constants;

public class Database {

	private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
	private static final String PROTOCOL = "jdbc:derby:";
	private static final String USER = "user1";
	private static final String PASSWORD = "user1";
	private static String DB_PATH;
	
	/**
	 * Singleton connection to the database.
	 */
	private static Connection conn;
	
	static {
		try {
			Class.forName(DRIVER).newInstance();
			String userHome = System.getProperty("user.home");
			String configDirPath = userHome + File.separator + Constants.CONFIG_DIR;
			DB_PATH = configDirPath + File.separator + Constants.DB_NAME;
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
	public static Connection getCreateConnection() throws Exception {
		Properties props = new Properties(); 
        props.put("user", USER);
        props.put("password", PASSWORD);
        
        return DriverManager.getConnection(PROTOCOL + DB_PATH
                + ";create=true", props);
	}
	
	/**
	 * Standard connection to the database.
	 * This is a singleton connection that is returned.
	 * @param dbPath Path including the database directory.
	 * @return A connection to the database.
	 * @throws Exception
	 */
	public static Connection getConnection() throws Exception {
		
		if(conn == null || conn.isClosed()) {
			Properties props = new Properties(); 
	        props.put("user", USER);
	        props.put("password", PASSWORD);
	        
	        conn = DriverManager.getConnection(PROTOCOL + DB_PATH, props);
		}
		return conn;
	}

}
