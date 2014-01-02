package org.ruchith.ae.peer;

import it.unisa.dia.gas.jpbc.Element;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;

public class ContactPrivateData {

	private AEPrivateKey key;
	private AEParameters params;
	private Element id;

	public ContactPrivateData(Element id, AEPrivateKey key, AEParameters params) {
		this.id = id;
		this.key = key;
		this.params = params;
	}

	public ContactPrivateData(ObjectNode on) {
		ObjectNode pOn = (ObjectNode) on.get("params");
		this.params = new AEParameters(pOn);

		ObjectNode kOn = (ObjectNode) on.get("key");
		this.key = new AEPrivateKey(kOn, this.params.getPairing());

		TextNode iOn = (TextNode) on.get("id");
		this.id = this.params.getPairing().getZr().newElement();
		String textValue = iOn.getTextValue();
		byte[] decode = Base64.decode(textValue);
		this.id.setFromBytes(decode);
	}

	public AEPrivateKey getKey() {
		return key;
	}

	public AEParameters getParams() {
		return params;
	}

	public Element getId() {
		return id;
	}

	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		on.put("id", new String(Base64.encode(this.id.toBytes())));
		on.put("key", this.key.serializeJSON());
		on.put("params", this.params.serializeJSON());

		return on;

	}

}
