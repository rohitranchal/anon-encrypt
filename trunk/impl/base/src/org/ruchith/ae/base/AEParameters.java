package org.ruchith.ae.base;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.bouncycastle.crypto.CipherParameters;

/**
 * Parameter wrapper.
 * This holds all the public parameters required for encryption.
 * Once instantiated this will also hold an instance of the {@link Pairing}.
 * 
 * @author Ruchith Fernando
 *
 */
public class AEParameters implements CipherParameters {

	private CurveParams curveParams;
	private Element g;
	private Element g1;
	private Element g2;
	private Element g3;

	private Element h1;
	private Element h2;
	private Element h3;
	
	private Pairing pairing;

	/**
	 * Used to instantiate with a stored/transferred parameter file.
	 * 
	 * @param elem
	 */
	public AEParameters(OMElement elem) {
		OMElement curveElem = elem.getFirstChildWithName(new QName("Curve"));
		this.curveParams = new CurveParams();
		this.curveParams.load(new ByteArrayInputStream(curveElem.getText()
				.getBytes()));

		this.pairing = PairingFactory.getPairing(this.curveParams);
		Field group1 = this.pairing.getG1();

		OMElement gElem = elem.getFirstChildWithName(new QName("G"));
		Element tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(gElem.getText()));
		this.g = tmpElem.getImmutable();

		OMElement g1Elem = elem.getFirstChildWithName(new QName("G1"));
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(g1Elem.getText()));
		this.g1 = tmpElem.getImmutable();

		OMElement g2Elem = elem.getFirstChildWithName(new QName("G2"));
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(g2Elem.getText()));
		this.g2 = tmpElem.getImmutable();

		OMElement g3Elem = elem.getFirstChildWithName(new QName("G3"));
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(g3Elem.getText()));
		this.g3 = tmpElem.getImmutable();

		OMElement h1Elem = elem.getFirstChildWithName(new QName("H1"));
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(h1Elem.getText()));
		this.h1 = tmpElem.getImmutable();

		OMElement h2Elem = elem.getFirstChildWithName(new QName("H2"));
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(h2Elem.getText()));
		this.h2 = tmpElem.getImmutable();

		OMElement h3Elem = elem.getFirstChildWithName(new QName("H3"));
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(h3Elem.getText()));
		this.h3 = tmpElem.getImmutable();

	}

	public AEParameters(CurveParams curveParams) {
		this.curveParams = curveParams;
		this.pairing = PairingFactory.getPairing(this.curveParams);
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

	public void setG1(Element g1) {
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

	public Pairing getPairing() {
		return pairing;
	}

	public String serialize() {
		String output = "<AEParameters>\n";
		output += "<Curve>" + this.curveParams.toString() + "</Curve>\n";
		output += "<G>" + Base64.encode(this.g.toBytes()) + "</G>\n";
		output += "<G1>" + Base64.encode(this.g1.toBytes()) + "</G1>\n";
		output += "<G2>" + Base64.encode(this.g2.toBytes()) + "</G2>\n";
		output += "<G3>" + Base64.encode(this.g3.toBytes()) + "</G3>\n";
		output += "<H1>" + Base64.encode(this.h1.toBytes()) + "</H1>\n";
		output += "<H2>" + Base64.encode(this.h2.toBytes()) + "</H2>\n";
		output += "<H3>" + Base64.encode(this.h3.toBytes()) + "</H3>\n";
		output += "</AEParameters>";
		return output;
	}

}
