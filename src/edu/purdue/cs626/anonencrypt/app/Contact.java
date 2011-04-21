package edu.purdue.cs626.anonencrypt.app;

import java.io.File;

import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.AEPrivateKey;
import it.unisa.dia.gas.jpbc.Element;

/**
 * A remote contact.
 * 
 * @author Ruchith Fernando
 *
 */
public class Contact {

	/**
	 * The identifier issued by the remote contact.
	 */
	private Element id;
	
	/**
	 * The global parameters of the remote contact.
	 */
	private AEParameters params;
	
	/**
	 * Friendly identifier of the remote contact.
	 */
	private String contactId;
	
	/**
	 * Private key issued by the remote contact
	 */
	private AEPrivateKey privKey;
	
	
	public Contact(File privCert) {
		//TODO
	}

	public Element getId() {
		return id;
	}

	public AEParameters getParams() {
		return params;
	}

	public String getContactId() {
		return contactId;
	}

	public AEPrivateKey getPrivKey() {
		return privKey;
	}
	
	
	
}
