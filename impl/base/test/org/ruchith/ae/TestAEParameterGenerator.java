package org.ruchith.ae;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;

import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

public class TestAEParameterGenerator extends TestCase {

	public void testGenerateParameters() throws Exception {

		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();
		
		System.out.println(params.serialize());
		
/*		byte[] bytes = params.serialize().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		OMElement elem = new StAXOMBuilder(is).getDocumentElement();
		AEParameters newParams = new AEParameters(elem);
		
		assertEquals(params.getCurveParams(), newParams.getCurveParams());
		assertEquals(params.getG(), newParams.getG());
		assertEquals(params.getG1(), newParams.getG1());
		assertEquals(params.getG2(), newParams.getG2());
		assertEquals(params.getG3(), newParams.getG3());
		assertEquals(params.getH1(), newParams.getH1());
		assertEquals(params.getH2(), newParams.getH2());
		assertEquals(params.getH3(), newParams.getH3());
		*/
	}
}
