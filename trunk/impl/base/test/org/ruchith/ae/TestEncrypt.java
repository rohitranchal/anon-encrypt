package org.ruchith.ae;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.io.FileInputStream;
import java.io.InputStream;

import org.ruchith.ae.base.AECipherTextBlock;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.Decrypt;
import org.ruchith.ae.base.Encrypt;
import org.ruchith.ae.base.RootKeyGen;

import junit.framework.TestCase;

public class TestEncrypt extends TestCase {

	public void testDoEncrypt() throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);

		Pairing pairing = paramGen.getPairing();

		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);

		Field zr = pairing.getZr();

		Element id1 = zr.newRandomElement();
		System.out.println("ID:" + id1);

		AEPrivateKey contactPriv = rkg.genKey(id1, paramGen.getMasterKey());

		ContactKeyGen conKeyGen = new ContactKeyGen();
		conKeyGen.init(id1, contactPriv, params);
		Element id2 = conKeyGen.genRandomID();
		AEPrivateKey tmpPriv = conKeyGen.getTmpPrivKey(id2);

		Encrypt encrypt = new Encrypt();
		encrypt.init(params);

		Element plain = pairing.getGT().newRandomElement();

//		Element pubKey = params.getH1().powZn(id1)
//				.mul(params.getH2().powZn(id2));

		Element pubKey = conKeyGen.getTmpPubKey(id2);

		AECipherTextBlock ct = encrypt.doEncrypt(plain, pubKey);

		Decrypt decrypt = new Decrypt();
		decrypt.init(params);
		Element result = decrypt.doDecrypt(ct, tmpPriv);

		assertTrue(result.equals(plain));

		System.out.println(pubKey);

	}

}
