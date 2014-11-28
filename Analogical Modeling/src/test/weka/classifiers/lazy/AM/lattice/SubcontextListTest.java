package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instance;
import weka.core.Instances;

public class SubcontextListTest {

	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.chapter3Train();
		Instance test = TestUtils.chapter3Test();

		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				test, false);

		SubcontextList subs = new SubcontextList(labeler, train);
		assertEquals(subs.getCardinality(), 3);

		assertEquals(
				"(001|"
						+ AMUtils.NONDETERMINISTIC_STRING
						+ "|3,1,0,e/3,1,1,r),(100|r|2,1,2,r),(101|r|2,1,0,r),(110|r|0,3,2,r)",
				subs.toString());
		
		List<Subcontext> subList = getSubList(subs);
		assertEquals(subList.size(), 4);

		Subcontext expected = new Subcontext(new Label(0b001, 3));
		expected.add(train.get(0));// 310e
		expected.add(train.get(4));// 311r
		assertTrue(subList.contains(expected));

		expected = new Subcontext(new Label(0b100, 3));
		expected.add(train.get(3));// 212r
		assertTrue(subList.contains(expected));

		expected = new Subcontext(new Label(0b101, 3));
		expected.add(train.get(1));// 210r
		assertTrue(subList.contains(expected));

		expected = new Subcontext(new Label(0b110, 3));
		expected.add(train.get(2));// 032r
		assertTrue(subList.contains(expected));
	}

	private List<Subcontext> getSubList(final SubcontextList subcontextList) {
		@SuppressWarnings("serial")
		List<Subcontext> subs = new ArrayList<Subcontext>(){{
			for(Subcontext s : subcontextList)
				add(s);
		}};
		return subs;
	}

}
