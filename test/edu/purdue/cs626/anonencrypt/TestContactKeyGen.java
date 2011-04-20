package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

public class TestContactKeyGen extends TestCase {

	public void testGenKey() throws Exception {
        CurveParams curveParams = new CurveParams();
        InputStream res = new FileInputStream("/home/ruchith/wp-j/anon_encrypt/keys/a_181_603.properties");
		curveParams.load(res);
		
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();
		
		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element id1 = paramGen.getPairing().getZr().newRandomElement();
		AEPrivateKey contactPriv = rkg.genKey(id1, paramGen.getMasterKey());
		
		ContactKeyGen conKeyGen = new ContactKeyGen();
		conKeyGen.init(id1, contactPriv, params);
		Element id2 = conKeyGen.genRandomID();
		AEPrivateKey tmpPriv = conKeyGen.genKey(id2); 
		
	}
}
