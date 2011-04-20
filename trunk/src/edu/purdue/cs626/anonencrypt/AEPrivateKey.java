package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

/**
 * This class represents a private key of a contact.
 * Also when a contact creates a temporary key to request data to be encrypted
 * this class will hold the temporary private key as well.
 * 
 * @author Ruchith Fernando
 *
 */
public class AEPrivateKey {

	private Element c1;
	private Element c2;
	private ArrayList<Element> c3;

	public AEPrivateKey(Element c1, Element c2, ArrayList<Element> c3) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		
	}

	public Element getC1() {
		return c1;
	}

	public Element getC2() {
		return c2;
	}

	public ArrayList<Element> getC3() {
		return this.c3;
	}
	
	public String serialize() {
		String output = "<AEPrivateKey>\n";
		output += "<C1>" + this.c1 + "</C1>";
		output += "<C2>" + this.c2 + "</C2>";
		output += "<C3>" + this.c3 + "</C3>";
		output += "</AEPrivateKey>";
		return output;
	}

}
