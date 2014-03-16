package org.ruchith.ae;

import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;

public class TestAEParameterGenerator extends TestCase {

       public void testGenerateParameters() throws Exception {

               CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
                               .generate();

               AEParameterGenerator paramGen = new AEParameterGenerator();
               paramGen.init(curveParams);
               AEParameters params = paramGen.generateParameters();

               ObjectNode on = params.serializeJSON();
               
               ObjectMapper mapper = new ObjectMapper();
               ObjectNode newOn = (ObjectNode)mapper.readTree(on.toString());

               AEParameters newParams = new AEParameters(newOn);

               assertEquals(params.getCurveParams(), newParams.getCurveParams());
               assertEquals(params.getG(), newParams.getG());
               assertEquals(params.getG1(), newParams.getG1());
               assertEquals(params.getG2(), newParams.getG2());
               assertEquals(params.getG3(), newParams.getG3());
               assertEquals(params.getH1(), newParams.getH1());
               assertEquals(params.getH2(), newParams.getH2());
               assertEquals(params.getH3(), newParams.getH3());
               
               //Generator check
               Element tmpG = params.getG();
               BigInteger order = params.getPairing().getG1().getOrder();
               Element mul = tmpG.mul(order);
               //g * order == identity
               assertTrue(mul.isZero());
               //g == g * (order + 1)
               assertEquals(tmpG, mul.add(tmpG));
       }
}