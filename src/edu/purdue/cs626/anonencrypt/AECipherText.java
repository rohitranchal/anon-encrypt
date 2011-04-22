package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;
import java.util.Iterator;

import it.unisa.dia.gas.jpbc.Pairing;

import org.apache.axiom.om.OMElement;

public class AECipherText {
	
	private AECipherTextBlock[] blocks;
	
	public AECipherText(AECipherTextBlock[] blocks) {
		this.blocks = blocks;
	}
	
	public AECipherText(OMElement elem, Pairing pairing) {
		ArrayList<AECipherTextBlock> blockList = new ArrayList<AECipherTextBlock>();
		Iterator<OMElement> blockElems = elem.getChildrenWithLocalName("CipherTextBlock");
		while (blockElems.hasNext()) {
			OMElement blockElem = (OMElement) blockElems.next();
			blockList.add(new AECipherTextBlock(blockElem, pairing));
		}

		this.blocks = blockList.toArray(new AECipherTextBlock[blockList.size()]);
	}

	public String serialize() {
		String ouput = "<CipherText>\n";
		for(int i = 0; i < this.blocks.length; i ++) {
			ouput = this.blocks[i].serialize() + "\n";
		}
		ouput += "</CipherText>";
		return ouput;
	}

	public AECipherTextBlock[] getBlocks() {
		return blocks;
	}
	
}
