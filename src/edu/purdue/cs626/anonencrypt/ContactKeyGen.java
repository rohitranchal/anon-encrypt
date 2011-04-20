package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ContactKeyGen {
	
	private AEParameters params;
	private Element id;
	private AEPrivateKey privKey;
	private Pairing pairing;
	
	public void init(Element id, AEPrivateKey privKey, AEParameters params) {
		this.params = params;
		this.id = this.params.getH1().powZn(id);
		this.privKey = privKey;
		this.pairing = PairingFactory.getPairing(this.params.getCurveParams());
	}
	
	public Element genRandomID() {
		return this.pairing.getZr().newRandomElement();
	}
	
	public AEPrivateKey genKey(Element rndId) {
		Element t = this.pairing.getZr().newRandomElement();
		
		Element tmp1 = this.params.getH2().powZn(rndId);
		
		Element h1h2 = this.id.mul(tmp1);
		Element tmp2  = h1h2.mul(this.params.getG3());
		
		Element bkIk = this.privKey.getC3().get(0).powZn(rndId);
		
		Element a0BkIk = this.privKey.getC1().mul(bkIk);
		
		Element c1 = a0BkIk.mul(tmp2.powZn(t));
		
		Element c2 = this.privKey.getC2().mul(this.params.getG().powZn(t));
		
		ArrayList<Element> c3 = new ArrayList<Element>();
		c3.add(this.privKey.getC3().get(1).mul(this.params.getH3().powZn(t)));
		
		return new AEPrivateKey(c1, c2, c3);
	}

}
