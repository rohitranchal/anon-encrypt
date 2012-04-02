package org.ruchith.secmsg.ae;

import java.security.MessageDigest;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import android.util.Log;

public class UpdateRequest {
	
	private static final String TAG = "DataRequester";

	private String contact;
	private String salt;
	private String randId;
	
	public UpdateRequest(String contact, String salt, String randId) {
		this.contact = contact;
		this.salt = salt;
		this.randId = randId;
	}

	public UpdateRequest(ObjectNode on) {
		//TODO
	}
	
	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		
		byte[] dgstVal = null;
		try {
			MessageDigest dgst = MessageDigest.getInstance("SHA-512");
			String val = this.contact + this.salt;
			dgstVal = dgst.digest(val.getBytes());
		} catch (Exception  e) {
			Log.e(TAG, e.getMessage());
		}

		String dgstValB64 = new String(Base64.encode(dgstVal));
		
		on.put("type",  "update_request");
		on.put("contact_dgst",  dgstValB64);
		on.put("salt",  this.salt);
		on.put("randId", this.randId);
		
		return on;
	}
	
}
