package org.ruchith.ae.base;

import it.unisa.dia.gas.jpbc.Pairing;

import java.util.ArrayList;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class AECipherText {
	
	private AECipherTextBlock[] blocks;
	
	public AECipherText(AECipherTextBlock[] blocks) {
		this.blocks = blocks;
	}
	
	public AECipherText(ArrayNode an, Pairing pairing) {
		ArrayList<AECipherTextBlock> blockList = new ArrayList<AECipherTextBlock>();
		for(int i = 0; i < an.size(); i++) {
			ObjectNode on = (ObjectNode)an.get(i);
			blockList.add(new AECipherTextBlock(on, pairing));
		}

		this.blocks = blockList.toArray(new AECipherTextBlock[blockList.size()]);
	}

	
	public ArrayNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode an = mapper.createArrayNode();
		for (int i = 0; i < this.blocks.length; i++) {
			an.add(this.blocks[i].serializeJSON());
		}
		
		return an;
	}
	

	public AECipherTextBlock[] getBlocks() {
		return blocks;
	}
	
}
