package edu.purdue.cs626.anonencrypt;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

public class TestContactKeyGen extends TestCase {

	public void testGenKey() throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element id1 = paramGen.getPairing().getZr().newRandomElement();
		AEPrivateKey contactPriv = rkg.genKey(id1, paramGen.getMasterKey());

		System.out.println(contactPriv.serialize());
		
		ContactKeyGen conKeyGen = new ContactKeyGen();
		conKeyGen.init(id1, contactPriv, params);
		Element id2 = conKeyGen.genRandomID();
		AEPrivateKey tmpPriv = conKeyGen.genKey(id2);
		
		System.out.println(tmpPriv.serialize());
		
		
		byte[] bytes = contactPriv.serialize().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		OMElement elem = new StAXOMBuilder(is).getDocumentElement();
		AEPrivateKey newContactPriv = new AEPrivateKey(elem, paramGen.getPairing());

		
		assertEquals(contactPriv.getC1(), newContactPriv.getC1());
		assertEquals(contactPriv.getC2(), newContactPriv.getC2());
		assertEquals(contactPriv.getC3(), newContactPriv.getC3());
	}
}
