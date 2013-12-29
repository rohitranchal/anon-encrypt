package org.ruchith.ae;

import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.TextEncoder;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

public class TestTextEncoder extends TestCase {

	public void testEncodeDecode() throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		TextEncoder encoder = new TextEncoder();
		encoder.init(params);

		String input = "The quick brown fox jumps over the lazy dog";
		Element[] out = encoder.encode(input.trim());
		String compareWith = new String(encoder.decode(out));
		System.out.println(">>" + input + "<<");
		System.out.println(">>" + compareWith.trim() + "<<");
		assertEquals("decoding failure", input.trim(), compareWith.trim());
	}
}
