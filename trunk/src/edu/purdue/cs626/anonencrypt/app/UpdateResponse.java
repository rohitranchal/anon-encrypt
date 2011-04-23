package edu.purdue.cs626.anonencrypt.app;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

public class UpdateResponse {

	private String user;
	private String cipherTextString;

	public UpdateResponse(String user, String cipherTextString) {
		this.user = user;
		this.cipherTextString = cipherTextString;
	}

	public UpdateResponse(OMElement elem) {

		OMElement userElem = elem.getFirstChildWithName(new QName("User"));
		this.user = userElem.getText();

		OMElement encrData = elem.getFirstChildWithName(new QName(
				"EncryptedData"));
		this.cipherTextString = encrData.getFirstChildWithName(new QName(
				"CipherText")).toString();
	}

	public String serialize() {
		String output = "<UpdateResponse>";
		output += "<User>" + this.user + "</User>\n";
		output += "<EncryptedData>" + this.cipherTextString
				+ "</EncryptedData>\n";
		output += "</UpdateResponse>";
		return output;
	}

	public String getUser() {
		return user;
	}

	public String getCipherTextString() {
		return cipherTextString;
	}

}
