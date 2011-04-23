package edu.purdue.cs626.anonencrypt.app;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;

import edu.purdue.cs626.anonencrypt.ReKeyInformation;
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

	public void testCreateContactAndRegister() throws Exception {
		Application app = new Application();
		ContactPrivData data = app.createContact("Bob");

		// now register the contact using the same private information
		app.registerContact("Bob", data);

		// Probe the DB to check the inserted values
		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM Contact");
		String user = "";
		String id = "";
		String myIDFromContact = "";
		String privDatafromContact = "";

		while (rs.next()) {
			user = rs.getString(1);
			id = rs.getString(2);
			privDatafromContact = rs.getString(4);
			myIDFromContact = rs.getString(5);
		}

		assertEquals("Bob", user);
		assertEquals(Base64.encode(data.getId().toBytes()), id);

		assertEquals(Base64.encode(data.getId().toBytes()), myIDFromContact);

		StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(
				privDatafromContact.getBytes()));

		ContactPrivData dataFromContact = new ContactPrivData(builder.getDocumentElement());
		assertEquals(data.getId(), dataFromContact.getId());
		assertEquals(data.getParams().getCurveParams(), dataFromContact.getParams().getCurveParams());
		
		//test saving a msg from Bob
		String msg = "Hello!!!";
		app.saveMessage("Bob", msg);
		
		rs = s.executeQuery("SELECT lastMsg FROM Contact WHERE contactId = 'Bob'");
		rs.next();
		String val = rs.getString(1);
		assertEquals(msg, val);
		
		
		UpdateRequest ur = app.getUpdateRequest("Bob");
		String responseString = app.getUpdate(ur.serialize());
		
		String updateMsg = app.processUpdateResponse(responseString);
		
		assertEquals(msg.trim(), updateMsg);
		
	}
	
}
