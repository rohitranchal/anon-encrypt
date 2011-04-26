package edu.purdue.cs626.anonencrypt.app.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.purdue.cs626.anonencrypt.ReKeyInformation;
import edu.purdue.cs626.anonencrypt.Util;
import edu.purdue.cs626.anonencrypt.app.Application;
import edu.purdue.cs626.anonencrypt.app.ApplicationInstaller;
import edu.purdue.cs626.anonencrypt.app.ContactPrivData;
import edu.purdue.cs626.anonencrypt.app.UpdateRequest;

public class Messenger {

	private static Application app;

	private static void printUsage() {
		System.out.println("Options (Help 0):");

		System.out.println("1.) List contacts");
		System.out.println("2.) Add contact");
		System.out.println("3.) Add contact : enter remote priv data");
		System.out.println("4.) Set direct update of contact");
		System.out.println("5.) Request remote update of contact");
		System.out.println("6.) Serve update request");
		System.out.println("7.) Process remote update of contact");
		System.out.println("8.) Remove contact");
		System.out.println("9.) Re-key system");
		System.out.println("10.) Process contact re-key information");
		System.out.println("20.) Install");
		System.out.println("21.) Uninstall");
		System.out.println("22.) Exit!!!");
	}

	public static void main(String[] args) throws Exception {

		if (ApplicationInstaller.isInstalled()) {
			app = new Application();
		}
		boolean start = true;
		int selection = 0;
		while (selection != 22) {
			if (start) {
				printUsage();
				start = false;
			}
			System.out.println("Enter option: ");

			BufferedReader stdin = new BufferedReader(new InputStreamReader(
					System.in));
			String value;
			value = stdin.readLine();

			if (value.equals("")) {
				continue;
			}

			selection = Integer.parseInt(value);
			switch (selection) {
			case 1:
				printContactList();
				break;
			case 2:
				addContact(stdin);
				break;
			case 3:
				processRemotePrivData(stdin);
				break;
			case 4:
				doDirectMsg(stdin);
				break;
			case 5:
				remoteUpdateReq(stdin);
				break;
			case 6:
				serveUpdateReq(stdin);
				break;
			case 7:
				doProcessUpdateResponse(stdin);
				break;
			case 8:
				removeContact(stdin);
				break;
			case 9:
				doReKey(stdin);
				break;
			case 10:
				processContactReKey(stdin);
				break;
			case 20:
				install();
				break;
			case 21:
				unInstall();
				break;
			case 0:
				printUsage();
				break;
			case 22:
				exit();
				break;
			default:
				break;
			}
		}

	}

	private static void processContactReKey(BufferedReader stdin) throws Exception {
		System.out.println("Enter contact name :");
		String user = stdin.readLine();
		
		String data = getInputFromZenity("Enter re-key information from " + user);
		boolean result = app.processReKey(user, data);
		if(result) {
			System.out.println("Update successful!");
		} else {
			System.out.println("Seems like " + user + 
					" doesn't consider you to be a friend anymore :P");
		}
	}

	private static void doReKey(BufferedReader stdin) throws Exception {
		System.out.println("Are you sure (Y/N)?");
		String resp = stdin.readLine();
		if(resp.equalsIgnoreCase("Y")) {
			ReKeyInformation rki = app.reKey();
			System.out.println("--------------RE-KEY_INFO:START--------------");
			String info = rki.serialize();
			System.out.println(info);
			System.out.println("--------------RE-KEY_INFO:END----------------");
		}
	}

	private static void removeContact(BufferedReader stdin) throws IOException,
			Exception {
		System.out.println("Enter contact name :");
		String user = stdin.readLine();
		boolean result = app.removeContact(user);
		if (result) {
			System.out.println(user + " removed successfully!");
		}
	}

	private static void doProcessUpdateResponse(BufferedReader stdin)
			throws IOException, FileNotFoundException, Exception {
		String data = getInputFromZenity("Enter update response");
		String msg = app.processUpdateResponse(data);

		System.out.println("Update message : " + msg);
	}

	private static void serveUpdateReq(BufferedReader stdin)
			throws IOException, FileNotFoundException, Exception {
		System.out.println("Enter update request : ");
	

		String data = getInputFromZenity("Enter update request");
		String resp = app.getUpdate(data);
		System.out
				.println("-----------REMOTE_UPDATE_RESPONSE:START-----------");
		System.out.println(resp);
		System.out
				.println("-----------REMOTE_UPDATE_RESPONSE:END-------------");
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
		if (app != null) {
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
		if (app != null) {
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

		String data = getInputFromZenity("Enter private cert from " + user);
		app.registerContact(user, new ContactPrivData(Util.getOMElement(data)));
		System.out.println("Private certificate installed successfully!\n");
	}

	private static void doDirectMsg(BufferedReader stdin) throws IOException,
			Exception {
		System.out.println("Enter contact name:");
		String user = stdin.readLine();
		System.out.println("Enter plain message that you received from " + user + " :");
		String msg = stdin.readLine();
		app.saveMessage(user, msg);
		System.out.println(user + " >> " + msg);
	}

	private static void printContactList() throws Exception {
		if (app == null) {
			printInstallMsg();
		}
		String[] contacts = app.getContactList();
		System.out.println("========================================");
		System.out.println("|          CONTACT LIST                |");
		System.out.println("========================================");

		for (int i = 0; i < contacts.length; i++) {
			System.out.println(contacts[i] + " >> "
					+ app.getMessage(contacts[i]));
		}
		System.out.println("========================================");

	}

	private static void addContact(BufferedReader stdin) throws IOException,
			Exception {
		if (app == null) {
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
	
	private static String getInputFromZenity(String title) throws Exception {
		String val = "";
		String[] cmd = new String[]{"zenity", "--text-info", "--editable", "--title=" + title + ""};
		
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String data = null;
		while((data = in.readLine()) != null) {
			val += data + "\n";
		}
		System.out.println(val);
		return val;
	}
	
	
}
