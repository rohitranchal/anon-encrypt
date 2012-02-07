package edu.purdue.cs626.anonencrypt.app;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

/**
 * The wrapper for information used to request for an update of 
 * a contact.
 * 
 * @author Ruchith Fernando
 *
 */
public class UpdateRequest {

	private String user;
	private String rndId;
	
	public UpdateRequest(String user, String rndId) {
		this.user = user;
		this.rndId = rndId;
	}
	
	public UpdateRequest(OMElement elem) {
		OMElement userElem = elem.getFirstChildWithName(new QName("User"));
		this.user = userElem.getText();
		
		OMElement idElem = elem.getFirstChildWithName(new QName("ID"));
		this.rndId = idElem.getText();
	}
	
	public String serialize() {
		String output = "<UpdateRequest>";
		output += "<User>" + this.user + "</User>\n";
		output += "<ID>" + this.rndId + "</ID>\n";
		output += "</UpdateRequest>";
		return output;
	}

	public String getUser() {
		return user;
	}

	public String getRndId() {
		return rndId;
	}
	
	
}
