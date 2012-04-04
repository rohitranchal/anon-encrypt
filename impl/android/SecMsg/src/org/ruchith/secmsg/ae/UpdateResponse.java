package org.ruchith.secmsg.ae;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class UpdateResponse {

	public static final String TYPE = "update_response";
	
	private String replyTo;
	private String encryptedData;
	private String encryptedKey;
	
	public UpdateResponse(ObjectNode on) {
		this.replyTo = new String(Base64.decode(on.get("reply_to").getTextValue()));
		this.encryptedData = on.get("encrypted_data").getTextValue();
		this.encryptedKey = on.get("encrypted_key").getTextValue();
	}
	
	public UpdateResponse(String replyTo, String cipherData, String encryptedKey) {
		this.replyTo = replyTo;
		this.encryptedData = cipherData;
		this.encryptedKey = encryptedKey;
	}
	
	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		
		on.put("type",  TYPE);
		on.put("reply_to", new String(Base64.encode(this.replyTo.getBytes())));
		on.put("encrypted_data", new String(Base64.encode(this.encryptedData.getBytes())));
		on.put("encrypted_key", this.encryptedKey);
	
		return on;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public String getCipherData() {
		return encryptedData;
	}

	public String getEncryptedKey() {
		return encryptedKey;
	}
	
}
