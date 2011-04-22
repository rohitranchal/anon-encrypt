package edu.purdue.cs626.anonencrypt;

import it.unisa.dia.gas.jpbc.Element;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;

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

	/**
	 * Output the XML data to publish as a {@link String}.
	 * 
	 * @return {@link String} of XML data.
	 */
	public String serialize() {
		String output = "<ReKeyInformation>\n";
		output += "<G1>" + Base64.encode(this.g1.toBytes()) + "</G1>";
		output += "<Random>" + Base64.encode(this.rnd.toBytes()) + "</Random>";
		output += "<Contacts>";

		Iterator<Element> ids = this.newC1map.keySet().iterator();
		while (ids.hasNext()) {
			output += "<Contact>";
			Element id = (Element) ids.next();
			output += "<Id>" + Base64.encode(id.toBytes()) + "</Id>";

			Element c1 = this.newC1map.get(id);
			output += "<C1>" + Base64.encode(c1.toBytes()) + "</C1>";

			output += "</Contact>";

		}
		output += "</Contacts>";

		output += "</ReKeyInformation>";
		return output;
	}
}
