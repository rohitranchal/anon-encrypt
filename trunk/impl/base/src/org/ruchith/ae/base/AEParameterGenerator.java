package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class AEParameterGenerator {

	private CurveParams curveParams;
	private Pairing pairing;
	private Element alpha;
	private Element mk;
	
	public void init(CurveParams curveParams) {
		this.curveParams = curveParams;
		this.pairing = PairingFactory.getPairing(this.curveParams);
	}
	
	public AEParameters generateParameters() {
		
		AEParameters params = new AEParameters(this.curveParams);
		
		Field group1 = this.pairing.getG1();
		Element g = group1.newRandomElement().getImmutable();
		params.setG(g);
		
		this.alpha = this.pairing.getZr().newRandomElement().getImmutable();
		Element g1 = g.powZn(this.alpha);
		params.setG1(g1);
		
		Element g2 = group1.newRandomElement().getImmutable();
		params.setG2(g2);
		params.setG3(group1.newRandomElement().getImmutable());
		params.setH1(group1.newRandomElement().getImmutable());
		params.setH2(group1.newRandomElement().getImmutable());
		params.setH3(group1.newRandomElement().getImmutable());
		
		this.mk = g2.powZn(this.alpha).getImmutable();
		
		return params;
	}

	public Element getMasterKey() {
		return this.mk;
	}
	
	public Pairing getPairing() {
		return pairing;
	}	
}
