package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;
import java.util.Arrays;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class TextEncoder {
	
	private AEParameters params;
	private Pairing pairing;
	private Field gt;
	private byte pad = (byte) 0;
	
	public void init(AEParameters params) {
		this.params = params;
		this.pairing = PairingFactory.getPairing(this.params.getCurveParams());
		this.gt = this.pairing.getGT();
	}
	
	public int getBlockSize() {
		return this.gt.getLengthInBytes();
	}
	
	public Element[] encode(String input) {
		//Break input into blocks
		int len = this.getBlockSize();
		byte[] inputBytes = input.getBytes();
		int inputLen = inputBytes.length;
		ArrayList<byte[]> blocks = new ArrayList<byte[]>();
		for(int i = 0; i <= inputLen/len; i++) {
			int left = len;
			if(i == inputLen/len) {
				left = inputLen - i*len;
			}
			byte[] block = new byte[left];
			System.arraycopy(inputBytes, i*len, block, 0, left);
			blocks.add(block);
		}
		return this.encode(blocks.toArray(new byte[inputLen/len + 1][]));
	}
	
	public Element[] encode(byte[][] input) {
		ArrayList<Element> output = new ArrayList<Element>();
		
		for(int i = 0; i < input.length; i++) {
			int blockSize = this.getBlockSize();
			if(input[i].length < blockSize) {
				byte[] newBlock = new byte[blockSize];
				System.arraycopy(input[i], 0, newBlock, 0, input[i].length);
				Arrays.fill(newBlock, input[i].length, blockSize - 1, this.pad);
				input[i] = newBlock;
			}
			Element elem = this.gt.newElement();
			elem.setFromBytes(input[i]);
			output.add(elem);
		}
		
		return output.toArray(new Element[input.length]);
		
	}
	
	public byte[] decode(Element[] input) {
		byte[][] outBlocks = new byte[input.length][];
		int length = 0;
		for(int i = 0; i < input.length; i++) {
			outBlocks[i] = input[i].toBytes();
			length += outBlocks[i].length;
		}
		
		byte[] output = new byte[length];
		int copiedSoFar = 0;
		for(int i = 0; i < outBlocks.length; i++) {
			System.arraycopy(outBlocks[i], 0, output, copiedSoFar, outBlocks[i].length);
			copiedSoFar += outBlocks[i].length;
		}
		
		return output;
	}

}
