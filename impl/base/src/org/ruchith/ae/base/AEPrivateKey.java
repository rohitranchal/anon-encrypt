package org.ruchith.ae.base;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

/**
 * This class represents a private key of a contact.
 * Also when a contact creates a temporary key to request data to be encrypted
 * this class will hold the temporary private key as well.
 * 
 * @author Ruchith Fernando
 *
 */
public class AEPrivateKey {

	private Element c1;
	private Element c2;
	private ArrayList<Element> c3;

	public AEPrivateKey(OMElement elem, Pairing pairing) {
		
		Field group1 = pairing.getG1();

		OMElement c1Elem = elem.getFirstChildWithName(new QName("C1"));
		OMElement c2Elem = elem.getFirstChildWithName(new QName("C2"));
		OMElement c3Elem = elem.getFirstChildWithName(new QName("C3"));
		
		Element tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(c1Elem.getText()));
		this.c1  = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(c2Elem.getText()));
		this.c2  = tmpElem.getImmutable();
		
		
		Iterator<OMElement> c3Children = c3Elem.getChildrenWithLocalName("Elem");
		this.c3 = new ArrayList<Element>();
		while (c3Children.hasNext()) {
			OMElement omElem = (OMElement) c3Children.next();
			tmpElem = group1.newElement();
			tmpElem.setFromBytes(Base64.decode(omElem.getText()));
			this.c3.add(tmpElem.getImmutable());
		}
	}
	
	public AEPrivateKey(Element c1, Element c2, ArrayList<Element> c3) {
		this.c1 = c1.getImmutable();
		this.c2 = c2.getImmutable();
		this.c3 = new ArrayList<Element>();
		for (Iterator iterator = c3.iterator(); iterator.hasNext();) {
			Element elem = (Element) iterator.next();
			this.c3.add(elem.getImmutable());
		}
	}

	public Element getC1() {
		return c1;
	}

	public Element getC2() {
		return c2;
	}

	public ArrayList<Element> getC3() {
		return this.c3;
	}
	
	public String serialize() {
		String output = "<AEPrivateKey>\n";
		output += "<C1>" + Base64.encode(this.c1.toBytes()) + "</C1>\n";
		output += "<C2>" + Base64.encode(this.c2.toBytes()) + "</C2>\n";
		output += "<C3>\n";
		for (Iterator iterator = this.c3.iterator(); iterator.hasNext();) {
			Element tmpElem = (Element) iterator.next();
			output +="<Elem>" + Base64.encode(tmpElem.toBytes()) + "</Elem>\n";
		}
		output += "</C3>\n";
		output += "</AEPrivateKey>";
		return output;
	}

}
