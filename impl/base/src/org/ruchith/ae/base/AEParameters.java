package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.ByteArrayInputStream;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Parameter wrapper. This holds all the public parameters required for
 * encryption. Once instantiated this will also hold an instance of the
 * {@link Pairing}.
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
	public AEParameters(ObjectNode on) {
		this.curveParams = new CurveParams();
		this.curveParams.load(new ByteArrayInputStream(Base64.decode(on.get(
				"curve").getTextValue())));

		this.pairing = PairingFactory.getPairing(this.curveParams);
		Field group1 = this.pairing.getG1();

		Element tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("g").getTextValue()));
		this.g = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("g1").getTextValue()));
		this.g1 = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("g2").getTextValue()));
		this.g2 = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("g3").getTextValue()));
		this.g3 = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("h1").getTextValue()));
		this.h1 = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("h2").getTextValue()));
		this.h2 = tmpElem.getImmutable();
		
		tmpElem = group1.newElement();
		tmpElem.setFromBytes(Base64.decode(on.get("h3").getTextValue()));
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

	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		on.put("curve", Base64.encode(this.curveParams.toString().getBytes()));
		on.put("g", Base64.encode(this.g.toBytes()));
		on.put("g1", Base64.encode(this.g1.toBytes()));
		on.put("g2", Base64.encode(this.g2.toBytes()));
		on.put("g3", Base64.encode(this.g3.toBytes()));
		on.put("h1", Base64.encode(this.h1.toBytes()));
		on.put("h2", Base64.encode(this.h2.toBytes()));
		on.put("h3", Base64.encode(this.h3.toBytes()));
		return on;
	}
}
