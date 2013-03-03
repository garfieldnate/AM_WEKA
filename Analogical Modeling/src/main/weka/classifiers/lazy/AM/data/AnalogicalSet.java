/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package weka.classifiers.lazy.AM.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.Supracontext;

/**
 * This class holds a list of the exemplars that influenced the predicted
 * outcome of a certain test item, along with the analogical effect of each.
 * 
 * @author Nate Glenn
 * 
 */
public class AnalogicalSet {

	/**
	 * Mapping of an exemplar to its analogical effect
	 */
	private Map<Exemplar, Double> exEffectMap = new HashMap<Exemplar, Double>();

	/**
	 * Mapping of exemplar to the number of pointers to it
	 */
	private Map<Exemplar, Integer> exPointerMap;

	private Map<Integer, Integer> classPointerMap = new HashMap<Integer, Integer>();

	private Map<Integer, Double> classLikelihoodMap = new HashMap<Integer, Double>();

	private int totalPointers = 0;

	private int classIndex = -1;
	private double classProbability = Double.NEGATIVE_INFINITY;

	/**
	 * The exemplar whose class is being predicted by this set
	 */
	private Exemplar classifiedExemplar;

	private static String newline = System.getProperty("line.separator");

	/**
	 * 
	 * @param supraList
	 *            Supracontext list generated by a Lattice
	 * @param testItem
	 *            Exemplar being classified
	 * @param linear
	 *            True if counting of pointers should be done linearly; false if
	 *            quadratically.
	 */
	public AnalogicalSet(List<Supracontext> supraList, Exemplar testItem,
			boolean linear) {

		this.classifiedExemplar = testItem;

		// find numbers of pointers to individual exemplars
		exPointerMap = getPointers(supraList, linear);

		// find the total number of pointers
		for (Exemplar e : exPointerMap.keySet())
			totalPointers += exPointerMap.get(e);

		// find the analogical effect of an exemplar by dividing its pointer
		// count by the total pointer count
		for (Exemplar e : exPointerMap.keySet())
			exEffectMap.put(e, exPointerMap.get(e)
					/ ((double) getTotalPointers()));

		// find the likelihood for a given outcome based on the pointers
		for (Exemplar e : exPointerMap.keySet()) {
			if (classPointerMap.containsKey(e.getOutcome()))
				classPointerMap.put(
						e.getOutcome(),
						classPointerMap.get(e.getOutcome())
								+ exPointerMap.get(e));
			else
				classPointerMap.put(e.getOutcome(), exPointerMap.get(e));
		}

		// set the likelihood of each possible class index to be its share of
		// the total pointers
		for (Integer i : classPointerMap.keySet())
			classLikelihoodMap.put(i, classPointerMap.get(i)
					/ (double) totalPointers);
		// Set the class index to that with the highest likelihood
		Double temp;
		for (Integer i : classLikelihoodMap.keySet()) {
			temp = classLikelihoodMap.get(i);
			if (temp > getClassProbability()) {
				classProbability = temp;
				classIndex = i;
			}
		}
	}

	/**
	 * See page 392 of the red book.
	 * 
	 * @param supraList
	 *            List of Supracontexts created by filling the supracontextual
	 *            lattice.
	 * @param linear
	 *            True if pointer counting should be done linearly; false if it
	 *            should be done quadratically
	 * @return A mapping of each exemplar to the number of pointers pointing to
	 *         it.
	 */
	private Map<Exemplar, Integer> getPointers(List<Supracontext> supraList,
			boolean linear) {
		Map<Exemplar, Integer> pointers = new HashMap<Exemplar, Integer>();
		
		//number of pointers in a supracontext,
		//that is the number of exemplars in the whole thing
		int pointersInList = 0;
		int pointersToSupra = 0;
		//iterate all supracontext
		for (Supracontext supra : supraList) {
			if (!linear) {
				pointersInList = 0;
				//sum number of exemplars for each subcontext
				for (int index : supra.getData())
					pointersInList += Subcontext.getSubcontext(index).getData().size();
			}
			//iterate subcontexts in supracontext
			for (int index : supra.getData()) {
				//number of supras containing this subcontext
				pointersToSupra = supra.getCount();
				//iterate exemplars in subcontext
				for (Exemplar e : Subcontext.getSubcontext(index).getData()) {
					//pointers to exemplar = pointersToSupra * pointers in list
					// add together if already in the map
					if (pointers.get(e) != null)
						pointers.put(e, pointers.get(e)
								+ (linear ? 1 : pointersInList)
								* pointersToSupra);
					else
						pointers.put(e, (linear ? 1 : pointersInList)
								* pointersToSupra);
				}
			}
		}
		return pointers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("predicting:");
		sb.append(getClassifiedEx());
		sb.append(newline);

		sb.append("outcome: ");
		sb.append(classifiedExemplar.getStringOutcome());
		sb.append(" (");
		sb.append(classProbability);
		sb.append(")");
		sb.append(newline);

		for (Entry<Exemplar, Double> e : exEffectMap.entrySet()) {
			sb.append(e.getKey());
			sb.append(": ");
			sb.append(e.getValue());
			sb.append(newline);
		}
		sb.append("Outcome likelihoods:" + newline);
		for (Integer i : classLikelihoodMap.keySet()) {
			sb.append(classifiedExemplar.classString(i) + ": ");
			sb.append(classLikelihoodMap.get(i) + newline);
		}

		sb.append("Exemplar pointers:" + newline);
		for (Integer i : classPointerMap.keySet()) {
			sb.append(classifiedExemplar.classString(i) + ": ");
			sb.append(classPointerMap.get(i) + newline);
		}

		return sb.toString();
	}

	/**
	 * 
	 * @return A mapping between exemplars and their analogical effect (decimal
	 *         percentage)
	 */
	public Map<Exemplar, Double> getExemplarEffectMap() {
		return exEffectMap;
	}

	/**
	 * 
	 * @return Mapping of exemplars in the analogical set to the number of
	 *         pointers to it
	 */
	public Map<Exemplar, Integer> getExemplarPointers() {
		return exPointerMap;
	}

	/**
	 * 
	 * @return A mapping between a possible class index and its likelihood
	 *         (decimal probability)
	 */
	public Map<Integer, Double> getClassLikelihoodMap() {
		return classLikelihoodMap;
	}

	/**
	 * 
	 * @return The total number of pointers in this analogical set
	 */
	public int getTotalPointers() {
		return totalPointers;
	}

	/**
	 * 
	 * @return A mapping between a class value index the number of pointers
	 *         pointing to it
	 */
	public Map<Integer, Integer> getClassPointers() {
		return classPointerMap;
	}

	/**
	 * 
	 * @return A mapping between the class value index and its selection
	 *         probability
	 */
	public Map<Integer, Double> getClassLikelihood() {
		return classLikelihoodMap;
	}

	/**
	 * 
	 * @return The exemplar which was classified
	 */
	public Exemplar getClassifiedEx() {
		return classifiedExemplar;
	}

	/**
	 * 
	 * @return Probability of the predicted class
	 */
	public double getClassProbability() {
		return classProbability;
	}

	/**
	 * 
	 * @return Index of the predicted class attribute value
	 */
	public int getIndex() {
		return classIndex;
	}
}
