package org.ruchith.secmsg;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherText;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.Decrypt;
import org.ruchith.ae.base.Encrypt;
import org.ruchith.ae.base.TextEncoder;
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
	
	//Cached contact list
	ArrayList<String> contactList = new ArrayList<String>();

	public DataRequestManager(DBAdapter dbAdapter, Context ctx) {
		this.db = dbAdapter;
		this.ctx = ctx;
		this.pubChannel = PublicChannel.getInstance();

		Cursor c = db.fetchAllContacts();
		c.moveToFirst();
		while(!c.isAfterLast()) {
			String contact = c.getString(c.getColumnIndex(DBAdapter.KEY_CONTACT_ID));
			this.contactList.add(contact);
			c.moveToNext();
		}
		c.close();

	}

	/**
	 * Generate and publish an update request for the given contact.
	 * @param contact Identifier of the contact.
	 * @return true upon success, false otherwise.
	 */
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
			privDataC.close();

			AEManager aeManager = AEManager.getInstance();
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode on = (ObjectNode) mapper.readTree(privDataStr);
			AEParameters params = aeManager.getParameters();
			Pairing pairing = params.getPairing();
			AEPrivateKey privKey = new AEPrivateKey(on, pairing);

			// Generate temp key
			Element id = pairing.getZr().newElement();
			id.setFromBytes(Base64.decode(idStr));
			id = id.getImmutable();
			ContactKeyGen keyGen = new ContactKeyGen();
			keyGen.init(id, privKey, params);

			Element randId = keyGen.genRandomID();
			
			//Temp private key
			AEPrivateKey randPrivKey = keyGen.getTmpPrivKey(randId);

			//Temp public key
			Element tmpPubKey = params.getH1().powZn(id).mul(params.getH2().powZn(randId));
			Log.d(TAG, "PUB_KEY_VAL" + tmpPubKey.toString());
			String pubKeyVal = new String(Base64.encode(tmpPubKey.toBytes()));
			
			// Store temp private key
			String salt = UUID.randomUUID().toString();
			String privKeyVal = randPrivKey.serializeJSON().toString();
			Log.d(TAG, "PRIV_KEY_VAL" + privKeyVal);
			db.addRequestInfo(pubKeyVal, contact, salt, 
					privKeyVal);

			UpdateRequest ur = new UpdateRequest(contact, salt, pubKeyVal);

			String publish = ur.serializeJSON().toString();

			// Send request
			return this.pubChannel.publish(publish);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}
	
	/**
	 * Update with the latest set of messages from the public channel.
	 */
	public void refresh() {
		
		int index = -1;
		
		try {
			//Get all new content
			ArrayNode an = this.pubChannel.pullAll();
			if(an == null) {
				return;
			}
			
			for(int i = 0; i < an.size(); i++) {
				ObjectNode contentNode = (ObjectNode) an.get(i);
				
				//Get the JSON Object in Content
				String onVAlue = contentNode.get("Content").getTextValue();
				
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode on = (ObjectNode) mapper.readTree(Base64.decode(onVAlue));
				String type = on.get("type").getTextValue();
				
				if(type.equals(UpdateRequest.TYPE)) {
					UpdateRequest tmpReq = new UpdateRequest(on);
					UpdateResponse resp = processIncomingUpdateRequest(tmpReq);
					if(resp != null) {
						this.pubChannel.publish(resp.serializeJSON().toString());
					}
				} else if(type.equals(UpdateResponse.TYPE)) {
					UpdateResponse tmpRes = new UpdateResponse(on);
					processIncomingUpdateResponse(tmpRes);
				}
				
				index = contentNode.get("ID").getIntValue();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
		//Update public channel entry index
		db.updatePublicChannelIndex(index);
		AEManager.getInstance().reload();

	}

	/**
	 * Process the given {@link UpdateRequest} instance.
	 * @param ur An {@link UpdateRequest} instance.
	 * @return If the related contact is in the current contact list, then
	 * an {@link UpdateResponse} instance. If not, null.
	 */
	private UpdateResponse processIncomingUpdateRequest(UpdateRequest ur) {
		String b64Dgst = ur.getContactDgst();
		String salt = ur.getSalt();
		String contact = getContact(b64Dgst, salt);
		if(contact != null) {
			Log.i(TAG, "Contact found : " + contact);
			//Send Response
			//We store one message per contact for now and we will send that message out
			String msg = db.getMessage(contact);
			if(msg != null) {
				//Create update response
				return createUpdateResponse(ur.getRandId(), msg);
			}
			
		}
		return null;
	}
	
	/**
	 * Generate update response. This will create a symmetric key and encrypt
	 * this symmetric key using the provided random key.
	 * 
	 * @param randId
	 *            The id to use for encryption of the key.
	 * @param msg
	 *            Message to be encrypted with the symmetric key
	 * @return An {@link UpdateResponse} instance.
	 */
	private UpdateResponse createUpdateResponse(String randId, String msg) {
		try {
			// Create random AES-256 KEY
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			SecretKey key = keyGen.generateKey();
			byte[] keyBytes = key.getEncoded();

			// Encrypt data
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			cipher.update(("R: " + msg).getBytes());
			byte[] encData = cipher.doFinal();
			String encDataVal = new String(Base64.encode(encData));

			// Encrypt key
			AEManager aeMan = AEManager.getInstance();
			AEParameters params = aeMan.getParameters();

			String keyb64 = new String(Base64.encode(keyBytes));
			Log.d(TAG, "SYMM_KEY: " + keyb64);

			TextEncoder encoder = new TextEncoder();
			encoder.init(params);
			Element[] encoded = encoder.encode(keyb64);

			Element pubKey = params.getPairing().getG1().newElement();
			pubKey.setFromBytes(Base64.decode(randId));
			pubKey = pubKey.getImmutable();
			Log.d(TAG, "PUB_KEY_VAL" + pubKey.toString());

			Encrypt encrypt = new Encrypt();
			encrypt.init(params);
			AECipherText encKey = encrypt.doEncrypt(encoded, pubKey);

			String encryptedKeyVal = encKey.serializeJSON().toString();
			UpdateResponse resp = new UpdateResponse(randId,
					encDataVal, encryptedKeyVal);

			return resp;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}

	/**
	 * Process the given {@link UpdateResponse} instance and save the decrypted
	 * message.
	 * 
	 * @param ur
	 *            {@link UpdateResponse} instance with encrypted data.
	 */
	private void processIncomingUpdateResponse(UpdateResponse ur) {
		try {
			String replyTo = ur.getReplyTo();
			String privKeyVal = db.getTempPrivKey(replyTo);
			if(privKeyVal != null) {
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode on = (ObjectNode)mapper.readTree(privKeyVal.getBytes());
				
				AEParameters params = AEManager.getInstance().getParameters();
				Pairing pairing = params.getPairing();
				
				AEPrivateKey tmpPriv = new AEPrivateKey(on, pairing);
				Log.d(TAG, "PRIV_KEY_VAL" + tmpPriv.serializeJSON().toString());
				
				Decrypt decrypt = new Decrypt();
				decrypt.init(params);
				
				ArrayNode an = (ArrayNode)mapper.readTree(ur.getEncryptedKey());
				AECipherText ct = new AECipherText(an, pairing);
				
				Element[] result = decrypt.doDecrypt(ct.getBlocks(), tmpPriv);
				
				TextEncoder encoder = new TextEncoder();
				encoder.init(params);
				
				String keyValB64 = new String(encoder.decode(result));
				Log.i(TAG, "SYMM_KEY: " + keyValB64.trim());
				byte[] symmKey = Base64.decode(keyValB64);
				byte[] symmKeyCopy = new byte[32];
				System.arraycopy(symmKey, 0, symmKeyCopy, 0, 32);
				
				
				SecretKeySpec keySpec = new SecretKeySpec(symmKeyCopy, "AES");
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, keySpec);
				cipher.update(Base64.decode(ur.getCipherData()));
				byte[] plainText = cipher.doFinal();
				String msg = new String(plainText);
				
				//Save the message
				String name = db.getContactID(replyTo);
				db.setMessage(name, msg);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

	}
	
	/**
	 * Obtain the contact name if it exists in the current list of contacts in
	 * the database.
	 * 
	 * @param dgstVal
	 *            The salted hash value of the contact name.
	 * @param salt
	 *            Salt value.
	 * @return The value of the contact name if the contact exists in the
	 *         database. If not null.
	 */
	private String getContact(String dgstVal, String salt) {
		//Get the list of contacts I have

		try {
			MessageDigest dgst = MessageDigest.getInstance("SHA-512");
			byte[] incoming = Base64.decode(dgstVal);

			for (Iterator<String> iterator = this.contactList.iterator(); 
						iterator.hasNext();) {
				String contact = (String) iterator.next();
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
