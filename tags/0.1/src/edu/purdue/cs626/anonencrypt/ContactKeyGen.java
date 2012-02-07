package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;

/**
 * This is the ephemeral private key generation API to be used with a remote 
 * contact's parameters.
 * 
 * @author Ruchith Fernando
 *
 */
public class ContactKeyGen {
	
	private AEParameters params;
	private Element id;
	private AEPrivateKey privKey;
	
	public void init(Element id, AEPrivateKey privKey, AEParameters params) {
		this.params = params;
		this.id = this.params.getH1().powZn(id).getImmutable();
		this.privKey = privKey;
	}
	
	public Element genRandomID() {
		return this.params.getPairing().getZr().newRandomElement();
	}
	
	public AEPrivateKey getTmpPrivKey(Element rndId) {
		Element t = this.params.getPairing().getZr().newRandomElement();
		
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
	
	public Element getTmpPubKey(Element rndId) {
		return this.id.mul(params.getH2().powZn(rndId));
		
	}

}
