package edu.purdue.cs626.anonencrypt.app;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Statement;

import edu.purdue.cs626.anonencrypt.AEParameterGenerator;
import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.db.Database;

/**
 * Installer in charge of setting up parameters and the database. The database
 * and parameters stored in the .ae directory in the user home directory.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ApplicationInstaller {

	public void install() throws Exception {
		// Create new parameters
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		// Create the config dir
		String userHome = System.getProperty("user.home");
		String configDirPath = userHome + File.separator + Constants.CONFIG_DIR;
		File configDir = new File(configDirPath);

		if (configDir.exists()) {
			rmDir(configDir); // Get rid of any existing stuff
		}
		configDir.mkdir();

		// Save the param file
		String paramFilePath = configDirPath + File.separator
				+ Constants.PARAM_FILE_NAME;
		File paramFile = new File(paramFilePath);
		paramFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(paramFile);
		fos.write(params.serialize().getBytes());
		fos.flush();
		fos.close();

		// Save master key
		Element mkElem = paramGen.getMasterKey();
		saveMasterKey(mkElem);

		// Create database
		Connection conn = Database.getCreateConnection();

		Statement s = conn.createStatement();

		s.execute("CREATE TABLE Contact(" + "contactId varchar(100), "
				+ "id varchar(512), " + // The id that I give him
				"random varchar(512), " + // The r that I assign
				"privDataFromContact clob," + // The priv data that he gives me
				"myIDFromContact varchar(512)," + // The ID that he gives me
				"lastMsg varchar(512))");
				
		conn.commit();
		conn.close();

	}

	public static void saveMasterKey(Element mkElem) throws Exception {
		FileOutputStream fos;
		String userHome = System.getProperty("user.home");
		String configDirPath = userHome + File.separator + Constants.CONFIG_DIR;
		String mkFilePath = configDirPath + File.separator
				+ Constants.MASTER_KEY_FILE_NAME;
		File mkFile = new File(mkFilePath);
		if (mkFile.exists()) {
			mkFile.delete();
		}
		mkFile.createNewFile();
		fos = new FileOutputStream(mkFile);
		fos.write(mkElem.toBytes());
		fos.flush();
		fos.close();
	}

	/**
	 * Remove configuration directory
	 */
	public void unInstall() {
		String userHome = System.getProperty("user.home");
		String configDirPath = userHome + File.separator + Constants.CONFIG_DIR;
		File configDir = new File(configDirPath);
		rmDir(configDir);
	}
	
	public static boolean isInstalled() {
		String userHome = System.getProperty("user.home");
		String configDirPath = userHome + File.separator + Constants.CONFIG_DIR;
		File configDir = new File(configDirPath);
		return configDir.exists() && configDir.isDirectory();
	}

	/**
	 * Delete a directory and its content.
	 * 
	 * @param dir
	 *            The directory to be deleted
	 */
	private void rmDir(File dir) {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					// Call rmDir again
					rmDir(files[i]);
				} else {
					files[i].delete();
				}
			}
			// After deleting everything
			// delete the dir
			dir.delete();
		}
	}
	


}
