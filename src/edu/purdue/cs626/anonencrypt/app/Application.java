package edu.purdue.cs626.anonencrypt.app;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.AEPrivateKey;
import edu.purdue.cs626.anonencrypt.ReKey;
import edu.purdue.cs626.anonencrypt.ReKeyInformation;
import edu.purdue.cs626.anonencrypt.RootKeyGen;
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
	 * @return Public information as a {@link ReKeyInformation} instance.
	 * @throws Exception
	 */
	public ReKeyInformation reKey() throws Exception {
		
		ReKey reKey = new ReKey(this.params);
		Element mk = reKey.update();
		ApplicationInstaller.saveMasterKey(mk);
		
		//Get all contacts in the database and generate public information.
		String sql = "SELECT id, random FROM Contact";
		Connection conn = Database.getConnection();
		Statement s = conn.createStatement();
		
		ResultSet rs = s.executeQuery(sql);
		HashMap<Element, Element> idRndMap = new HashMap<Element, Element>();
		while(rs.next()) {
			Element id = this.params.getPairing().getZr().newElement();
			Element rnd = this.params.getPairing().getZr().newElement();
			
			id.setFromBytes(Base64.decode(rs.getString(1)));
			rnd.setFromBytes(Base64.decode(rs.getString(2)));
			
			idRndMap.put(id, rnd);
		}
		
		return reKey.getPublicInfo(idRndMap);

	}

}
