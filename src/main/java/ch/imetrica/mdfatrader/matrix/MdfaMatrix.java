package ch.imetrica.mdfatrader.matrix;

import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;

public class MdfaMatrix {

	int nRows;
	int nColumns;
	DenseMatrix anymatrix;
	
	public MdfaMatrix(int nRows, int nColumns) {
		
		this.nRows = nRows;
		this.nColumns = nColumns;
		
		anymatrix = new DenseMatrix(nRows, nColumns);
		anymatrix.zero();
	}

	
	public MdfaMatrix(DenseMatrix nd) {
				
		anymatrix = nd;
		this.nColumns = anymatrix.numColumns();
		this.nRows = anymatrix.numRows();
	}
	
	public void mdfaMatrixSet(int i, int j, double d) {
		anymatrix.set(i, j, d);
	}

	public void mdfaVectorSet(int i, double d) {
		anymatrix.set(i, 0, d);		
	}
	

	public double mdfaVectorGet(int i) {
		return anymatrix.get(i,0);			
	}
	
	
	public double mdfaMatrixGet(int i, int k) {
		return anymatrix.get(i,k);			
	}

	public void mdfaMatrixScale(double d) {		
		anymatrix.scale(d);
	}

	public void mdfaMatrixAdd(MdfaMatrix q_decay) {		  
		anymatrix.add(q_decay.getMatrix());
	}

	private DenseMatrix getMatrix() {
		
		return anymatrix;
	}

	public MdfaMatrix mdfaMatrixMultTransA(MdfaMatrix des_mat) {
		
	
		DenseMatrix C = new DenseMatrix(anymatrix.numColumns(), des_mat.nColumns);
		anymatrix.transAmult(des_mat.getMatrix(), C);
		
		return new MdfaMatrix(C);
	}
	
	public MdfaMatrix mdfaMatrixMultTransB(MdfaMatrix des_mat) {
		
		
		DenseMatrix C = new DenseMatrix(anymatrix.numRows(), des_mat.nRows);
		anymatrix.transBmult(des_mat.getMatrix(), C);
		
		return new MdfaMatrix(C);
	}
	
	
	public MdfaMatrix mdfaSolve(MdfaMatrix b) {
			
		DenseLU.factorize(anymatrix).solve(b.getMatrix());	
		return b;
	}

	

	
	public static void main(String[] args) {
		
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
		
		System.out.println("Before inverse");
		System.out.println(test.getMatrix().toString());
//		System.out.println(eye.getMatrix().toString());
//		
		test.mdfaMatrixScale(2.0);
		System.out.println(test.getMatrix().toString());
		
		MdfaMatrix invtest = test.mdfaSolve(eye);
//		
		System.out.println("After inverse");
		System.out.println(invtest.getMatrix().toString());
		
		MdfaMatrix inverse = invtest.mdfaMatrixMultTransB(test);
		System.out.println(inverse.getMatrix().toString());
		
	}

}
