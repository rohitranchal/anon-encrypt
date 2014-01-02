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
		assertEquals(privData.getKey().serializeJSON().toString(), tmpPrivateData.getKey().serializeJSON().toString());
		assertEquals(privData.getParams().serializeJSON().toString(), tmpPrivateData.getParams().serializeJSON().toString());
	}
	
	public void testTwoPeers() {
		Peer p = new Peer();
		
		ContactPrivateData alicePrivData = p.createContact("alice");
		Peer alice = new Peer();
		alice.registerContact("p", alicePrivData);

		ContactPrivateData bobPrivData = p.createContact("alice");
		Peer bob = new Peer();
		bob.registerContact("p", bobPrivData);
		
		alice.addDirectMessage("p", "Attack");

		MessageRequest req = bob.generateRequest("p");
		
		System.out.println(req.serializeJSON());

	}
}
