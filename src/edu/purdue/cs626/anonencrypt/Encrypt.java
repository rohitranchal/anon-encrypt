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
	 * @return An array of {@link AECipherTextBlock} objects
	 */
	public AECipherText doEncrypt(Element[] input, Element pubKey) {
		AECipherTextBlock[] blocks = new AECipherTextBlock[input.length];
		for(int i = 0; i < input.length; i++) {
			blocks[i] = doEncrypt(input[i], pubKey);
		}
		
		return new AECipherText(blocks);
	}
	
	/**
	 * Encrypt a given plaintext encoded element
	 * @param plainText The plaintext encoded element
	 * @param pubKey Public key element
	 * @return an {@link AECipherTextBlock} object
	 */
	public AECipherTextBlock doEncrypt(Element plainText, Element pubKey) {
		
		Element s = this.pairing.getZr().newRandomElement();
		
		Element tmp1 = this.pairing.pairing(this.params.getG1().getImmutable(), this.params.getG2().getImmutable()).powZn(s);
		
		Element a = tmp1.mul(plainText);
		
		Element b = this.params.getG().getImmutable().powZn(s);
		
		Element c = pubKey.getImmutable().mul(this.params.getG3().getImmutable()).powZn(s);
		
		return new AECipherTextBlock(a, b, c);
		
	}
	
}
