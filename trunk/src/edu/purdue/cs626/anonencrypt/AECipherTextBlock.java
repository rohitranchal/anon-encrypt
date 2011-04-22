package edu.purdue.cs626.anonencrypt;

import org.apache.axiom.om.util.Base64;

import it.unisa.dia.gas.jpbc.Element;

/**
 * A block of cipher text.
 * 
 * @author Ruchith Fernando
 *
 */
public class AECipherTextBlock {
	
	private Element a;
	private Element b;
	private Element c;

	public AECipherTextBlock(Element a, Element b, Element c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Element getA() {
		return a;
	}

	public Element getB() {
		return b;
	}

	public Element getC() {
		return c;
	}

	public String serialize() {
		String output = "<CipherTextBlock>\n";
		
		output += "<A>" + Base64.encode(this.a.toBytes()) + "</A>\n";
		output += "<B>" + Base64.encode(this.b.toBytes()) + "</B>\n";
		output += "<C>" + Base64.encode(this.c.toBytes()) + "</C>\n";
		output += "</CipherTextBlock>";
		
		return output;
	}
}
