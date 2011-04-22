package edu.purdue.cs626.anonencrypt;

public class AECipherText {
	
	private AECipherTextBlock[] blocks;
	
	public AECipherText(AECipherTextBlock[] blocks) {
		this.blocks = blocks;
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
