package ch.imetrica.mdfa.mdfa;

import ch.imetrica.mdfa.customization.Customization;
import ch.imetrica.mdfa.matrix.MdfaMatrix;
import ch.imetrica.mdfa.regularization.Regularization;
import ch.imetrica.mdfa.spectraldensity.SpectralBase;


/**
 * 
 * The main solver for constructing the real-time filter
 * coefficients given customization and regularization
 * rules.
 *
 * The solution for the real-time coefficients is given by a
 * system of equations of the standard form 
 * <code>A*x = b</code> of size L*nseries x L*nseries matrix  
 * and where A is SPD matrix featuring the effects of the regularization
 * and customization. The result x are the coefficients.
 * 
 * @author lisztian
 *
 */

public class MDFASolver {

	
	private Customization anyCustomization;
	private Regularization anyReg;
	
	
	/**
	* Constructs an MDFA solver given any customization and 
	* regularization design matrices.
	* 
	* @param anyCustomization
	*      Is an object of type Customization that
	*      holds the Grammian matrix of the complex exponential 
	*      basis functions. Also holds the right hand side of the 
	*      system for the coefficients
	*      
	* @param anyReg 
	*      Is the regularization object that holds the regularization matrices
	*      for smooth, decay, and cross regularization
	*/
	
	public MDFASolver(Customization anyCustomization, Regularization anyReg) {
		this.anyCustomization = anyCustomization;
		this.anyReg = anyReg;
	}
	
	
	/**
	* Sets a new customization group
	* 
	* @param anyCustomization
	*      Is an object of type Customization that
	*      holds the Grammian matrix of the complex exponential 
	*      basis functions. Also holds the right hand side of the 
	*      system for the coefficients
	*/
	
	public void setAnyCustomization(Customization anyCustomization) {
		this.anyCustomization = anyCustomization;
	}
	
	/**
	* 
	* Sets a new regulatization group
	*  
	* @param anyReg 
	*      Is the regularization object that holds the regularization matrices
	*      for smooth, decay, and cross regulatization
	*/
	public void setAnyRegularization(Regularization anyReg) {
		this.anyReg = anyReg;
	}
	
	/**
	 * Updates the SpectralBase information in the customization 
	 * matrices. The customization matrices are recomputed with the
	 * updated spectral information
	 * 
	 * @param anySpectralBase
	 *      The new updated spectral information from the new time series
	 *      data
	 *        
	 * @throws Exception
	 *     If the number of series in the spectral base does not equal 
	 *     the number of series for the MDFA estimation process an 
	 *     exception is thrown
	 */
	public void updateSpectralBase(SpectralBase anySpectralBase) throws Exception {
		
		anyCustomization.setSpectralBase(anySpectralBase);
	}
	
	
	/**
	* 
	* Assembles the customization and regularization matrices together 
	* to put in the form <code>A*x = b</code> and solves for x. This solution
	* are the coefficients for the MDFA real-time filtering process. 
	*  
	* @return MdfaMatrix b_coeffs 
	*      Is the solution to the 
	*      
	*/
	public MdfaMatrix solver() {
	
		double dev;
		
		MdfaMatrix des = anyReg.getQSmooth().mdfaMatrixMultTransB(anyReg.getDesignMatrix());
		MdfaMatrix reg_mat = anyReg.getDesignMatrix().mdfaMatrixMult(des);
		MdfaMatrix temp = anyReg.getQSmooth().mdfaMatrixMult(anyReg.getWeight());
		MdfaMatrix reg_xtxy = anyReg.getDesignMatrix().mdfaMatrixMult(temp);
		

		if(anyReg.getQSmooth().mdfaMatrixGet(0, 0) != 0.0) {
		     
	      double distangle = anyReg.getQSmooth().meanDiag(false)/reg_mat.meanDiag(false);
		  reg_mat.mdfaMatrixScale(distangle);	
		  reg_xtxy.mdfaMatrixScale(distangle);

		} 
		
	
		MdfaMatrix reX = anyCustomization.getREX().mdfaMatrixMultTransB(anyReg.getDesignMatrix());
		MdfaMatrix imX = anyCustomization.getIMX().mdfaMatrixMultTransB(anyReg.getDesignMatrix());
		
		MdfaMatrix XtX = reX.mdfaMatrixMultTransA(reX);
		MdfaMatrix imXtX = imX.mdfaMatrixMultTransA(imX);
		
		XtX.mdfaMatrixAdd(imXtX);

		MdfaMatrix temp2 = reX.mdfaMatrixMultTransA(anyCustomization.getREX().mdfaMatrixMult(anyReg.getWeight()));
		MdfaMatrix xtxy = imX.mdfaMatrixMultTransA(anyCustomization.getIMX().mdfaMatrixMult(anyReg.getWeight()));
		
		xtxy.mdfaMatrixAdd(temp2);
		xtxy.mdfaMatrixScale(-1.0);
        dev = XtX.meanDiag(true); 
        
        MdfaMatrix b = reX.mdfaMatrixMultTransA(anyCustomization.getGamma());
        b.mdfaMatrixAdd(xtxy);
        reg_xtxy.mdfaMatrixScale(-dev);
        b.mdfaMatrixAdd(reg_xtxy);

        reg_mat.mdfaMatrixScale(dev);
        XtX.mdfaMatrixAdd(reg_mat);
     
        XtX.mdfaSolve(b);
        
        MdfaMatrix b_coeffs = anyReg.getDesignMatrix().mdfaMatrixMultTransA(b);
        b_coeffs.mdfaMatrixAdd(anyReg.getWeight());

        return b_coeffs;
        
	}
        
}