package org.ruchith.ae.peer;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.util.encoders.Base64;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.RootKeyGen;

/**
 * Completely in-memory implementation
 * 
 * @author ruchith
 */
public class Peer {

	private AEParameters params;
	private Element masterKey;
	
	private HashMap<String, Contact> contacts = new HashMap<>();
	private HashMap<String, ContactPrivateData> privData = new HashMap<>();
	private HashMap<String, ArrayList<String>> messages = new HashMap<>();
	
	private HashMap<String, HashMap<Element, AEPrivateKey>> tmpKeyList = new HashMap<>(); 
	
	public Peer() {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		
		params = paramGen.generateParameters();
		this.masterKey = paramGen.getMasterKey();
		
	}

	/**
	 * Create a contact and return its private key
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ContactPrivateData createContact(String name) {

		// Generate a key with a random ID
		RootKeyGen rkg = new RootKeyGen();
		rkg.init(this.params);
		Field zr = this.params.getPairing().getZr();
		Element id1 = zr.newRandomElement().getImmutable();
		Element r = zr.newRandomElement().getImmutable();
		AEPrivateKey contactKey = rkg.genKey(id1, this.masterKey, r);
		
		Contact contact = new Contact();
		contact.setName(name);
		contact.setId(id1);
		contact.setR(r);
		
		this.contacts.put(name, contact);
		
		return new ContactPrivateData(id1, contactKey, params);
	}


	/**
	 * Add information provided by a remote contact.
	 * @param name
	 * @param privateData
	 */
	public void registerContact(String name, ContactPrivateData privateData) {
		this.privData.put(name, privateData);
	}


	/**
	 * Add a message directly received from a peer.
	 * @param user Name of the peer.
	 * @param message The message.
	 */
	public void addDirectMessage(String user, String message) {
		ArrayList<String> msgs = this.messages.get(user);
		if(msgs == null) {
			msgs = new ArrayList<String>();
			this.messages.put(user, msgs);
		}
		
		msgs.add(message);
	}
	
	public MessageRequest generateRequest(String user) {
		ContactPrivateData cpd = privData.get(user);
		
		ContactKeyGen keyGen = new ContactKeyGen();
		keyGen.init(cpd.getId(), cpd.getKey(), cpd.getParams());
		
		Element rndId = keyGen.genRandomID();
		AEPrivateKey privKey = keyGen.getTmpPrivKey(rndId);
		Element tmpPubKey = keyGen.getTmpPubKey(rndId);

		//Store private key
		HashMap<Element, AEPrivateKey> tmpKeyMap = this.tmpKeyList.get(user);
		if(tmpKeyMap == null) {
			tmpKeyMap = new HashMap<Element, AEPrivateKey>();
		}
		tmpKeyMap.put(tmpPubKey, privKey);
		
		String tmpPubKeyStr = new String(Base64.encode(tmpPubKey.toBytes()));
		
		return new MessageRequest(user, tmpPubKeyStr);
	}
	
}
