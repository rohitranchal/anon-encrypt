package org.ruchith.ae.peer;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.util.encoders.Base64;
import org.ruchith.ae.base.AECipherText;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.Decrypt;
import org.ruchith.ae.base.Encrypt;
import org.ruchith.ae.base.RootKeyGen;
import org.ruchith.ae.base.TextEncoder;

/**
 * Completely in-memory implementation
 * 
 * @author ruchith
 */
public class Peer {

	private String name;
	private AEParameters params;
	private Element masterKey;
	
	private HashMap<String, Contact> contacts = new HashMap<>();
	private HashMap<String, ContactPrivateData> privData = new HashMap<>();
	private HashMap<String, ArrayList<String>> messages = new HashMap<>();
	
	private HashMap<String, HashMap<String, AEPrivateKey>> tmpKeyList = new HashMap<>(); 
	
	public Peer(String name) {
		this.name = name;
		
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		
		params = paramGen.generateParameters();
		this.masterKey = paramGen.getMasterKey();
		
	}

	/**
	 * Create a contact and return its private key
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ContactPrivateData createContact(String name) {

		// Generate a key with a random ID
		RootKeyGen rkg = new RootKeyGen();
		rkg.init(this.params);
		Field zr = this.params.getPairing().getZr();
		Element id1 = zr.newRandomElement().getImmutable();
		Element r = zr.newRandomElement().getImmutable();
		AEPrivateKey contactKey = rkg.genKey(id1, this.masterKey, r);
		
		Contact contact = new Contact();
		contact.setName(name);
		contact.setId(id1);
		contact.setR(r);
		
		this.contacts.put(name, contact);
		
		return new ContactPrivateData(id1, contactKey, params);
	}


	/**
	 * Add information provided by a remote contact.
	 * @param name
	 * @param privateData
	 */
	public void registerContact(String name, ContactPrivateData privateData) {
		this.privData.put(name, privateData);
	}


	/**
	 * Add a message directly received from a peer.
	 * @param user Name of the peer.
	 * @param message The message.
	 */
	public void addDirectMessage(String user, String message) {
		ArrayList<String> msgs = this.messages.get(user);
		if(msgs == null) {
			msgs = new ArrayList<String>();
			this.messages.put(user, msgs);
		}
		
		msgs.add(message);
	}
	
	/**
	 * Generate a request for data.
	 * @param user 
	 * @return
	 */
	public MessageRequest generateRequest(String user) {
		ContactPrivateData cpd = privData.get(user);
		
		ContactKeyGen keyGen = new ContactKeyGen();
		keyGen.init(cpd.getId(), cpd.getKey(), cpd.getParams());
		
		Element rndId = keyGen.genRandomID();
		AEPrivateKey privKey = keyGen.getTmpPrivKey(rndId);
		Element tmpPubKey = keyGen.getTmpPubKey(rndId);
		String tmpPubKeyStr = new String(Base64.encode(tmpPubKey.toBytes()));
		
		//Store private key
		HashMap<String, AEPrivateKey> tmpKeyMap = this.tmpKeyList.get(user);
		if(tmpKeyMap == null) {
			tmpKeyMap = new HashMap<String, AEPrivateKey>();
			this.tmpKeyList.put(user, tmpKeyMap);
		}
		tmpKeyMap.put(tmpPubKeyStr, privKey);
		
		return new MessageRequest(user, tmpPubKeyStr);
	}
	
	/**
	 * Generate a response for a request.
	 * @param req
	 * @return
	 */
	public MessageResponse generateResponse(MessageRequest req) {
		String user = req.getUser();
		//If there are no messages from the user there's no point of looking
		//any further
		if(messages.containsKey(user)) { 
			//This is definitely there
			ContactPrivateData cpd = privData.get(user);
			AEParameters contactParams = cpd.getParams();
			
			String tmpPubKey = req.getTmpPubKey();
			Element idElem = contactParams.getPairing().getG1().newElement();
			idElem.setFromBytes(Base64.decode(tmpPubKey));
			idElem = idElem.getImmutable();
			
			//Last msg form user
			ArrayList<String> msgList = messages.get(user);
			String msg = msgList.get(msgList.size()-1);
			
			TextEncoder encoder = new TextEncoder();
			encoder.init(contactParams);
			Element[] msgElems = encoder.encode(msg);
			
			// Encrypt the msg
			Encrypt encrypt = new Encrypt();
			encrypt.init(contactParams);

			AECipherText cipherText = encrypt.doEncrypt(msgElems, idElem);

			MessageResponse resp = new MessageResponse(user, tmpPubKey, cipherText);
			return resp;
		}
		return null;
	}
	
	/**
	 * Process an incoming response.
	 * @param resp
	 * @return
	 */
	public String processResponse(MessageResponse resp) {
		String user = resp.getUser();

		HashMap<String, AEPrivateKey> pkMap = this.tmpKeyList.get(user);
		if(pkMap != null) {
			String tmpPubKey = resp.getTmpPubKey();
			AEPrivateKey tmpKey = pkMap.get(tmpPubKey);
			if(tmpKey != null) {
				ContactPrivateData cpd = this.privData.get(user);
				AEParameters contactParams = cpd.getParams();
				
				AECipherText ct = resp.getCipherText();
				
				//Decrypt
				Decrypt decrypt = new Decrypt();
				decrypt.init(contactParams);
				Element[] plainElems = decrypt.doDecrypt(ct.getBlocks(), tmpKey);
				TextEncoder encoder = new TextEncoder();
				encoder.init(contactParams);
				byte[] decoded = encoder.decode(plainElems);
				
				String msg = new String(decoded).trim();
				return msg;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}
	
}
