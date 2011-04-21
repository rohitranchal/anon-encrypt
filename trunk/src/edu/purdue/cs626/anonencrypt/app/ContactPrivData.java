package edu.purdue.cs626.anonencrypt.app;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.AEPrivateKey;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * Wrapper for private data of a contact.
 * The xml element generated by the {@link ContactPrivData}{@link #serialize()}
 * method is used give this information to the contact.
 * 
 * @author Ruchith Fernando
 *
 */
public class ContactPrivData {
	
	/**
	 * Encryption parameters
	 */
	private AEParameters params;
	
	/**
	 * Random identifier assigned to the contact
	 */
	private Element id;
	
	/**
	 * Private key of the contact
	 */
	private AEPrivateKey privKey;
	
	/**
	 * Parse the {@link OMElement} with the private information and instantiate
	 * this object.
	 * @param elem The {@link OMElement} generated by the {@link #serialize()}
	 * method.
	 */
	public ContactPrivData(OMElement elem) {
		OMElement paramElem = elem.getFirstChildWithName(new QName("AEParameters"));
		this.params = new AEParameters(paramElem);
		
		Pairing pairing = PairingFactory.getPairing(this.params.getCurveParams());

		OMElement idElem = elem.getFirstChildWithName(new QName("ID"));
		this.id = pairing.getZr().newElement();
		this.id.setFromBytes(Base64.decode(idElem.getText()));
		
		OMElement privKeyElem = elem.getFirstChildWithName(new QName("AEPrivateKey"));
		this.privKey = new AEPrivateKey(privKeyElem, pairing);
	}
	
	/**
	 * This is used ot create a {@link ContactPrivData} instance when the 
	 * private key is generated.
	 * @param params Encryption parameters.
	 * @param id Random identifier for this contact.
	 * @param privKey Private key of the contact.
	 */
	public ContactPrivData(AEParameters params, Element id, AEPrivateKey privKey) {
		this.params = params;
		this.id = id;
		this.privKey = privKey;
	}
	
	/**
	 * Generate the XMl representation of this object.
	 * @return XML content as a {@link String}.
	 */
	public String serialize() {
		String output = "<PrivateCert>\n";
		output += this.params.serialize() + "\n";
		output += "<ID>" + Base64.encode(this.id.toBytes())+ "</ID>\n";
		output += this.privKey.serialize() + "\n";
		output += "</PrivateCert>";
		return output;
	}

	public AEParameters getParams() {
		return params;
	}

	public Element getId() {
		return id;
	}

	public AEPrivateKey getPrivKey() {
		return privKey;
	}

	
}
