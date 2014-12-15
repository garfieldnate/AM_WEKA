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

package weka.classifiers.lazy.AM.lattice;

import java.util.HashSet;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Analogical Modeling uses labels composed of boolean vectors in order to group
 * instances into subcontexts and subcontexts in supracontexts. Training set
 * instances are assigned labels by comparing them with the instance to be
 * classified and encoding matched attributes and mismatched attributes in a
 * boolean vector.
 * 
 * For example, if we were classifying an instance <a, b, c>, and we had three
 * training instances <x, y, c>, <w, m, c> and <a, b, z>, and used 'n' to
 * represent mismatches and 'y' for matches, the labels would be <n, n, y>, <n,
 * n, y>, and <y, y, n>.
 * 
 * The current implementation takes advantage of binary arithmetic by
 * representing mismatches as a 1 bit and matches as a 0 bit.
 * 
 * @author Nathan Glenn
 */
public class IntLabeler {

	private final MissingDataCompare mdc;
	private final Instance testItem;
	private final Set<Integer> ignoreSet;
	private final boolean ignoreUnknowns;
	private int classIndex;

	/**
	 * 
	 * @param mdc
	 *            Specifies how to compare missing attributes
	 * @param instance
	 *            Instance being classified
	 * @param ignroeUnknowns
	 *            true if attributes with undefined values in the test item
	 *            should be ignored; false if not.
	 */
	public IntLabeler(MissingDataCompare mdc, Instance instance,
			boolean ignoreUnknowns) {
		this.mdc = mdc;
		this.testItem = instance;
		this.ignoreUnknowns = ignoreUnknowns;
		ignoreSet = new HashSet<>();
		classIndex = instance.classIndex();
		if (ignoreUnknowns) {
			int length = testItem.numAttributes() - 1;
			for (int i = 0; i < length; i++) {
				if (testItem.isMissing(i))
					ignoreSet.add(i);
			}
		}
	}

	/**
	 * 
	 * @return The cardinality of the label, or how many instance attributes are
	 *         considered during labeling.
	 */
	public int getCardinality() {
		return testItem.numAttributes() - ignoreSet.size() - 1;
	}

	public boolean getIgnoreUnknowns() {
		return ignoreUnknowns;
	}

	/**
	 * @param data
	 *            Instance to be labeled
	 * @return binary label of length n, where n is the length of the feature
	 *         vectors. If the features of the test exemplar and the data
	 *         exemplar are the same at index i, then the i'th bit will be 1;
	 *         otherwise it will be 0.
	 */
	public IntLabel getContextLabel(Instance data) {
		int label = 0;
		int length = getCardinality();
		Attribute att;
		int index = 0;
		for (int i = 0; i < testItem.numAttributes(); i++) {
			// skip ignored attributes and the class attribute
			if (ignoreSet.contains(i))
				continue;
			if (i == classIndex)
				continue;
			att = testItem.attribute(i);
			// use mdc if were are comparing a missing attribute
			if (testItem.isMissing(i) || data.isMissing(i)) {
				if (!mdc.matches(testItem, data, att))
					// use length-1-index instead of index so that in binary the
					// labels show left to right, first to last feature.
					label |= (1 << (length - 1 - index));
			} else if (testItem.value(att) != data.value(att)) {
				// same as above
				label |= (1 << (length - 1 - index));
			}
			index++;
		}
		return new IntLabel(label, getCardinality());
	}
}