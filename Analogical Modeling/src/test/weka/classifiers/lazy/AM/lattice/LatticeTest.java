package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import weka.classifiers.lazy.AM.AMconstants;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class LatticeTest {

	@Test
	public void testChapter3Data() throws Exception {
		DataSource source = new DataSource("data/ch3example.arff");
		Instances train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);

		source = new DataSource("data/ch3exampleTest.arff");
		Instances test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				test.firstInstance(), false);

		SubcontextList subList = new SubcontextList(labeler, train);

		Lattice lattice = new Lattice(test.get(0).numAttributes() - 1, subList);
		
		List<Supracontext> supras = lattice.getSupracontextList();
		assertEquals(3, supras.size());
		
		Supracontext expected = new Supracontext();
		Subcontext sub1 = new Subcontext(new Label(0b100, 3));
		sub1.add(train.get(3)); //212r
		expected.setData(new int[]{sub1.getIndex()});
		expected.setCount(1);
		expected.setOutcome(0);//r
		assertTrue(supras.contains(expected));
		
		expected = new Supracontext();
		sub1 = new Subcontext(new Label(0b100, 3));
		sub1.add(train.get(3));//212r
		Subcontext sub2 = new Subcontext(new Label(0b110, 3));
		sub2.add(train.get(2));//032r
		expected.setData(new int[]{sub1.getIndex(), sub2.getIndex()});
		expected.setCount(1);
		expected.setOutcome(0);//r
		assertTrue(supras.contains(expected));
		
		expected = new Supracontext();
		sub1 = new Subcontext(new Label(0b001, 3));
		sub1.add(train.get(0));//310e
		sub1.add(train.get(4));//311r
		expected.setData(new int[]{sub1.getIndex()});
		expected.setCount(2);
		expected.setOutcome(AMconstants.NONDETERMINISTIC);
		assertTrue(supras.contains(expected));
	}

}
