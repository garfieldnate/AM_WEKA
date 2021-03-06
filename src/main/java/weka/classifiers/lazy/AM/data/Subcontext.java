/*
 * **************************************************************************
 * Copyright 2021 Nathan Glenn                                              *
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

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a subcontext, containing a list of {@link Instance Instances}
 * which belong to it, along with their shared {@link Label} and common outcome.
 * If the contained instances do not have the same outcome, then the outcome is
 * set to {@link AMUtils#NONDETERMINISTIC}.
 *
 * @author Nathan Glenn
 */
public class Subcontext {
    private final Label label;
    private final String displayLabel;
    private final Set<Instance> data = new HashSet<>();
    private double outcome;

    /**
     * Initializes the subcontext by creating a list to hold the data
     *
	 * @param label Binary label of the subcontext
	 * @param displayLabel user-friendly label string {@link Labeler#getContextString(Label)}
	 */
    public Subcontext(Label label, String displayLabel) {
        this.label = label;
		this.displayLabel = displayLabel;
    }

    /**
     * Adds an exemplar {@code e} to the subcontext and sets the outcome accordingly. If
     * different outcomes are present in the contained exemplars, the outcome is
     * {@link AMUtils#NONDETERMINISTIC}
     *
     */
    public void add(Instance e) {
        if (data.size() != 0) {
            if (e.classValue() != data.iterator().next().classValue()) outcome = AMUtils.NONDETERMINISTIC;
        } else {
            outcome = e.classValue();
        }
        data.add(e);
    }

    public double getOutcome() {
        return outcome;
    }

    /**
     * @return Binary label of of this subcontext
     */
    public Label getLabel() {
        return label;
    }

	/**
	 * @see Labeler#getContextString(Label)
	 * @return User-friendly label string
	 */
	public String getDisplayLabel() {
		return displayLabel;
	}

    /**
     * @return list of Exemplars contained in this subcontext
     */
    public Set<Instance> getExemplars() {
        return data;
    }

    /**
     * Two Subcontexts are considered equal if they have the same label and
     * contain the same instances.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof Subcontext)) return false;
        Subcontext otherSub = (Subcontext) other;
        if (!label.equals(otherSub.label)) return false;
        return data.equals(otherSub.data);
    }

    private final static int SEED = 37;
    private int hash = -1;

    @Override
    public int hashCode() {
        if (hash != -1) return hash;
        hash = SEED * label.hashCode() + data.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');

        sb.append(label);
        sb.append('|');

        // we know all of the exemplars must have the same outcome;
        // otherwise the outcome is nondeterministic
        if (outcome == AMUtils.NONDETERMINISTIC) {
        	sb.append(AMUtils.NONDETERMINISTIC_STRING);
		} else {
        	if (!data.isEmpty()) {
				sb.append(data.iterator().next().stringValue(data.iterator().next().classAttribute()));
			}
		}
        sb.append('|');

        for (Instance instance : data) {
            sb.append(instance);
            // Instance.toString() separates attributes with commas, so we can't
            // use a comma here or it will be difficult to read
            sb.append('/');
        }
        // remove last slash
        sb.deleteCharAt(sb.length() - 1);

        sb.append(')');

        return sb.toString();
    }

    public boolean isNondeterministic() {
        return outcome == AMUtils.NONDETERMINISTIC;
    }
}
