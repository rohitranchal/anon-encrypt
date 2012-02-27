package org.ruchith.ae;

import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import junit.framework.TestCase;

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
               System.out.println(on);

               AEParameters newParams = new AEParameters(on);

               assertEquals(params.getCurveParams(), newParams.getCurveParams());
               assertEquals(params.getG(), newParams.getG());
               assertEquals(params.getG1(), newParams.getG1());
               assertEquals(params.getG2(), newParams.getG2());
               assertEquals(params.getG3(), newParams.getG3());
               assertEquals(params.getH1(), newParams.getH1());
               assertEquals(params.getH2(), newParams.getH2());
               assertEquals(params.getH3(), newParams.getH3());
       }
}