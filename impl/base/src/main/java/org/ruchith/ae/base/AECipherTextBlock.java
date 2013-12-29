package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

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
	
	public AECipherTextBlock(ObjectNode on, Pairing pairing) {
		Element tmp = pairing.getGT().newElement();
		tmp.setFromBytes(Base64.decode(on.get("a").getTextValue()));
		this.a = tmp.getImmutable();
		
		tmp = pairing.getG1().newElement();
		tmp.setFromBytes(Base64.decode(on.get("b").getTextValue()));
		this.b = tmp.getImmutable();
		
		tmp = pairing.getG1().newElement();
		tmp.setFromBytes(Base64.decode(on.get("c").getTextValue()));
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

	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		on.put("a", new String(Base64.encode(this.a.toBytes())));
		on.put("b", new String(Base64.encode(this.b.toBytes())));
		on.put("c", new String(Base64.encode(this.c.toBytes())));
		
		return on;
	}
}
