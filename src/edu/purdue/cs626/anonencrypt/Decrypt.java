package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Decrypt {
	private AEParameters params;
	private Pairing pairing;
	
	public void init(AEParameters params) {
		this.params = params;
		this.pairing = PairingFactory.getPairing(this.params.getCurveParams());
	}

	public Element doDecrypt(AECipherText cipherText, AEPrivateKey privateKey) {
		
		Element tmp1 = cipherText.getA().mul(this.pairing.pairing(privateKey.getC2(), cipherText.getC()));
		Element tmp2 = this.pairing.pairing(cipherText.getB(), privateKey.getC1());
		
		return tmp1.div(tmp2);
	}
	
	public Element[] doDecrypt(AECipherText[] cipherText, AEPrivateKey privateKey) {
		
		Element[] output = new Element[cipherText.length];
		for(int i = 0; i < cipherText.length; i++) {
			output[i] = this.doDecrypt(cipherText[i], privateKey);
		}
		
		return output;
	}
}
