package edu.purdue.cs626.anonencrypt.app;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.AEPrivateKey;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ContactPrivData {
	
	private AEParameters params;
	private Element id;
	private AEPrivateKey privKey;
	
	
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
	
	public ContactPrivData(AEParameters params, Element id, AEPrivateKey privKey) {
		this.params = params;
		this.id = id;
		this.privKey = privKey;
	}
	
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
