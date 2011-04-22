package edu.purdue.cs626.anonencrypt.app;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import edu.purdue.cs626.anonencrypt.db.Database;
import junit.framework.TestCase;

public class TestApplicationInstaller extends TestCase {

	public void testInstall() throws Exception {

		ApplicationInstaller installer = new ApplicationInstaller();
		installer.install();

		String userHome = System.getProperty("user.home");
		String dbPath = userHome + File.separator
				+ ApplicationInstaller.CONFIG_DIR + File.separator
				+ ApplicationInstaller.DB_NAME;
		
		Connection conn = Database.getConnection(dbPath);

		Statement s = conn.createStatement();
        
        s.execute("INSERT INTO Contact " +
        			"VALUES('Bob', '251789358979577744758182258194692528664', " 
        			+ "'324683896779935702435841186478040627037', '')");
        

        ResultSet rs = s.executeQuery("SELECT * FROM Contact");
        while(rs.next()) {
        	assertEquals("Bob", rs.getString(1));
        	assertEquals("251789358979577744758182258194692528664", rs.getString(2));
        	assertEquals("324683896779935702435841186478040627037", rs.getString(3));
        }
        
        //cleanup
        installer.unInstall();

		
	}
}
