package ch.imetrica.mdfa.matrix;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

public class MdfaMatrixGPU {

	int nRows;
	int nColumns;
	INDArray anymatrix;
	
	public MdfaMatrixGPU(int nRows, int nColumns) {
		
		this.nRows = nRows;
		this.nColumns = nColumns;
		
		anymatrix = Nd4j.zeros(nRows, nColumns);
	}

	
	public MdfaMatrixGPU(INDArray nd) {
				
		anymatrix = nd;
		this.nColumns = anymatrix.columns();
		this.nRows = anymatrix.rows();
	}
	
	public void mdfaMatrixSet(int i, int j, double d) {
		anymatrix.putScalar(i, j, d);
	}

	public void mdfaVectorSet(int i, double d) {
		anymatrix.putScalar(i, d);		
	}
	

	public double mdfaVectorGet(int i) {
		return anymatrix.getDouble(i);			
	}
	
	
	public double mdfaMatrixGet(int i, int k) {
		return anymatrix.getDouble(i,k);			
	}

	public void mdfaMatrixScale(double d) {		
		anymatrix = anymatrix.mul(d);
	}

	public void mdfaMatrixAdd(MdfaMatrixGPU q_decay) {		  
		anymatrix = anymatrix.addi(q_decay.getMatrix());
	}

	private INDArray getMatrix() {
		
		return anymatrix;
	}

	public MdfaMatrixGPU mdfaMatrixMult(MdfaMatrixGPU des_mat) {
		
		return new MdfaMatrixGPU(anymatrix.mmul(des_mat.getMatrix()));
			
	}
	
	public MdfaMatrixGPU mdfaMatrixMultTrans(MdfaMatrixGPU des_mat) {
		
		return new MdfaMatrixGPU(anymatrix.mmul(des_mat.getMatrix().transpose()));
			
	}
	
	public static MdfaMatrixGPU mdfaSolve(MdfaMatrixGPU A, MdfaMatrixGPU b) {
	

		DenseMatrix Aclone = new DenseMatrix(A.nRows, A.nColumns, A.getMatrix().data().asDouble(), true);
		DenseMatrix bclone = new DenseMatrix(b.nRows, b.nColumns, b.getMatrix().data().asDouble(), true);
		
		DenseLU.factorize(Aclone).solve(bclone);
	
		return new MdfaMatrixGPU(Nd4j.create(bclone.getData(), new int[]{bclone.numRows(), bclone.numColumns()}));
	}

	
	
	
	public static void main(String[] args) {
		
		int nRows = 4;
		int nColumns = 5;
		
		
		MdfaMatrixGPU test = new MdfaMatrixGPU(Nd4j.rand(nRows, nColumns));
		test = test.mdfaMatrixMultTrans(test);
		
		MdfaMatrixGPU eye = new MdfaMatrixGPU(Nd4j.eye(nRows));
		
		System.out.println("Before inverse");
		System.out.println(test.getMatrix().toString());
//		System.out.println(eye.getMatrix().toString());
//		
		MdfaMatrixGPU invtest = MdfaMatrixGPU.mdfaSolve(test, eye);
//		
		System.out.println("After inverse");
		System.out.println(invtest.getMatrix().toString());
		
		MdfaMatrixGPU inverse = invtest.mdfaMatrixMultTrans(test);
		System.out.println(inverse.getMatrix().toString());
		
	}

}
