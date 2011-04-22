package edu.purdue.cs626.anonencrypt.app;

import java.io.File;
import java.io.FileInputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import it.unisa.dia.gas.jpbc.Element;
import edu.purdue.cs626.anonencrypt.AEParameters;

/**
 * Main application API that depends on an installation. 
 * (i.e. requires the configuration to be in the user home dir)
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

	public ContactPrivData createContactPrivData() {
		// TODO
		return null;
	}

}
