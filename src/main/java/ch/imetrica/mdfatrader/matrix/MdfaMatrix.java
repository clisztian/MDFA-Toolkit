package ch.imetrica.mdfatrader.matrix;

import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;


/**
 * A complete but simple Matrix (vector) wrapper for the 
 * Dense matrix Matrix-Toolkits-Java package that utilizes
 * Blas/Lapack on a CPU. For GPU matrix computations (for very large
 * systems), ideally the MdfaMatrixGPU wrapper will be used that
 * uses INDArray and JCudaBLAS as the computation backend
 * 
 * All systems are symmetric in MDFA computations, so LU decomposition/solver
 * from Dense matrix will be used. 
 * 
 */

public class MdfaMatrix {

	int nRows;
	int nColumns;
	DenseMatrix anymatrix;
	
	/**
     * TargetSeries is a type of time series which has two components:
     * The original time series, and the transformed time series.
     * TimeSeries data is raw series, with no transformations
     * applied. After transformation, the raw series is kept as the 
     * second index of the double[2] value. The first (zeroth) index
     * contains the transformed data
     * 
     * @param nRows
     *     Number of rows in matrix 
     * 
     * @param nColumns
     *     Number of columns
     */
	
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

	public DenseMatrix getMatrix() {
		
		return anymatrix;
	}
	
	public void transpose(MdfaMatrix B) {
		
		this.anymatrix.transpose(B.getMatrix());
		B.nColumns = B.getMatrix().numColumns();
		B.nRows = B.getMatrix().numRows();
	}

	
	public double meanDiag(boolean normalize) {
		
		double sum = 0; 
		int nCols = this.anymatrix.numColumns();

		for(int i = 0; i < nCols; i++) {
			sum += this.anymatrix.get(i, i);
		}
		
		if(!normalize) {
			nCols = 1;
	    }
		
		return sum/(double)nCols;
	}
	
	/**
     * Transposes this matrix and computes
     *   <code>A<sup>T</sup>*A</code>
     * 
     * @param des_mat
     *     Right side matrix not transposed
     * 
     * @return MdaMatrix 
     *     With size this.numColumns and des_mat.nColumns
     */
	
	public MdfaMatrix mdfaMatrixMultTransA(MdfaMatrix des_mat) {
		
	
		DenseMatrix C = new DenseMatrix(anymatrix.numColumns(), des_mat.nColumns);
		anymatrix.transAmult(des_mat.getMatrix(), C);
		
		return new MdfaMatrix(C);
	}
	
	/**
     * this matrix and computes
     *   <code>A*A<sup>T</sup></code>
     * 
     * @param des_mat
     *     Right side matrix transposed
     * 
     * @return MdaMatrix 
     *     With size this.numRows and des_mat.nRows
     */
	
	public MdfaMatrix mdfaMatrixMultTransB(MdfaMatrix des_mat) {
		
		
		DenseMatrix C = new DenseMatrix(anymatrix.numRows(), des_mat.nRows);
		anymatrix.transBmult(des_mat.getMatrix(), C);
		
		return new MdfaMatrix(C);
	}
	
	/**
     * this matrix and computes
     *   <code>A*A</code>
     * 
     * @param des_mat
     *     Right side matrix
     * 
     * @return MdaMatrix 
     *     With size this.numRows and des_mat.nColumns
     */	
	public MdfaMatrix mdfaMatrixMult(MdfaMatrix des_mat) {
		
		DenseMatrix C = new DenseMatrix(anymatrix.numRows(), des_mat.nColumns);
		anymatrix.mult(des_mat.getMatrix(), C);
		
		return new MdfaMatrix(C);
	}
	
	
	/**
     * Solves Ax = b where A is decomposed in LU form
     *   <code>A*A<sup>T</sup></code>
     * 
     * @param b
     *     Right side or equation. b is overwritten with the solution
     * 
     */
	public void mdfaSolve(MdfaMatrix b) {
			
		DenseLU.factorize(anymatrix).solve(b.getMatrix());	
	}

	public double sum() {
	  
		double sum = 0; 
		for(int i = 0; i < this.nRows; i++) {
			for(int j = 0; j < this.nColumns; j++) {
				sum += this.getMatrix().get(i, j);
			}
		}
		return sum;
	}

	public double expectation() {
		  
		double sum = 0; 
		for(int i = 0; i < this.nRows; i++) {
			for(int j = 0; j < this.nColumns; j++) {
				sum += this.getMatrix().get(i, j)*i;
			}
		}
		return sum;
	}
	
	/**
	 * 
	 * Gets a subarray from a given column of the matrix.
	 * 
	 * @param from
	 *    inclusive index
	 * @param to
	 *    exclusive index
	 * @return
	 *   double[] array of subset
	 * @throws Exception 
	 */
	public double[] getSubsetColumn(int column, int from, int to) throws Exception {
		
		if(from > to || to > this.nRows) {
			throw new Exception("Dimensions are incorrect: to must not be larger "
					+ " than number of rows");
		}
		
		int size = to - from;
		double[] subset = new double[size];
		
		for(int i = from; i < to; i++) {
			subset[i-from] = this.mdfaMatrixGet(i, column);
		}
		return subset;		
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
	
		test.mdfaMatrixScale(2.0);
		System.out.println(test.getMatrix().toString());
		
		test.mdfaSolve(eye);
	
		System.out.println("After inverse");
		System.out.println(eye.getMatrix().toString());
		
		MdfaMatrix inverse = eye.mdfaMatrixMultTransB(test);
		System.out.println(inverse.getMatrix().toString());
		
	}


	

}
