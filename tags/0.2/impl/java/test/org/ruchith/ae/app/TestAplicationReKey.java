package org.ruchith.ae.app;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.ruchith.ae.app.Application;
import org.ruchith.ae.app.ApplicationInstaller;
import org.ruchith.ae.app.ContactPrivData;
import org.ruchith.ae.base.ReKeyInformation;

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
		
		for(int i = 0; i < 3; i++) {
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
		
		//Update Bob1's info
		
		boolean result = app.processReKey("Bob1", publish);
		assertTrue(result);
		
	}
}
