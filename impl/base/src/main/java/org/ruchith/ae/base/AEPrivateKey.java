package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.util.ArrayList;
import java.util.Iterator;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

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

	public AEPrivateKey(ObjectNode on, Pairing pairing) {
		Field group1 = pairing.getG1();
		
		Element tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("c1").getTextValue()));
		this.c1  = tmpElem.getImmutable();

		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("c2").getTextValue()));
		this.c2  = tmpElem.getImmutable();
		
		this.c3 = new ArrayList<Element>();
		ArrayNode an = (ArrayNode)on.get("c3");
		for(int i = 0; i < an.size(); i++) {
			tmpElem = group1.newElement();
			tmpElem.setFromBytes(Base64.decode(an.get(i).getTextValue()));
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
	
	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		on.put("c1", new String(Base64.encode(this.c1.toBytes())));
		on.put("c2", new String(Base64.encode(this.c2.toBytes())));
		ArrayNode c3Node = mapper.createArrayNode();
		for (Iterator iterator = this.c3.iterator(); iterator.hasNext();) {
			Element tmpElem = (Element) iterator.next();
			c3Node.add(new String(Base64.encode(tmpElem.toBytes())));
		}
		on.put("c3", c3Node);
		
		return on;
	}

}
