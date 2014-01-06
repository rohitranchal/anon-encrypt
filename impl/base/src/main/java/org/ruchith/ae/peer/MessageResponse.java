package org.ruchith.ae.peer;

import it.unisa.dia.gas.jpbc.Pairing;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.ruchith.ae.base.AECipherText;

public class MessageResponse {
	
	private String user;
	private String tmpPubKey;
	private ArrayNode cipherText;
	
	public MessageResponse(String user, String tmpPubKey, ArrayNode cipherText) {
		this.user = user;
		this.tmpPubKey = tmpPubKey;
		this.cipherText = cipherText;
	}
	
	public MessageResponse(ObjectNode on) {
		TextNode uTn = (TextNode) on.get("user");
		this.user = uTn.getTextValue();
		
		TextNode pkTn = (TextNode) on.get("tmpPubKey");
		this.tmpPubKey = pkTn.getTextValue();
		
		ArrayNode ctTn = 
		this.cipherText = (ArrayNode) on.get("cipherText"); 
	}
	
	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		on.put("type", "data_response");
		on.put("user", this.user);
		on.put("tmpPubKey", this.tmpPubKey);
		on.put("cipherText", this.cipherText);
		return on;
	}

	public String getTmpPubKey() {
		return tmpPubKey;
	}

	public ArrayNode getCipherText() {
		return cipherText;
	}

	public String getUser() {
		return user;
	}
	
	
}
