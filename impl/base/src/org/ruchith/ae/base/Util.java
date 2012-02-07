package org.ruchith.ae.base;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

/**
 * 
 * This include utilities such as re-key system.
 * 
 * @author Ruchith Fernando
 *
 */
public class Util {

	public OMElement reKeySytem(AEParameters params) {
		//TODO
		return null;
	}
	
	public static OMElement getOMElement(String elemStr) throws Exception {
		StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(
				elemStr.getBytes()));
		return builder.getDocumentElement();
	}
	
}
