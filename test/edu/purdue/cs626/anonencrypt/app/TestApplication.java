package edu.purdue.cs626.anonencrypt.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.axiom.om.util.Base64;

import edu.purdue.cs626.anonencrypt.db.Database;

import junit.framework.TestCase;

/**
 * Testing main application API behavior.
 * 
 * @author Ruchith Fernando
 *
 */
public class TestApplication extends TestCase {

	private ApplicationInstaller installer;
	
	@Override
	protected void setUp() throws Exception {
		
		this.installer = new ApplicationInstaller();
		this.installer.install();
		
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		
		this.installer.unInstall();
		
		super.tearDown();
	}

	public void testCreateContact() throws Exception {
		Application app = new Application();
		ContactPrivData data = app.createContact("Bob");
		
		//Probe the DB to check the inserted values
		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM Contact");
		String user ="";
		String id = "";
		while(rs.next()) {
			user = rs.getString(1);
			id = rs.getString(2);
		}
		
		assertEquals("Bob", user);
		assertEquals(Base64.encode(data.getId().toBytes()), id);
		
	}
}
