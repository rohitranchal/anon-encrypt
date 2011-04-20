package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import org.bouncycastle.crypto.CipherParameters;

public class AEParameters implements CipherParameters {

	private CurveParams curveParams;
	private Element g;
	private Element g1;
	private Element g2;
	private Element g3;

	private Element h1;
	private Element h2;
	private Element h3;

	
	
	public AEParameters(CurveParams curveParams) {
		this.curveParams = curveParams;
	}

	public CurveParams getCurveParams() {
		return curveParams;
	}

	public Element getG() {
		return g;
	}

	public Element getG1() {
		return g1;
	}

	public Element getG2() {
		return g2;
	}

	public Element getG3() {
		return g3;
	}

	public Element getH1() {
		return h1;
	}

	public Element getH2() {
		return h2;
	}

	public Element getH3() {
		return h3;
	}

	void setG(Element g) {
		this.g = g;
	}

	void setG1(Element g1) {
		this.g1 = g1;
	}

	void setG2(Element g2) {
		this.g2 = g2;
	}

	void setG3(Element g3) {
		this.g3 = g3;
	}

	void setH1(Element h1) {
		this.h1 = h1;
	}

	void setH2(Element h2) {
		this.h2 = h2;
	}

	void setH3(Element h3) {
		this.h3 = h3;
	}
	
}
