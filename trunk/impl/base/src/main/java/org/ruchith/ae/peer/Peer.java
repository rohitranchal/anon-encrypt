package org.ruchith.ae.peer;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherText;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.Decrypt;
import org.ruchith.ae.base.Encrypt;
import org.ruchith.ae.base.ReKey;
import org.ruchith.ae.base.ReKeyInformation;
import org.ruchith.ae.base.RootKeyGen;
import org.ruchith.ae.base.TextEncoder;

/**
 * Completely in-memory implementation
 * 
 * @author ruchith
 */
public class Peer {

	private String name;
	private boolean lie;
	private AEParameters params;
	private Element masterKey;
	
	private HashMap<String, Contact> contacts = new HashMap<>();
	private HashMap<String, ContactPrivateData> privData = new HashMap<>();
	private HashMap<String, ArrayList<String>> messages = new HashMap<>();
	
	private HashMap<String, HashMap<String, AEPrivateKey>> tmpKeyList = new HashMap<>();
	
	public Peer(String name, boolean lie) {
		this.name = name;
		this.lie = lie;
		
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
	
	public String createContactStr(String name) {
		return this.createContact(name).serializeJSON().toString();
	}

	/**
	 * Add information provided by a remote contact.
	 * @param name
	 * @param privateData
	 */
	public void registerContact(String name, ContactPrivateData privateData) {
		this.privData.put(name, privateData);
	}

	public void registerContactStr(String name, String privData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode newOn = (ObjectNode)mapper.readTree(privData);
			this.registerContact(name, new ContactPrivateData(newOn));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public String generateRequestStr(String user) {
		return this.generateRequest(user).serializeJSON().toString();
	}
	
	/**
	 * Generate a response for a request.
	 * @param req
	 * @return
	 */
	public MessageResponse generateResponse(MessageRequest req) {
		String msg = "";
		String user = req.getUser();
		if(lie) {
			//Lie
			msg = "lie:BlahBlah";
		} else {
			if(messages.containsKey(user)) {
				//Check whether this is on of my requests
				HashMap<String, AEPrivateKey> tmpKeyMap = this.tmpKeyList.get(user);
				if(tmpKeyMap != null && tmpKeyMap.containsKey(req.getTmpPubKey())) {
					return null;
				}
				msg = getLastMesssageOfContact(user);
			} else {//If we don't have a message
				return null;
			}
		}
	
		//Encrypt msg and send response
		ContactPrivateData cpd = privData.get(user);
		AEParameters contactParams = cpd.getParams();
		
		String tmpPubKey = req.getTmpPubKey();
		Element idElem = contactParams.getPairing().getG1().newElement();
		idElem.setFromBytes(Base64.decode(tmpPubKey));
		idElem = idElem.getImmutable();
		
		
		TextEncoder encoder = new TextEncoder();
		encoder.init(contactParams);
		Element[] msgElems = encoder.encode(msg);
		
		// Encrypt the msg
		Encrypt encrypt = new Encrypt();
		encrypt.init(contactParams);

		AECipherText cipherText = encrypt.doEncrypt(msgElems, idElem);

		MessageResponse resp = new MessageResponse(user, tmpPubKey, cipherText.serializeJSON());
		return resp;
	}

	public String generateResponseStr(String reqStr) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode newOn = (ObjectNode)mapper.readTree(reqStr);
			MessageRequest req = new MessageRequest(newOn);
			MessageResponse resp = this.generateResponse(req);
			if(resp != null) {
				return resp.serializeJSON().toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String getLastMesssageOfContact(String user) {
		ArrayList<String> msgList = messages.get(user);
		if(msgList != null) {
			String msg = msgList.get(msgList.size()-1);
			return msg;
		} else {
			return null;
		}
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
				AECipherText ct = new AECipherText(resp.getCipherText(), contactParams.getPairing());
				
				//Decrypt
				Decrypt decrypt = new Decrypt();
				decrypt.init(contactParams);
				Element[] plainElems = decrypt.doDecrypt(ct.getBlocks(), tmpKey);
				TextEncoder encoder = new TextEncoder();
				encoder.init(contactParams);
				byte[] decoded = encoder.decode(plainElems);
				
				String msg = new String(decoded).trim();
				this.addDirectMessage(user, msg);
				return msg;
			}
		}
		return null;
	}
	
	public String processResponseStr(String respStr) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode newOn = (ObjectNode)mapper.readTree(respStr);
			MessageResponse resp = new MessageResponse(newOn);
			return this.processResponse(resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] getContactList() {
		String[] names = this.contacts.keySet().toArray(new String[this.contacts.size()]);
		return names;
	}
	
	public String getContacts() {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode root = mapper.createArrayNode();
		
		Iterator<String> contactIterator= this.contacts.keySet().iterator();
		while (contactIterator.hasNext()) {
			String contact = contactIterator.next();
			
			ObjectNode tmp = mapper.createObjectNode();
			ContactPrivateData tmpPrivData = this.privData.get(contact);
			
			tmp.put("name", contact);
			if(tmpPrivData != null) {
				tmp.put("priv_data", tmpPrivData.serializeJSON().toString());
			}
			String lstMsg = this.getLastMesssageOfContact(contact);
			if(lstMsg != null) {
				tmp.put("last_message", lstMsg);
			}
			root.add(tmp);
		}
		
		return root.toString();
	}

	public String getName() {
		return name;
	}
	
	public String removeContact(String name) {
		//Remove user from the list of contacts
		this.contacts.remove(name);
		this.privData.remove(name);
		
		//Generate re-key information
		ReKey rk = new ReKey(this.params);
		rk.update();
		this.masterKey = rk.getMk();
		HashMap<Element, Element> idRndMap = new HashMap<Element, Element>();
		for(Contact c: this.contacts.values()) {
			idRndMap.put(c.getId(), c.getR());
		}
		
		return rk.getPublicInfo(idRndMap).serializeJSON().toString();
	}
	
	
	public String processReKeyInformation(String user, String rkiVal) {
		System.out.println(rkiVal);
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode newOn = (ObjectNode)mapper.readTree(rkiVal);
			System.out.println(user);
			ContactPrivateData contactPrivateData = this.privData.get(user);
			if(contactPrivateData != null) {
				ReKeyInformation rki = new ReKeyInformation(newOn, contactPrivateData.getParams().getPairing());
				Element rnd = rki.getRnd();
				Element id = contactPrivateData.getId();
				Element key = rnd.powZn(id);
				String keyVal = new String(Base64.encode(key.toBytes()));
				Element c1 = rki.getNewC1map().get(keyVal);
				
				if(c1 != null) {
					contactPrivateData.getKey().setC1(c1);
					this.privData.put(user, contactPrivateData);
					return "UPDATED";
				} else {
					return "REMOVED";
				}
			} else {
				return "CONTACT_NOT_FOUND";
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "ERROR";
	}
}
