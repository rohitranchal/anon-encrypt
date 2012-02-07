package org.ruchith.ae.app;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.ruchith.ae.app.ContactPrivData;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.RootKeyGen;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

public class TestContactPrivData extends TestCase {

	public void testSerialize() throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element id1 = paramGen.getPairing().getZr().newRandomElement();
		AEPrivateKey contactPriv = rkg.genKey(id1, paramGen.getMasterKey());

		ContactPrivData cert = new ContactPrivData(params, id1, contactPriv);
		
		System.out.println(cert.serialize());
		
		
		OMElement elem = new StAXOMBuilder(new ByteArrayInputStream(cert.serialize().getBytes())).getDocumentElement();
		ContactPrivData newData = new ContactPrivData(elem);
		
		assertEquals(id1, newData.getId());
		assertEquals(contactPriv.getC1(), newData.getPrivKey().getC1());
		assertEquals(contactPriv.getC2(), newData.getPrivKey().getC2());
		assertEquals(contactPriv.getC3(), newData.getPrivKey().getC3());
		
	}

}
