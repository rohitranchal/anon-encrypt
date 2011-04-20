package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class RootKeyGen {

	private AEParameters params;
	
	
	public void init(AEParameters params) {
		this.params = params;
	}

	public AEPrivateKey genKey(Element id, Element masterKey) {
		
		
		Element h1 = this.params.getH1();
		Element h1I1 = h1.powZn(id);
		
		Pairing pairing = PairingFactory.getPairing(this.params.getCurveParams());
		
		Element r = pairing.getZr().newRandomElement();
		System.out.println(r);
		
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
