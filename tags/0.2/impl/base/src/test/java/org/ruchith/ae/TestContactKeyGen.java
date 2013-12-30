package org.ruchith.ae;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.RootKeyGen;

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

		System.out.println(contactPriv.serializeJSON());
		
		ContactKeyGen conKeyGen = new ContactKeyGen();
		conKeyGen.init(id1, contactPriv, params);
		Element id2 = conKeyGen.genRandomID();
		AEPrivateKey tmpPriv = conKeyGen.getTmpPrivKey(id2);
		System.out.println(tmpPriv.serializeJSON());
		
		
		String privKeyVal = contactPriv.serializeJSON().toString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode on = (ObjectNode)mapper.readTree(privKeyVal);
		
		AEPrivateKey newContactPriv = new AEPrivateKey(on, paramGen.getPairing());
		System.out.println(newContactPriv.serializeJSON());
		assertEquals(contactPriv.getC1(), newContactPriv.getC1());
		assertEquals(contactPriv.getC2(), newContactPriv.getC2());
		assertEquals(contactPriv.getC3(), newContactPriv.getC3());
		
	}
}
