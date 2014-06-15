package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.util.HashMap;
import java.util.Iterator;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

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
	 * @param on
	 *            The published re-key information .
	 * @param pairing
	 *            We need the {@link Pairing} to be able to recreate the
	 *            elements. This must be from the {@link AEParameters} of the
	 *            user that the published re-key information belongs to.
	 */
	public ReKeyInformation(ObjectNode on, Pairing pairing) {
		
		Field group1 = pairing.getG1();
		this.g1 = group1.newElement();
		this.g1.setFromBytes(Base64.decode(on.get("g1").getTextValue()));
		
		Field zr = pairing.getZr();
		this.rnd = zr.newElement();
		this.rnd.setFromBytes(Base64.decode(on.get("rnd").getTextValue()));
		
		this.newC1map = new HashMap<String, Element>();
		ArrayNode contacts = (ArrayNode)on.get("contacts");
		for(int i = 0; i < contacts.size(); i++) {
			ObjectNode c = (ObjectNode)contacts.get(i);
			System.out.println("Contact node : " + c);
			String id = c.get("id").getTextValue();
			Element c1 = group1.newElement();
			c1.setFromBytes(Base64.decode(c.get("c1").getTextValue()));
			this.newC1map.put(id, c1);
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

	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode on = mapper.createObjectNode();
		
		on.put("g1", new String(Base64.encode(this.g1.toBytes())));
		on.put("rnd", new String(Base64.encode(this.rnd.toBytes())));
		
		ArrayNode contacts = (ArrayNode) mapper.createArrayNode();
		Iterator<String> ids = this.newC1map.keySet().iterator();
		while (ids.hasNext()) {
			ObjectNode tmpC = mapper.createObjectNode();
			String id = (String) ids.next();
			tmpC.put("id", id);
			Element c1 = this.newC1map.get(id);
			tmpC.put("c1", new String(Base64.encode(c1.toBytes())));
			contacts.add(tmpC);
		}
		on.put("contacts", contacts);
		
		return on;
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
