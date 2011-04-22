package edu.purdue.cs626.anonencrypt.app;

import it.unisa.dia.gas.jpbc.Element;


/**
 * A remote contact.
 * 
 * @author Ruchith Fernando
 *
 */
public class RemoteContact {

	/**
	 * Friendly identifier of the remote contact.
	 */
	private String contactId;

	/**
	 * Data from the contact.
	 */
	private ContactPrivData privData;
	
	public RemoteContact(String contactId, ContactPrivData privData) {
		this.contactId = contactId;
		this.privData = privData;
	}

	public String getContactId() {
		return contactId;
	}

	public ContactPrivData getPrivData() {
		return privData;
	}

	/**
	 * In the case of a global parameter update, the first component of the 
	 * private key will have to be updated. This component will be made
	 * available publicly.
	 * 
	 * @param c1 First component to be updated as an {@link Element}
	 */
	public void update(Element c1) {
		//TODO
	}
	
}
