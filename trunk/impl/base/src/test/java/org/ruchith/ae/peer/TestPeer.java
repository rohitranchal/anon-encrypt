package org.ruchith.ae.peer;

import junit.framework.TestCase;

import org.codehaus.jackson.node.ObjectNode;

public class TestPeer extends TestCase {

	public void testCreateContact() {
		Peer p = new Peer();
		ContactPrivateData privData = p.createContact("alice");
		ObjectNode out = privData.serializeJSON();
		
		ContactPrivateData tmpPrivateData = new ContactPrivateData(out);
		
		assertEquals(privData.getId().toString(), tmpPrivateData.getId().toString());
	}
}
