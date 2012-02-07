package org.ruchith.ae.base;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

/**
 * A block of cipher text.
 * 
 * @author Ruchith Fernando
 *
 */
public class AECipherTextBlock {
	
	private Element a;
	private Element b;
	private Element c;

	public AECipherTextBlock(Element a, Element b, Element c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public AECipherTextBlock(OMElement elem, Pairing pairing) {
		OMElement aElem = elem.getFirstChildWithName(new QName("A"));
		Element tmp = pairing.getGT().newElement();
		tmp.setFromBytes(Base64.decode(aElem.getText()));
		this.a = tmp.getImmutable();
		
		OMElement bElem = elem.getFirstChildWithName(new QName("B"));
		tmp = pairing.getG1().newElement();
		tmp.setFromBytes(Base64.decode(bElem.getText()));
		this.b = tmp.getImmutable();
		
		OMElement cElem = elem.getFirstChildWithName(new QName("C"));
		tmp = pairing.getG1().newElement();
		tmp.setFromBytes(Base64.decode(cElem.getText()));
		this.c = tmp.getImmutable();
		
	}
	
	public Element getA() {
		return a;
	}

	public Element getB() {
		return b;
	}

	public Element getC() {
		return c;
	}

	public String serialize() {
		String output = "<CipherTextBlock>\n";
		
		output += "<A>" + Base64.encode(this.a.toBytes()) + "</A>\n";
		output += "<B>" + Base64.encode(this.b.toBytes()) + "</B>\n";
		output += "<C>" + Base64.encode(this.c.toBytes()) + "</C>\n";
		output += "</CipherTextBlock>";
		
		return output;
	}
}
