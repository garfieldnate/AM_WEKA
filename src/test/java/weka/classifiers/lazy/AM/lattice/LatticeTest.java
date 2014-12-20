package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Test the lattices that can be used for item classification. These are
 * implementations of the {@link Lattice} interface.
 * 
 * @author Nathan Glenn
 */
@RunWith(Parameterized.class)
public class LatticeTest {
	@Parameter(0)
	public String testName;
	@Parameter(1)
	public Constructor<Lattice> latticeConstructor;

	/**
	 * 
	 * @return A collection of argument arrays for running tests. In each array:
	 *         <ol>
	 *         <li>arg[0] is the test name.</li>
	 *         <li>arg[1] is the {@link Constructor} for the {@link Lattice} to
	 *         be tested.</li>
	 *         </ol>
	 * @throws Exception
	 */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		Collection<Object[]> parameters = new ArrayList<>();

		// basic, non-distributed lattice
		parameters.add(new Object[] { BasicLattice.class.getSimpleName(),
				BasicLattice.class.getConstructor(SubcontextList.class) });
		// distributed lattice
		parameters
				.add(new Object[] {
						DistributedLattice.class.getSimpleName(),
						DistributedLattice.class
								.getConstructor(SubcontextList.class) });

		return parameters;
	}

	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		String[] expectedSupras = new String[] {
				"[2x(001|&nondeterministic&|3,1,0,e/3,1,1,r)]",
				"[1x(100|r|2,1,2,r)]", "[1x(100|r|2,1,2,r),(110|r|0,3,2,r)]" };
		testSupras(train, 0, expectedSupras);
	}

	/**
	 * Test that supracontexts are properly marked heterogeneous.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHeterogeneousMarking() throws Exception {
		Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"6-10");
		String[] expectedSupras = new String[] {
				"[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]",
				"[2x(10000|A|K,U,V,U,0,A)]", "[2x(10000|A|K,U,V,U,0,A)]",
				"[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]",
				"[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]" };
		testSupras(train, 0, expectedSupras);

		train = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
		expectedSupras = new String[] {
				"[6x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B)]",
				"[2x(00110|B|A,A,V,U,0,B)]",
				"[2x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B),(00110|B|A,A,V,U,0,B)]",
				"[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]" };
		testSupras(train, 0, expectedSupras);
	}

	/**
	 * Test that {@link BasicLattice#cleanSupra()} is only run after a
	 * subcontext is inserted completely, not after each single insertion
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCleanSupraTiming() throws Exception {
		Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"1,7-10");

		String[] expectedSupras = new String[] { "[6x(00000|A|U,V,U,0,?,A)]",
				"[3x(00000|A|U,V,U,0,?,A),(00100|A|U,V,I,0,?,A)]",
				"[3x(00000|A|U,V,U,0,?,A),(01100|A|U,0,?,0,?,A),(00100|A|U,V,I,0,?,A)]" };
		testSupras(train, 0, expectedSupras);
	}

	/**
	 * This tests a bug where the count was off by 1 in the distributed lattice
	 * implementation due to failing to set the supracontext count the first
	 * time, leaving it at zero.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFinnverb() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.FINNVERB);

		String[] expectedSupras = new String[] {
				"[18x(1000110000|C|K,U,V,U,S,L,0,?,T,A,C)]",
				"[9x(0101000011|A|H,A,V,I,0,?,0,?,S,E,A),(0101000000|A|H,O,V,I,0,?,0,?,T,A,A)]",
				"[9x(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[18x(1000001100|A|M,U,V,U,0,?,O,T,T,A,A)]",
				"[3x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A)]",
				"[54x(0101000000|A|H,O,V,I,0,?,0,?,T,A,A)]",
				"[18x(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[15x(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A)]",
				"[9x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1011000011|A|L,U,0,?,0,?,0,?,K,E,A/S,U,0,?,0,?,0,?,R,E,A/T,U,0,?,0,?,0,?,L,E,A/P,U,0,?,0,?,0,?,K,E,A/T,U,0,?,0,?,0,?,K,E,A/P,U,0,?,0,?,0,?,R,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[9x(0101000011|A|H,A,V,I,0,?,0,?,S,E,A),(0101000000|A|H,O,V,I,0,?,0,?,T,A,A),(0111000011|A|H,A,0,?,0,?,0,?,K,E,A)]",
				"[32x(0001110000|C|H,U,V,O,S,L,0,?,T,A,C)]",
				"[6x(1000001100|A|M,U,V,U,0,?,O,T,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[3x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001001111|A|J,U,V,O,0,?,O,K,S,E,A),(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1011000011|A|L,U,0,?,0,?,0,?,K,E,A/S,U,0,?,0,?,0,?,R,E,A/T,U,0,?,0,?,0,?,L,E,A/P,U,0,?,0,?,0,?,K,E,A/T,U,0,?,0,?,0,?,K,E,A/P,U,0,?,0,?,0,?,R,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A),(1011001111|A|P,U,0,?,0,?,O,S,K,E,A)]",
				"[3x(1001001111|A|J,U,V,O,0,?,O,K,S,E,A),(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[45x(1001000000|A|V,U,V,O,0,?,0,?,T,A,A)]",
				"[9x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A)]",
				"[36x(1100000000|A|N,O,V,U,0,?,0,?,T,A,A/S,O,V,U,0,?,0,?,T,A,A)]", };
		testSupras(train, 15, expectedSupras);
	}

	/**
	 * Test that the given test/train combination yields the given list of
	 * supracontexts.
	 * 
	 * @param train
	 *            Dataset to train with
	 * @param testIndex
	 *            Index of item in dataset to remove and use as a test item
	 * @param expectedSupras
	 *            String representations of the supracontexts that should be
	 *            created from the train/test combo
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void testSupras(Instances train, int testIndex,
			String[] expectedSupras) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		final Instance test = train.get(testIndex);
		train.remove(testIndex);

		// test with the contrived full splitting labeler as well as with a
		// normal one
		Labeler[] labelers = new Labeler[] {
				new IntLabeler(MissingDataCompare.VARIABLE, test, false),
				getFullSplitLabeler(test) };
		for (Labeler labeler : labelers) {
			SubcontextList subList = new SubcontextList(labeler, train);
			Lattice testLattice = latticeConstructor.newInstance(subList);
			List<Supracontext> actualSupras = testLattice.getSupracontextList();

			assertEquals("Supras labeled with "
					+ labeler.getClass().getSimpleName(),
					expectedSupras.length, actualSupras.size());
			for (String expected : expectedSupras) {
				ClassifiedSupra supra = TestUtils.getSupraFromString(expected,
						train);
				TestUtils.assertContainsSupra(actualSupras, supra);
			}

		}

	}

	// create a labeler which splits labels into labels of cardinality 1
	private Labeler getFullSplitLabeler(final Instance test) {
		Labeler labeler = new Labeler(MissingDataCompare.VARIABLE, test, false) {
			Labeler internal = new IntLabeler(MissingDataCompare.VARIABLE,
					test, false);

			@Override
			public Label label(Instance data) {
				return internal.label(data);
			}

			@Override
			public Label partition(Label label, int partitionIndex) {
				int labelBit = label.matches(partitionIndex) ? 0 : 1;
				return new IntLabel(labelBit, 1);
			}

			@Override
			public int numPartitions() {
				return getCardinality();
			}
		};
		return labeler;
	}
}