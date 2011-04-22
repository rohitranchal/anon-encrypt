package edu.purdue.cs626.anonencrypt;

import java.util.HashMap;
import java.util.Iterator;

import it.unisa.dia.gas.jpbc.Element;

/**
 * To re key, create an instance of this with the current params. Call
 * {@link #update()} and then obtain the new parameters using
 * {@link #getParams()}.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ReKey {

	/**
	 * Public parameters. This will be updated after calling {@link #update()}.
	 */
	private AEParameters params;

	/**
	 * The new g1 value as an {@link Element}.
	 */
	private Element newG1;

	/**
	 * The new master key as an {@link Element}.
	 */
	private Element mk;

	/**
	 * Instantiate with the current public parameters.
	 * 
	 * @param params
	 *            Current public parameters as an {@link AEParameters} instance.
	 */
	public ReKey(AEParameters params) {
		this.params = params;
	}

	/**
	 * This will update the parameters and return new master key.
	 * 
	 * @return The new master key as an {@link Element}.
	 */
	public Element update() {

		Element newAlpha = this.params.getPairing().getZr().newRandomElement()
				.getImmutable();

		Element g = this.params.getG();

		this.newG1 = g.powZn(newAlpha).getImmutable();
		this.params.setG1(this.newG1);

		this.mk = this.params.getG2().powZn(newAlpha).getImmutable();

		return this.mk;
	}

	public AEParameters getParams() {
		return params;
	}

	public Element getMk() {
		return mk;
	}

	/**
	 * Generate the public information of re-keying.
	 * 
	 * @param idRndMap
	 *            The map of contact current contact ids and their random values
	 *            as {@link Element}s.
	 * @return A {@link ReKeyInformation} object that can be serialized to
	 *         obtain the information to publish.
	 */
	public ReKeyInformation getPublicInfo(HashMap<Element, Element> idRndMap) {

		HashMap<Element, Element> newC1Map = new HashMap<Element, Element>();

		Element rnd = this.params.getPairing().getZr().newRandomElement()
				.getImmutable();

		Iterator<Element> ids = idRndMap.keySet().iterator();
		while (ids.hasNext()) {
			Element id = (Element) ids.next();

			Element newC1 = this.mk.mul(
					this.params.getH1().powZn(id).mul(this.params.getG3()))
					.getImmutable();
			Element blindId = id.powZn(rnd);

			newC1Map.put(blindId, newC1);

		}

		return new ReKeyInformation(this.newG1, rnd, newC1Map);
	}

}
