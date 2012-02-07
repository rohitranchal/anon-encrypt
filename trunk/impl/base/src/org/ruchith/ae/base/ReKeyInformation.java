package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

/**
 * Wrapper for re-key information. Serialization of this generates the public
 * information contacts can use to update their configurations.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ReKeyInformation {

	/**
	 * New g1 value.
	 */
	private Element g1;

	/**
	 * This is used to blind all contact Id values by raising them to rnd.
	 */
	private Element rnd;

	private HashMap<String, Element> newC1map;

	/**
	 * Create {@link ReKeyInformation} object with published data.
	 * 
	 * @param elem
	 *            The published re-key information as an {@link OMElement}.
	 * @param pairing
	 *            We need the {@link Pairing} to be able to recreate the
	 *            elements. This must be from the {@link AEParameters} of the
	 *            user that the published re-key information belongs to.
	 */
	public ReKeyInformation(OMElement elem, Pairing pairing) {
		OMElement g1Elem = elem.getFirstChildWithName(new QName("G1"));
		Field group1 = pairing.getG1();
		this.g1 = group1.newElement();
		this.g1.setFromBytes(Base64.decode(g1Elem.getText()));
		
		OMElement rndElem = elem.getFirstChildWithName(new QName("Random"));
		Field zr = pairing.getZr();
		this.rnd = zr.newElement();
		this.rnd.setFromBytes(Base64.decode(rndElem.getText()));
		
		OMElement contactsElem = elem.getFirstChildWithName(new QName("Contacts"));
		
		Iterator<OMElement> contactElems = contactsElem.getChildrenWithLocalName("Contact");
		this.newC1map = new HashMap<String, Element>();
		while (contactElems.hasNext()) {
			OMElement contactElem = (OMElement) contactElems.next();
			
			OMElement idElem = contactElem.getFirstChildWithName(new QName("Id"));
			
			
			OMElement c1Elem = contactElem.getFirstChildWithName(new QName("C1"));
			Element c1 = group1.newElement();
			c1.setFromBytes(Base64.decode(c1Elem.getText()));
			
			this.newC1map.put(idElem.getText(), c1);
		}
		
	}

	/**
	 * Create new instance with the given values. Called by the re-keying
	 * entity.
	 * 
	 * @param g1
	 *            New g1 as an {@link Element}
	 * @param rnd
	 *            Random used to blind identities as an {@link Element}
	 * @param newC1Map
	 *            The map of new first components of the private keys indexed by
	 *            the blinded identity.
	 */
	public ReKeyInformation(Element g1, Element rnd,
			HashMap<String, Element> newC1Map) {
		this.g1 = g1;
		this.rnd = rnd;
		this.newC1map = newC1Map;
	}

	/**
	 * Output the XML data to publish as a {@link String}.
	 * 
	 * @return {@link String} of XML data.
	 */
	public String serialize() {
		String output = "<ReKeyInformation>\n";
		output += "<G1>" + Base64.encode(this.g1.toBytes()) + "</G1>\n";
		output += "<Random>" + Base64.encode(this.rnd.toBytes())
				+ "</Random>\n";
		output += "<Contacts>\n";

		Iterator<String> ids = this.newC1map.keySet().iterator();
		while (ids.hasNext()) {
			output += "<Contact>\n";
			String id = (String) ids.next();
			output += "<Id>" + id + "</Id>";

			Element c1 = this.newC1map.get(id);
			output += "<C1>" + Base64.encode(c1.toBytes()) + "</C1>\n";

			output += "</Contact>\n";

		}
		output += "</Contacts>\n";

		output += "</ReKeyInformation>";
		return output;
	}

	public Element getG1() {
		return g1;
	}

	public Element getRnd() {
		return rnd;
	}

	public HashMap<String, Element> getNewC1map() {
		return newC1map;
	}
	
	
}
