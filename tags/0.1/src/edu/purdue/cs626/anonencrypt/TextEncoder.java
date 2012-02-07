package edu.purdue.cs626.anonencrypt;

import java.util.ArrayList;
import java.util.Arrays;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * Encodes and decode strings into Elements of GT.
 * 
 * @author Ruchith Fernando
 *
 */
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
	
	/**
	 * Returns the block size in bytes.
	 * @return the block size in bytes.
	 */
	public int getBlockSize() {
		return this.gt.getLengthInBytes();
	}
	
	/**
	 * Encode a string into an array of {@link Element}s
	 * @param input {@link String} value to be encoded.
	 * @return An array of {@link Element}s 
	 */
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
	
	/**
	 * Encode a given array of byte arrays into an array of {@link Element}s
	 * This takes care of padding the final array and expects all other blocks
	 * to have data of block size.
	 * This is intended to be used by Element[] encode(String input) So use 
	 * with care.
	 * @param input The array of byte arrays
	 * @return  An array of {@link Element}s
	 */
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
	
	/**
	 * Decodes a given array of encoded elements back to a byte array.
	 * @param input An array of {@link Element}s
	 * @return Single byte array with the decoded data.
	 */
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
			System.arraycopy(outBlocks[i], 0, output, copiedSoFar, 
					outBlocks[i].length);
			copiedSoFar += outBlocks[i].length;
		}
		
		return output;
	}

}
