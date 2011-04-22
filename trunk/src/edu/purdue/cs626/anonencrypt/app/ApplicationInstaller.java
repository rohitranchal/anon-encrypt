package edu.purdue.cs626.anonencrypt.app;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Statement;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import edu.purdue.cs626.anonencrypt.AEParameterGenerator;
import edu.purdue.cs626.anonencrypt.AEParameters;
import edu.purdue.cs626.anonencrypt.db.Database;

/**
 * Installer inchage of setting up parameters and the database.
 * The database and parameters stored in the .ae directory in the user home 
 * directory.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ApplicationInstaller {

	public static final String PARAM_FILE_NAME = "parameters.xml";
	public static final String CONFIG_DIR = ".ae";
	public static final String DB_NAME = "db";
	public static final String MASTER_KEY_FILE_NAME = "mk";
	
	
	public void install() throws Exception {
		// Create new parameters
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();
		
		//Create the config dir
		String userHome = System.getProperty("user.home");
		String configDirPath = userHome + File.separator + CONFIG_DIR;
		File configDir = new File(configDirPath);
		
		if(configDir.exists()) {
			rmDir(configDir); //Get rid of any existing stuff
		}
		configDir.mkdir();
		
		//Save the param file
		String paramFilePath = configDirPath + File.separator + PARAM_FILE_NAME;
		File paramFile = new File(paramFilePath);
		paramFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(paramFile);
		fos.write(params.serialize().getBytes());
		fos.flush();
		fos.close();
		
		//Save master key
		Element mkElem = paramGen.getMasterKey();
		String mkFilePath = configDirPath + File.separator + MASTER_KEY_FILE_NAME;
		File mkFile = new File(mkFilePath);
		mkFile.createNewFile();
		fos = new FileOutputStream(mkFile);
		fos.write(mkElem.toBytes());
		fos.flush();
		fos.close();

		
		// Create database
		String dbPath = configDirPath + File.separator + DB_NAME;
		Connection conn = Database.getCreateConnection(dbPath);
		
        Statement s = conn.createStatement();
        
        
        s.execute("CREATE TABLE Contact(friendId varchar(100), " +
        				"id varchar(512), random varchar(512), privData clob)");
        
        conn.commit();
        conn.close();
        
		
	}
	
	public void unInstall() {
		String userHome = System.getProperty("user.home");
		String configDirPath = userHome + File.separator + CONFIG_DIR;
		File configDir = new File(configDirPath);
		rmDir(configDir);
	}
	
	public void rmDir(File dir) {
		if(dir.exists()) {
			File[] files = dir.listFiles();
			for(int i = 0; i < files.length; i++) {
				if(files[i].isDirectory()) {
					//Call rmDir again
					rmDir(files[i]);
				} else {
					files[i].delete();
				}
			}
			//After deleting everything
			//delete the dir
			dir.delete();
		}
	}

}
