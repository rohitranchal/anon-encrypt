package edu.purdue.cs626.anonencrypt.app;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;

import edu.purdue.cs626.anonencrypt.AECipherText;
import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.AEPrivateKey;
import edu.purdue.cs626.anonencrypt.ContactKeyGen;
import edu.purdue.cs626.anonencrypt.Encrypt;
import edu.purdue.cs626.anonencrypt.ReKey;
import edu.purdue.cs626.anonencrypt.ReKeyInformation;
import edu.purdue.cs626.anonencrypt.RootKeyGen;
import edu.purdue.cs626.anonencrypt.TextEncoder;
import edu.purdue.cs626.anonencrypt.Util;
import edu.purdue.cs626.anonencrypt.db.Database;

/**
 * Main application API that depends on an installation. (i.e. requires the
 * configuration to be in the user home directory)
 * 
 * @author Ruchith Fernando
 * 
 */
public class Application {

	/**
	 * My encryption parameters.
	 */
	private AEParameters params;

	/**
	 * My public key
	 */
	private Element masterKey;

	public Application() throws Exception {
		String userHome = System.getProperty("user.home");
		String paramPath = userHome + File.separator + Constants.CONFIG_DIR
				+ File.separator + Constants.PARAM_FILE_NAME;
		StAXOMBuilder builder = new StAXOMBuilder(paramPath);
		OMElement paramElem = builder.getDocumentElement();
		this.params = new AEParameters(paramElem);

		String mkPath = userHome + File.separator + Constants.CONFIG_DIR
				+ File.separator + Constants.MASTER_KEY_FILE_NAME;
		File mkFile = new File(mkPath);
		FileInputStream fis = new FileInputStream(mkFile);
		byte[] data = new byte[(int) mkFile.length()];
		fis.read(data, 0, data.length);
		fis.close();

		Element elem = params.getPairing().getG1().newElement();
		elem.setFromBytes(data);
		this.masterKey = elem.getImmutable();

	}

	/**
	 * Create a new random private key for the contact identified by the given
	 * friendly name and return the data to be sent to that contact.
	 * 
	 * @param name
	 *            Friendly name of the contact as a {@link String} value.
	 * @return A {@link ContactPrivData} object. Simply call
	 *         {@link ContactPrivData#serialize()} to obtain the XML data to
	 *         send to the contact.
	 */
	public ContactPrivData createContact(String name) throws Exception {

		// Generate a key with a random ID
		RootKeyGen rkg = new RootKeyGen();
		rkg.init(this.params);
		Field zr = this.params.getPairing().getZr();
		Element id1 = zr.newRandomElement().getImmutable();
		Element r = zr.newRandomElement().getImmutable();
		AEPrivateKey contactKey = rkg.genKey(id1, this.masterKey, r);

		// Store in the database
		String sql = "INSERT INTO Contact(contactId, id, random) VALUES ("
				+ "'" + name + "'," + "'" + Base64.encode(id1.toBytes()) + "',"
				+ "'" + Base64.encode(r.toBytes()) + "')";

		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		s.execute(sql);
		return new ContactPrivData(this.params, id1, contactKey);
	}

	/**
	 * Register a remote contact with the private information he/she created for
	 * me.
	 * 
	 * @param name
	 *            Friendly name of the remote contact.
	 * @param data
	 *            {@link ContactPrivData} instance from the remote contact.
	 */
	public void registerContact(String name, ContactPrivData data)
			throws Exception {
		// Store in the database
		String sql = "UPDATE Contact SET privDataFromContact='"
				+ data.serialize() + "', myIDFromContact='"
				+ Base64.encode(data.getId().toBytes())
				+ "' WHERE contactId = '" + name + "'";
		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		s.execute(sql);
	}

	/**
	 * Re-key parameters and return the information to publish.
	 * 
	 * @return Public information as a {@link ReKeyInformation} instance.
	 * @throws Exception
	 */
	public ReKeyInformation reKey() throws Exception {

		ReKey reKey = new ReKey(this.params);
		Element mk = reKey.update();
		ApplicationInstaller.saveMasterKey(mk);

		// Get all contacts in the database and generate public information.
		String sql = "SELECT id, random FROM Contact";
		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();

		ResultSet rs = s.executeQuery(sql);
		HashMap<Element, Element> idRndMap = new HashMap<Element, Element>();
		while (rs.next()) {
			Element id = this.params.getPairing().getZr().newElement();
			Element rnd = this.params.getPairing().getZr().newElement();

			id.setFromBytes(Base64.decode(rs.getString(1)));
			rnd.setFromBytes(Base64.decode(rs.getString(2)));

			idRndMap.put(id, rnd);
		}

		return reKey.getPublicInfo(idRndMap);

	}

	public void saveMessage(String user, String msg) throws Exception {
		String sql = "UPDATE Contact SET lastMsg='" + msg
				+ "' WHERE contactId = '" + user + "'";
		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		s.execute(sql);
	}

	/**
	 * Send the last message of the requested user.
	 * 
	 * @param updateRequest
	 * @return
	 */
	public String getUpdate(String updateRequest) throws Exception {
		UpdateRequest ur = new UpdateRequest(Util.getOMElement(updateRequest));

		// Get the user's parameter's from the DB
		String user = ur.getUser();

		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		String sql = "SELECT privDataFromContact, lstMsg FROM Contact WHERE contactId='"
				+ user + "'";
		ResultSet rs = s.executeQuery(sql);
		if (rs.next()) {
			String privDataStr = rs.getString(1);
			String msg = rs.getString(2);

			// Create priv data object
			ContactPrivData cpd = new ContactPrivData(
					Util.getOMElement(privDataStr));
			AEParameters contactParams = cpd.getParams();
			
			//Convert the request id to an Element
			Element idElem = contactParams.getPairing().getG1().newElement();
			idElem.setFromBytes(Base64.decode(ur.getRndId()));
			idElem = idElem.getImmutable();
			
			//Encode the msg to be sent
			TextEncoder encoder = new TextEncoder();
			encoder.init(contactParams);
			Element[] msgElems = encoder.encode(msg);
			
			//Encrypt the msg
			Encrypt encrypt = new Encrypt();
			encrypt.init(contactParams);
			
			
			AECipherText cipherText = encrypt.doEncrypt(msgElems, idElem);
			
			return cipherText.serialize();
			
		} 
		
		return null;
		
	}
	
	/**
	 * Create an update request for a given user.
	 * 
	 * @param user The name of the user as a {@link String}.
	 * 
	 * @return An {@link UpdateRequest} object with the new random key.
	 */
	public UpdateRequest getUpdateRequest(String user) throws Exception {

		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		String sql = "SELECT privDataFromContact FROM Contact WHERE contactId='"
				+ user + "'";
		ResultSet rs = s.executeQuery(sql);
		if(rs.next()) {
			String privDataStr = rs.getString(1);

			// Create priv data object
			ContactPrivData cpd = new ContactPrivData(
					Util.getOMElement(privDataStr));
			
			ContactKeyGen keyGen = new ContactKeyGen();
			keyGen.init(cpd.getId(), cpd.getPrivKey(), cpd.getParams());
			Element rndId = keyGen.genRandomID();
			
		} 
		
		return null;
	}

	AEParameters getParams() {
		return params;
	}

}
