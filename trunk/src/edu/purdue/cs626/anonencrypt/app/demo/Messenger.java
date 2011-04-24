package edu.purdue.cs626.anonencrypt.app.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.purdue.cs626.anonencrypt.Util;
import edu.purdue.cs626.anonencrypt.app.Application;
import edu.purdue.cs626.anonencrypt.app.ApplicationInstaller;
import edu.purdue.cs626.anonencrypt.app.ContactPrivData;
import edu.purdue.cs626.anonencrypt.app.UpdateRequest;

public class Messenger {

	private static Application app;
	
	private static void printUsage() {
		System.out.println("Options (Help 0):");
		System.out.println("1.) Install");
		System.out.println("2.) List contacts");
		System.out.println("3.) Add contact");
		System.out.println("4.) Add contact : enter remote priv data");
		System.out.println("5.) Set direct update of contact");
		System.out.println("6.) Request remote update of contact");
		System.out.println("7.) Serve update request");
		System.out.println("8.) Process remote update of contact");
		System.out.println("9.) Uninstall");
		System.out.println("10.) Exit!!!");
	}
	
	public static void main(String[] args) throws Exception {

		if(ApplicationInstaller.isInstalled()) {
			app = new Application();
		}
		boolean start = true;
		int selection = 0;
		while(selection != 10) {
			if(start) {
				printUsage();
				start = false;
			}
			System.out.println("Enter option: ");
			
			BufferedReader stdin = new BufferedReader
		      						(new InputStreamReader(System.in));
			String value;
			value = stdin.readLine();
			
			if(value.equals("")) {
				continue;
			}
			
			selection = Integer.parseInt(value);
			switch(selection) {
			case 1:
				install();
				break;
			case 2:
				printContactList();
				break;
			case 3:
				addContact(stdin);
				break;
			case 4:
				processRemotePrivData(stdin);
				break;
			case 5:
				doDirectMsg(stdin);
				break;
			case 6:
				remoteUpdateReq(stdin);
				break;
			case 7:
				serveUpdateReq(stdin);		
				break;
			case 8:
				System.out.println("Enter update response file path : ");
				String path = stdin.readLine();
				
				String data = getFileData(path);
				String msg = app.processUpdateResponse(data);
				
				System.out.println("Update message : " + msg);
				break;
			case 9:
				unInstall();
				break;
			case 0:
				printUsage();
				break;
			case 10:
				exit();
				break;
			default:
				break;
			}
		}
		
	}

	private static void serveUpdateReq(BufferedReader stdin)
			throws IOException, FileNotFoundException, Exception {
		System.out.println("Enter update request file path : ");
		String path = stdin.readLine();
		
		String data = getFileData(path);
		String resp = app.getUpdate(data);
		System.out.println("-----------REMOTE_UPDATE_RESPONSE:START-----------");
		System.out.println(resp);
		System.out.println("-----------REMOTE_UPDATE_RESPONSE:END-------------");
	}

	private static void remoteUpdateReq(BufferedReader stdin)
			throws IOException, Exception {
		System.out.println("Enter contact name :");
		String user = stdin.readLine();
		
		UpdateRequest ur = app.getUpdateRequest(user);
		
		System.out.println("-----------REMOTE_UPDATE_REQUEST:START-----------");
		System.out.println(ur.serialize());
		System.out.println("-----------REMOTE_UPDATE_REQUEST:END-------------");
	}

	private static void install() throws Exception {
		if(app != null) {
			System.out.println("Aleady installed");
		} else {
			new ApplicationInstaller().install();
			app = new Application();
			System.out.println("Installation complete!");
		}
	}

	private static void exit() {
		System.out.println("BYE!");
		System.exit(0);
	}

	private static void unInstall() {
		if(app != null) {
			new ApplicationInstaller().unInstall();
			System.out.println("Removed!");
			app = null;
		} else {
			printInstallMsg();
		}
	}

	private static void processRemotePrivData(BufferedReader stdin)
			throws IOException, FileNotFoundException, Exception {
		System.out.println("Enter contact name :");
		String user = stdin.readLine();
		
		System.out.println("Enter file path :");
		String dataFilePath = stdin.readLine();
		String data = getFileData(dataFilePath);
		app.registerContact(user, new ContactPrivData(Util.getOMElement(data)));
	}

	private static String getFileData(String dataFilePath)
			throws FileNotFoundException, IOException {
		File f = new File(dataFilePath);
		
		StringBuffer data = new StringBuffer();
		
		if(f.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = null;
			while((line = reader.readLine()) != null) {
				data.append(line).append(System.getProperty("line.separator"));
			}
			reader.close();
		} else {
			System.out.println("ERROR: no such file!");
		}
		return data.toString();
	}

	private static void doDirectMsg(BufferedReader stdin) throws IOException,
			Exception {
		System.out.println("Enter contact name:");
		String user = stdin.readLine();
		System.out.println("Enter plain message:");
		String msg = stdin.readLine();
		app.saveMessage(user, msg);
		System.out.println(user  + " >> " + msg);
	}

	private static void printContactList() throws Exception {
		if(app == null) {
			printInstallMsg();
		}
		String[] contacts = app.getContactList();
		System.out.println("========================================");
		System.out.println("|          CONTACT LIST                |");
		System.out.println("========================================");
		
		for (int i = 0; i < contacts.length; i++) {
			System.out.println(contacts[i] + " >> " + app.getMessage(contacts[i]));
		}
		System.out.println("========================================");
		
	}

	private static void addContact(BufferedReader stdin) throws IOException,
			Exception {
		if(app == null) {
			printInstallMsg();
		}
		System.out.println("Enter contact name: ");
		System.out.flush();
		String name = stdin.readLine();
		ContactPrivData privData = app.createContact(name);
		System.out.println("---------------PRIVATE_DATA:START---------------");
		System.out.println(privData.serialize());
		System.out.println("---------------PRIVATE_DATA:END-----------------");
	}
	
	private static void printInstallMsg() {
		System.out.println("Please install application!");
	}
}
