package edu.purdue.cs626.anonencrypt;

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
		//TODO
		return null;
	}
}
