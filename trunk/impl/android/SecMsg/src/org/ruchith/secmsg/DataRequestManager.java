package org.ruchith.secmsg;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.secmsg.ae.PublicChannel;
import org.ruchith.secmsg.ae.UpdateRequest;
import org.ruchith.secmsg.ae.UpdateResponse;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class DataRequestManager {

	private static final String TAG = "DataRequester";

	private DBAdapter db;
	private Context ctx;
	private PublicChannel pubChannel;

	public DataRequestManager(DBAdapter dbAdapter, Context ctx) {
		this.db = dbAdapter;
		this.ctx = ctx;
		this.pubChannel = PublicChannel.getInstance();
	}

	public boolean request(String contact) {
		try {

			// Get private key of contact
			Cursor privDataC = db.getPrivData(contact);
			String privDataStr = null;
			String idStr = null;
			if (privDataC.moveToFirst()) {
				privDataStr = privDataC.getString(privDataC
						.getColumnIndex(DBAdapter.KEY_PRIV_DATA));
				idStr = privDataC.getString(privDataC
						.getColumnIndex(DBAdapter.KEY_ID));
			} else {
				Log.i(TAG, this.ctx.getString(R.string.ex_no_such_contact));
				return false;
			}

			AEManager aeManager = AEManager.getInstance();
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode on = (ObjectNode) mapper.readTree(privDataStr);
			AEParameters params = aeManager.getParameters();
			Pairing pairing = params.getPairing();
			AEPrivateKey privKey = new AEPrivateKey(on, pairing);

			// Generate temp key
			Element id = pairing.getZr().newElement();
			id.setFromBytes(idStr.getBytes());
			ContactKeyGen keyGen = new ContactKeyGen();
			keyGen.init(id, privKey, params);

			Element randId = keyGen.genRandomID();
			AEPrivateKey randPrivKey = keyGen.getTmpPrivKey(randId);

			// Store temp key
			String salt = UUID.randomUUID().toString();
			db.addRequestInfo(randId.toString(), contact, salt, 
					randPrivKey.serializeJSON().toString());

			UpdateRequest ur = new UpdateRequest(contact, salt,
					randId.toString());

			String publish = ur.serializeJSON().toString();

			// Send request
			return this.pubChannel.publish(publish);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}
	
	public void refresh() {
		try {
			//Get all new content
			ArrayNode an = this.pubChannel.pullAll();
			
			for(int i = 0; i < an.size(); i++) {
				ObjectNode contentNode = (ObjectNode) an.get(i);
				
				//Get the JSON Object in Content
				String onVAlue = contentNode.get("Content").getTextValue();
				
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode on = (ObjectNode) mapper.readTree(Base64.decode(onVAlue));
				String type = on.get("type").getTextValue();
				
				if(type.equals(UpdateRequest.TYPE)) {
					UpdateRequest tmpReq = new UpdateRequest(on);
					processIncomingUpdateRequest(tmpReq);
				} else if(type.equals(UpdateResponse.TYPE)) {
					UpdateResponse tmpRes = new UpdateResponse(on);
					processIncomingUpdateResponse(tmpRes);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

	}

	private void processIncomingUpdateRequest(UpdateRequest ur) {
		String b64Dgst = ur.getContactDgst();
		String salt = ur.getSalt();
		String contact = getContact(b64Dgst, salt);
		if(contact != null) {
			Log.i(TAG, "Contact found : " + contact);
			//Send Response
			
		}
	}
	
	private void processIncomingUpdateResponse(UpdateResponse ur) {
		//TODO
	}
	
	
	private String getContact(String dgstVal, String salt) {
		//Get the list of contacts I have
		Cursor c = db.fetchAllContacts();
		c.moveToFirst();

		try {
			MessageDigest dgst = MessageDigest.getInstance("SHA-512");
			byte[] incoming = Base64.decode(dgstVal);
			while(!c.isAfterLast()) {
				String contact = c.getString(c.getColumnIndex(DBAdapter.KEY_CONTACT_ID));
				String tmpVal = contact + salt;
				byte[] tmpDgstValBytes = dgst.digest(tmpVal.getBytes());
				
				if(Arrays.equals(incoming, tmpDgstValBytes)) {
					return contact;
				}
			}
		} catch (Exception  e) {
			Log.e(TAG, e.getMessage());
		}
		
		return null;
	}
}
