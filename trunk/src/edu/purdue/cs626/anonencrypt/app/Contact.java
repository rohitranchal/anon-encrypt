package edu.purdue.cs626.anonencrypt.app;


/**
 * A remote contact.
 * 
 * @author Ruchith Fernando
 *
 */
public class Contact {

	/**
	 * Friendly identifier of the remote contact.
	 */
	private String contactId;

	private ContactPrivData privData;
	
	public Contact(String contactId, ContactPrivData privData) {
		this.contactId = contactId;
		this.privData = privData;
	}

	public String getContactId() {
		return contactId;
	}

	public ContactPrivData getPrivData() {
		return privData;
	}
	
	
}
