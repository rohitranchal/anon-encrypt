package org.ruchith.ae.peer;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.codehaus.jackson.node.ObjectNode;

public class TestPeer extends TestCase {

	public void testCreateContact() {
		Peer p = new Peer("Bob");
		ContactPrivateData privData = p.createContact("alice");
		ObjectNode out = privData.serializeJSON();
		ContactPrivateData tmpPrivateData = new ContactPrivateData(out);
		
		assertEquals(privData.getId().toString(), tmpPrivateData.getId().toString());
		assertEquals(privData.getKey().serializeJSON().toString(), tmpPrivateData.getKey().serializeJSON().toString());
		assertEquals(privData.getParams().serializeJSON().toString(), tmpPrivateData.getParams().serializeJSON().toString());
	}
	
	public void testTwoPeers() {
		Peer p = new Peer("p");
		
		ContactPrivateData alicePrivData = p.createContact("alice");
		Peer alice = new Peer("alice");
		alice.registerContact("p", alicePrivData);

		ContactPrivateData bobPrivData = p.createContact("bob");
		Peer bob = new Peer("bob");
		bob.registerContact("p", bobPrivData);
		
		String origMsg = "Attack";
		alice.addDirectMessage("p", origMsg);

		MessageRequest req = bob.generateRequest("p");
		
		MessageResponse resp = alice.generateResponse(req);
		
		String outputMsg = bob.processResponse(resp);
		assertEquals(origMsg, outputMsg);
	}
	
	public void test10Peers() {
		
		Peer p = new Peer("p");
		
		ArrayList<Peer> contacts = new ArrayList<Peer>();
		
		for(int i = 0; i < 10; i++) {
			ContactPrivateData tmpPrivData = p.createContact("alice");
			
			Peer tmp = new Peer("contact" + i);
			tmp.registerContact("p", tmpPrivData);
			contacts.add(tmp);
		}
		
		String orig = "Testing 123";
		Peer firstContact = contacts.get(0);
		firstContact.addDirectMessage("p", orig);
		
		for(int i = 1; i < 10; i++) {
			Peer tmpContact = contacts.get(i);
			MessageRequest req = tmpContact.generateRequest("p");
			MessageResponse resp = firstContact.generateResponse(req);
			String out = tmpContact.processResponse(resp);
			
			assertEquals(orig, out);
		}
		
	}
}
