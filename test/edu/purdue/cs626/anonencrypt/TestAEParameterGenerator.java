package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

public class TestAEParameterGenerator extends TestCase {

	public void testGenerateParameters() throws Exception {

		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		paramGen.generateParameters();

	}
}
