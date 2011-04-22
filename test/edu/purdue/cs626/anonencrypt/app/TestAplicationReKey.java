package edu.purdue.cs626.anonencrypt.app;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import edu.purdue.cs626.anonencrypt.ReKeyInformation;
import junit.framework.TestCase;

public class TestAplicationReKey extends TestCase {

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
	
	public void testReKey() throws Exception {
		Application app = new Application();
		
		for(int i = 0; i < 20; i++) {
			ContactPrivData data = app.createContact("Bob" + i);
			// now register the contact using the same private information
			app.registerContact("Bob" + i, data);
			
		}
		
		ReKeyInformation info = app.reKey();

		String publish = info.serialize();
		System.out.println(publish);
		
		
		StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(publish.getBytes()));
		ReKeyInformation newInfo = new ReKeyInformation(builder.getDocumentElement(), app.getParams().getPairing());
		
		assertEquals(info.getG1(), newInfo.getG1());
	}
}
