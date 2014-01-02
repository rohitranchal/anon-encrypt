package org.ruchith.ae.peer;

import java.util.HashMap;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
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

	

}
