package org.ruchith.ae;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.RootKeyGen;

public class TestRootKeyGen extends TestCase {

	public void testGenKey() throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 128)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element id1 = paramGen.getPairing().getZr().newRandomElement();
		AEPrivateKey pk = rkg.genKey(id1, paramGen.getMasterKey());
		System.out.println(pk.serializeJSON());
	}

}
