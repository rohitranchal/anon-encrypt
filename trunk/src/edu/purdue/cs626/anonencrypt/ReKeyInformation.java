package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;

import java.util.HashMap;

import org.apache.axiom.om.OMElement;

/**
 * Wrapper for re-key information. Serialization of this generates the public
 * information contacts can use to update their configurations.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ReKeyInformation {

	/**
	 * New g1 value.
	 */
	private Element g1;

	/**
	 * This is used to blind all contact Id values by raising them to rnd.
	 */
	private Element rnd;

	private HashMap<Element, Element> newC1map = new HashMap<Element, Element>();

	public ReKeyInformation(OMElement elem) {
		// TODO
	}

	/**
	 * Create new instance with the given values. Called by the re-keying
	 * entity.
	 * 
	 * @param g1
	 *            New g1 as an {@link Element}
	 * @param rnd
	 *            Random used to blind identities as an {@link Element}
	 * @param newC1Map
	 *            The map of new first components of the private keys indexed by
	 *            the blinded identity.
	 */
	public ReKeyInformation(Element g1, Element rnd,
			HashMap<Element, Element> newC1Map) {
		this.g1 = g1;
		this.rnd = rnd;
		this.newC1map = newC1Map;
	}

	public String serialize() {
		String output = "<ReKeyInformation>\n";

		output += "</ReKeyInformation>";
		return output;
	}
}
