package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Encrypt {
	
	private AEParameters params;
	private Pairing pairing;
	
	public void init(AEParameters params) {
		this.params = params;
		this.pairing = PairingFactory.getPairing(this.params.getCurveParams());
	}

	/**
	 * Encrypt a set of given plaintext encoded elements
	 * @param input Array of plaintext encoded elements
	 * @param pubKey Public key element
	 * @return An array of {@link AECipherText} objects
	 */
	public AECipherText[] doEncrypt(Element[] input, Element pubKey) {
		AECipherText[] output = new AECipherText[input.length];
		for(int i = 0; i < input.length; i++) {
			output[i] = doEncrypt(input[i], pubKey);
		}
		return output;
	}
	
	/**
	 * Encrypt a given plaintext encoded element
	 * @param plainText The plaintext encoded element
	 * @param pubKey Public key element
	 * @return an {@link AECipherText} object
	 */
	public AECipherText doEncrypt(Element plainText, Element pubKey) {
		
		Element s = this.pairing.getZr().newRandomElement();
		
		Element tmp1 = this.pairing.pairing(this.params.getG1(), this.params.getG2()).powZn(s);
		
		Element a = tmp1.mul(plainText);
		
		Element b = this.params.getG().powZn(s);
		
		Element c = pubKey.mul(this.params.getG3()).powZn(s);
		
		return new AECipherText(a, b, c);
		
	}
	
}
