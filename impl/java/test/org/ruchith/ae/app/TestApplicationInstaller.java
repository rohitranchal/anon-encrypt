package org.ruchith.ae.app;

import it.unisa.dia.gas.jpbc.Element;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;
import org.ruchith.ae.app.ApplicationInstaller;
import org.ruchith.ae.app.Constants;
import org.ruchith.ae.app.db.Database;
import org.ruchith.ae.base.AEParameters;

import junit.framework.TestCase;

public class TestApplicationInstaller extends TestCase {

	public void testInstall() throws Exception {

		ApplicationInstaller installer = new ApplicationInstaller();
		installer.install();

		String userHome = System.getProperty("user.home");

		Connection conn = Database.getConnection();

		Statement s = conn.createStatement();

		s.execute("INSERT INTO Contact "
				+ "VALUES('Bob', '251789358979577744758182258194692528664', "
				+ "'324683896779935702435841186478040627037', '', '', '')");

		ResultSet rs = s.executeQuery("SELECT * FROM Contact");
		while (rs.next()) {
			assertEquals("Bob", rs.getString(1));
			assertEquals("251789358979577744758182258194692528664",
					rs.getString(2));
			assertEquals("324683896779935702435841186478040627037",
					rs.getString(3));
		}

		String paramPath = userHome + File.separator
				+ Constants.CONFIG_DIR + File.separator
				+ Constants.PARAM_FILE_NAME;
		StAXOMBuilder builder = new StAXOMBuilder(paramPath);
		OMElement paramElem = builder.getDocumentElement();
		AEParameters params = new AEParameters(paramElem);

		// Check whether we recover the parameters properly
		assertEquals(paramElem.toString(), params.serialize());

		String mkPath = userHome + File.separator
				+ Constants.CONFIG_DIR + File.separator
				+ Constants.MASTER_KEY_FILE_NAME;
		File mkFile = new File(mkPath);
		FileInputStream fis = new FileInputStream(mkFile);
		byte[] data = new byte[(int)mkFile.length()];
		fis.read(data, 0, data.length);
		fis.close();
		
		Element elem = params.getPairing().getG1().newElement();
		elem.setFromBytes(data);
		
		assertEquals(Base64.encode(data), Base64.encode(elem.toBytes()));
		
		// cleanup
		installer.unInstall();

	}
	
}
