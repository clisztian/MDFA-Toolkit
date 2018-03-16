package ch.imetrica.mdfa.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.imetrica.mdfa.matrix.MdfaMatrix;

public class TestMdfaMatrix {

	
	@Test
	public void testMatrixSolve() {
		
		int nRows = 4;
		int nColumns = 5;

		MdfaMatrix test = new MdfaMatrix(nRows, nColumns);
		
		test.mdfaMatrixSet(0, 0, .3);
		test.mdfaMatrixSet(0, 1, .1);
		test.mdfaMatrixSet(0, 2, 1.0);
		test.mdfaMatrixSet(0, 3, .3);
		test.mdfaMatrixSet(0, 4, 0);
		
		test.mdfaMatrixSet(1, 0, .1);
		test.mdfaMatrixSet(1, 1, .3);
		test.mdfaMatrixSet(1, 2, 2.0);
		test.mdfaMatrixSet(1, 3, .2);
		test.mdfaMatrixSet(1, 4, 1.0);
		
		test.mdfaMatrixSet(2, 0, .1);
		test.mdfaMatrixSet(2, 1, .2);
		test.mdfaMatrixSet(2, 2, 1.0);
		test.mdfaMatrixSet(2, 3, .9);
		test.mdfaMatrixSet(2, 4, 2.0);
		
		test.mdfaMatrixSet(3, 0, .5);
		test.mdfaMatrixSet(3, 1, .1);
		test.mdfaMatrixSet(3, 2, 1.4);
		test.mdfaMatrixSet(3, 3, .1);
		test.mdfaMatrixSet(3, 4, .5);
				
		test = test.mdfaMatrixMultTransB(test);
		
		MdfaMatrix eye = new MdfaMatrix(nRows, nRows);
		for(int i = 0; i < nRows; i++) {eye.mdfaMatrixSet(i, i, 1.0);}
			
		test.mdfaMatrixScale(2.0);		
		test.mdfaSolve(eye);		
		MdfaMatrix inverse = eye.mdfaMatrixMultTransB(test);

		assertEquals(4.0, inverse.meanDiag(false), .0000001);
		
	}
	
}
