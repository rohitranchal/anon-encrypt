package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

import it.unisa.dia.gas.jpbc.Element;

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

	public AEPrivateKey(OMElement elem) {
		OMElement curveElem = elem.getFirstChildWithName(new QName("Curve"));
		
	}
	
	public AEPrivateKey(Element c1, Element c2, ArrayList<Element> c3) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		
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
