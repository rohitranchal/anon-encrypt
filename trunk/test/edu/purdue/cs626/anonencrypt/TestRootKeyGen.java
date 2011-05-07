package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

public class TestRootKeyGen extends TestCase {

	public void testGenKey() throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element id1 = paramGen.getPairing().getZr().newRandomElement();
		AEPrivateKey pk = rkg.genKey(id1, paramGen.getMasterKey());
		System.out.println(pk.serialize());
	}

}
