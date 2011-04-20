package edu.purdue.cs626.anonencrypt;

import java.io.FileInputStream;
import java.io.InputStream;

import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import junit.framework.TestCase;

public class TestAEParameterGenerator extends TestCase {

	public void testGenerateParameters() throws Exception {
		
        CurveParams curveParams = new CurveParams();
        InputStream res = new FileInputStream("/home/ruchith/wp-j/anon_encrypt/keys/a_181_603.properties");
		curveParams.load(res);
		
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		paramGen.generateParameters();
		
	}
}
