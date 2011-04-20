package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

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

}
