package org.ruchith.ae.peer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

public class MessageRequest {
	
	private String user;
	private String tmpPubKey;
	
	public MessageRequest(String user, String tmpPubKey) {
		this.user = user;
		this.tmpPubKey = tmpPubKey;
	}
	
	public MessageRequest(ObjectNode on) {
		TextNode uTn = (TextNode) on.get("user");
		this.user = uTn.getTextValue();
		
		TextNode pkTn = (TextNode) on.get("tmpPubKey");
		this.tmpPubKey = pkTn.getTextValue();
		
	}

	public String getUser() {
		return user;
	}

	public String getTmpPubKey() {
		return tmpPubKey;
	}
	
	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		on.put("type", "data_request");
		on.put("user", this.user);
		on.put("tmpPubKey", this.tmpPubKey);
		return on;

	}

	
}
