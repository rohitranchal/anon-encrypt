package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;

/**
 * This is the private key generation API used by a owner of the parameters when
 * creating a contact's private key.
 * 
 * @author Ruchith Fernando
 * 
 */
public class RootKeyGen {

	private AEParameters params;

	public void init(AEParameters params) {
		this.params = params;
	}

	public AEPrivateKey genKey(Element id, Element masterKey) {

		Element r = this.params.getPairing().getZr().newRandomElement();
		return this.genKey(id, masterKey, r);
	}

	public AEPrivateKey genKey(Element id, Element masterKey, Element r) {

		Element h1 = this.params.getH1();
		Element h1I1 = h1.powZn(id);

		return genAnonKey(h1I1, masterKey, r);
	}
	
	/**
	 * This is the case where the remote identity is blinded.
	 *  
	 * @param anonId
	 * @param masterKey
	 * @param r
	 * @return
	 */
	public AEPrivateKey genAnonKey(Element anonId, Element masterKey, Element r) {

		Element h1I1 = anonId;

		Element g3 = this.params.getG3();
		Element tmp1 = h1I1.mul(g3);
		Element tmp2 = tmp1.powZn(r);

		Element c1 = masterKey.mul(tmp2);

		Element c2 = this.params.getG().powZn(r);

		ArrayList<Element> c3 = new ArrayList<Element>();
		c3.add(this.params.getH2().powZn(r));
		c3.add(this.params.getH3().powZn(r));

		return new AEPrivateKey(c1, c2, c3);
	}
}
