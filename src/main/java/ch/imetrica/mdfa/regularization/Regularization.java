package ch.imetrica.mdfa.regularization;

import ch.imetrica.mdfa.matrix.MdfaMatrix;
import ch.imetrica.mdfa.mdfa.MDFABase;


/**
 * The Regularization class holds the regularization matrix
 * Q for the regularization effects along with a design matrix
 * that maps the dimensions of the system to handle the constraints
 * i1 and i2. 
 * 
 * 
 * 
 * @author lisztian
 *
 */
public class Regularization {

	
		
	private MdfaMatrix Q_smooth; 
	private MdfaMatrix des_mat; 
	private MdfaMatrix w_eight;
	private MDFABase anyMDFA;
	private MdfaMatrix Q_decay;
	private MdfaMatrix Q_cross;
	

	/**
	 * 
	 * Constructs the regularization and design matrices 
	 * according to the MDFABase object that holds all the necessary 
	 * parameters for construction the system. Needs a MDFABase 
	 * object. Checks on parameters inside the MDFABase object should 
	 * have already been made in the MDFABase class.
	 * 
	 * @param anyMDFA
	 *     The MDFABase object holding all the necessary 
	 *     mdfa filtering parameters
	 */
	public Regularization(MDFABase anyMDFA) {
		
		this.anyMDFA = anyMDFA;
		this.computeWeightMatrix();
		this.computeRegularizationMatrices();
	}
	
	
	public void setMDFABase(MDFABase anyMDFA) {
		this.anyMDFA = anyMDFA;
	}
	
	/**
	 * 
	 * Returns the regularization matrix that accounts for 
	 * all the regulatization effects in one matrix
	 * 
	 * @return Q_smooth
	 *     The aggregate regularization matrix 
	 *     that includes smooth, decays, and cross, 
	 *     all in one
	 */
	public MdfaMatrix getQSmooth() {
		return Q_smooth;
	}

	/**
	 * 
	 * Returns the design matrix which regulates the dimensions 
	 * of the final system taking into account the i1 and i2 
	 * constraints and the lag operator 
	 * 
	 * @return des_mat
	 *     The final design matrix 
	 */
	public MdfaMatrix getDesignMatrix() {
		return des_mat;
	}

	/**
	 * 
	 * Returns the design vector for the right-hand side 
	 * of the final system of equations taking into account the i1 and i2
	 * constraints and the lag operator along with the shift constraint
	 * at frequency zero 
	 * 
	 * @return w_eight
	 *      The right-hand side vector of the final equation
	 *    
	 */
	public MdfaMatrix getWeight() {
		return w_eight;
	}
	
	
	private void computeWeightMatrix() {
		
		
		int L = anyMDFA.getFilterLength(); 
		int i1 = anyMDFA.getI1();
		int i2 = anyMDFA.getI2(); 
		double lag = anyMDFA.getLag(); 
		int nseries = anyMDFA.getNSeries();
		double shift_constraint = anyMDFA.getShift_constraint();
		double[] weight_constraint = new double[nseries];
		weight_constraint[0] = 1.0;	
		w_eight = new MdfaMatrix(nseries*L, 1);
		
		
	    if(i1 == 1) {
	    	
	      if(i2 == 1)  {
	         
	        for(int j=0; j < nseries; j++) {
	        	
	        	if(lag < 1) {
	        		
	        		w_eight.mdfaVectorSet( j*L,    -(lag-1.0)*weight_constraint[j] - shift_constraint);
	        		w_eight.mdfaVectorSet( j*L + 1,  1.0*lag*weight_constraint[j] + shift_constraint); 
	        	} 
	        	else {
	        	  
	        		w_eight.mdfaVectorSet((int)lag + j*L, weight_constraint[j] - shift_constraint);
	        		w_eight.mdfaVectorSet((int)lag + j*L + 1, shift_constraint);     
	            }
	        }
	      }
	      else {
	    	  
	        for(int j=0; j < nseries; j++) {
	        	
	          if (lag<1) {
	        	  w_eight.mdfaVectorSet( j*L, weight_constraint[j]); 	       
	          } 
	          else {
	        	  w_eight.mdfaVectorSet( (int)lag + j*L,weight_constraint[j]); 
	          }
	        }
	      }
		}    
		else {
			
		    if(i2 == 1) {
		    
		      for(int j=0;j<nseries; j++) {
		        
		    	if (lag<1) {
		        	w_eight.mdfaVectorSet( L*j, 0); 
		        	w_eight.mdfaVectorSet( L*j+1, shift_constraint/(1.0-lag));
		        }
		        else {
		        	w_eight.mdfaVectorSet( (int)lag + L*j, 0); 
		        	w_eight.mdfaVectorSet( (int)lag + L*j+1, shift_constraint);
		        }        
		      }
		    } 
		}
	}
	
	
	
	private void computeRegularizationMatrices() {
		
		
		double cross_cor      = 100*Math.tan(Math.min(anyMDFA.getCrossCorr(),0.999999)*Math.PI/2.0);
		double decay_length   = 100*Math.tan(Math.min(anyMDFA.getDecayStart(),0.999999)*Math.PI/2.0);
		double decay_strength = 100*Math.tan(Math.min(anyMDFA.getDecayStrength(),0.999999)*Math.PI/2.0);
		double smooth         = 100*Math.tan(Math.min(anyMDFA.getSmooth(),0.999999)*Math.PI/2.0);
		
		
		int L = anyMDFA.getFilterLength(); 
		int i1 = anyMDFA.getI1();
		int i2 = anyMDFA.getI2(); 
		double lag = anyMDFA.getLag(); 
		int nseries = anyMDFA.getNSeries();
		

		//---  create dimensions of regularization matrices
		int i,j,k,start;
		int ncols2 = L*nseries;
		int ncols = L*nseries;
		int il; 
		double im; 
		double in;
		
		if(L > 2) {
		  if(i2 == 1) {
		     if(i1 == 0) ncols2 = (L-1)*nseries;
		     else ncols2 = (L-2)*nseries;
		  }
		  else {
		     if(i1 == 0) ncols2 = L*nseries;
		     else ncols2 = (L-1)*nseries;
		  }
		}

		Q_smooth = new MdfaMatrix(ncols, ncols);  
		des_mat = new MdfaMatrix(ncols2,ncols); 
		Q_decay = new MdfaMatrix(ncols,ncols);          
		Q_cross = new MdfaMatrix(ncols,ncols);  
		
		MdfaMatrix _Q_smooth = new MdfaMatrix(L,L);                
		MdfaMatrix _Q_decay = new MdfaMatrix(L,L); 


	    //--- set initial values -------
		if(L > 2) {
			
			_Q_smooth.mdfaMatrixSet(0,0,  1.0*smooth); 
		    _Q_smooth.mdfaMatrixSet(0,1, -2.0*smooth);  
		    _Q_smooth.mdfaMatrixSet(0,2,  1.0*smooth);  
		    _Q_decay.mdfaMatrixSet( 0, 0, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(0.0-lag)))); 
		 
		    _Q_smooth.mdfaMatrixSet(1,0,  -2.0*smooth); 
		    _Q_smooth.mdfaMatrixSet(1,1,   5.0*smooth);
		    _Q_smooth.mdfaMatrixSet(1,2,  -4.0*smooth);
		    _Q_smooth.mdfaMatrixSet(1,3,   1.0*smooth);
		    _Q_decay.mdfaMatrixSet( 1, 1, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(1.0-lag)))); 

		    i=L-1;
		    _Q_smooth.mdfaMatrixSet(i,i-2,  1.0*smooth);        
		    _Q_smooth.mdfaMatrixSet(i,i-1, -2.0*smooth);     
		    _Q_smooth.mdfaMatrixSet(i,i,    1.0*smooth);    
		    _Q_decay.mdfaMatrixSet( i, i, decay_strength*Math.pow(1.0 + decay_length, (2.0*Math.abs(i-lag))));        

		    i=L-2;
		    _Q_smooth.mdfaMatrixSet(i,i-2,  1.0*smooth); 
		    _Q_smooth.mdfaMatrixSet(i,i-1, -4.0*smooth);
		    _Q_smooth.mdfaMatrixSet(i,i,    5.0*smooth);
		    _Q_smooth.mdfaMatrixSet(i,i+1, -2.0*smooth);
		    _Q_decay.mdfaMatrixSet( i, i, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(i-lag))));

		    
		    //------------ Now do the rest -------------------------
		    for(i=2;i<L-2;i++)
		    {     
		      _Q_decay.mdfaMatrixSet( i, i, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(1.0*i-lag))));                  
		      _Q_smooth.mdfaMatrixSet(i,i-2,  1.0*smooth); 
		      _Q_smooth.mdfaMatrixSet(i,i-1, -4.0*smooth);
		      _Q_smooth.mdfaMatrixSet(i,i,    6.0*smooth);
		      _Q_smooth.mdfaMatrixSet(i,i+1, -4.0*smooth);
		      _Q_smooth.mdfaMatrixSet(i,i+2, 1.0*smooth);
		    }


		    for(j=0;j<nseries;j++)
		    {
		      start = j*L;
		      for(i=0;i<L;i++)
		      { 
		        for(k=0;k<L;k++)
		        {
		           Q_smooth.mdfaMatrixSet(start + i, start + k, _Q_smooth.mdfaMatrixGet(i,k));
		           Q_decay.mdfaMatrixSet(start + i, start + k, _Q_decay.mdfaMatrixGet(i,k));
		        }
		      }
		    }
		 }
		
		//---- set cross -------------------------------------------------------------
	    if(nseries > 1) {
	      for(i = 0; i < ncols; i++) Q_cross.mdfaMatrixSet(i, i, 1.0);	    
	    }
	    
	    for(i=0;i<nseries;i++) {
	      for(j=0;j<L;j++) {
		    for(k=0;k<nseries;k++) {
		    	double val = Q_cross.mdfaMatrixGet( i*L+j, k*L+j) - 1.0/(1.0*nseries);   
		    	Q_cross.mdfaMatrixSet(i*L+j, j + k*L, val);
		    }
	      }
	    }
	    
	    //----------- 
	    double trace=0.0; double strace = 0.0; double ctrace = 0.0;
	    for(i=0;i<L;i++) 
	    {
	      trace = trace + Q_decay.mdfaMatrixGet(i,i);  
	      strace = strace + Q_smooth.mdfaMatrixGet(i,i);
	      ctrace = ctrace + Q_cross.mdfaMatrixGet(i,i);
	    }   


	    if(decay_strength > 0) 
	    {Q_decay.mdfaMatrixScale(decay_strength/(nseries*trace));}
	    if(smooth > 0) 
	    {Q_smooth.mdfaMatrixScale(smooth/(nseries*strace));}
	    if(cross_cor > 0.0) 
	    {Q_cross.mdfaMatrixScale( cross_cor/(nseries*ctrace));}
	 
	    Q_cross.mdfaMatrixScale(cross_cor);		
		
			
	  if(i2 == 1) {    
		
		  if(i1 == 1) {
	      for(i=0;i<L-2;i++) {       
	       
	       if(lag<1) {
	         
	          for(j=0;j<nseries; j++) {          
		       start = j*L;
	           des_mat.mdfaMatrixSet(i, i + 2 + start, 1.0); 
	           des_mat.mdfaMatrixSet(i, start        , i+1); 
	           des_mat.mdfaMatrixSet(i, 1 + start   ,-(i+2)); 
	          }         
	       }
	       else {
	    	   if(i >= lag) {il=i+2; im = i-lag+1.0;  in = -1.0*(i-lag+2.0);} 
	    	   else {il = i; im = -(lag+2.0-(i+1)); in = 1.0*(lag+2.0-i);}
		  
	           for(j=0;j<nseries; j++) {        
		         start = j*L; 
	             des_mat.mdfaMatrixSet(i, il + start     , 1.0); 
	             des_mat.mdfaMatrixSet(i, (int)lag+ start     , im);  
	             des_mat.mdfaMatrixSet(i, (int)lag + 1 + start, in);    
	          }        
	       }
	     }
	     for(j=1;j<nseries;j++) {
	    	 
	       start = j*(L-2); 
	       for (i=0;i<L-2;i++)  {
		   if (lag<1) {
		        
		         des_mat.mdfaMatrixSet(i + start, i + 2,  -1.0);  
	             des_mat.mdfaMatrixSet(i + start, 0,    -(i+1));  
	             des_mat.mdfaMatrixSet(i + start, 1,    (i+2));                  
	       } 
	       else {
		     
		        if(i >= lag) {il=i+2; im = i-lag+1.0; in = -1.0*(i-lag+2.0);} 
		        else {il = i; im = -(lag+2.0-(i+1.0)); in = lag+1.0-(i+1.0);}
		       
		        des_mat.mdfaMatrixSet( i + start, il, -1.0);
		        des_mat.mdfaMatrixSet( i + start, (int)lag, -im);  
		        des_mat.mdfaMatrixSet( i + start, (int)lag+1, -in);   
	       }
           if (lag<1) {
		        
		         des_mat.mdfaMatrixSet(i + start, i + 2 + j*L, 1.0);  
	             des_mat.mdfaMatrixSet(i + start, j*L,   (i+1.0));  
	             des_mat.mdfaMatrixSet(i + start, 1 + j*L, -(i+2.0));          
	       } 
	       else {
		     
		        if(i >= lag) {il=i+2; im = i-lag+1.0; in = -1.0*(i-lag+2.0);} 
		        else {il = i; im = -(lag+2.0-(i+1.0)); in = lag+1.0-(i+1.0);}
		       
		        des_mat.mdfaMatrixSet( i + start, il + j*L, -1.0); 
		        des_mat.mdfaMatrixSet( i + start, (int)lag + j*L, im);  
		        des_mat.mdfaMatrixSet( i + start, (int)lag+1 + j*L, in); 
	       }  
	     }
	   }
	  }
	  else {
	    for (i=0;i<L-1;i++) {

		  for(j=0;j<nseries;j++)
		  {
		    	    	    
		    if(lag<1)  {
		      
		      if(i < 1) {il=i; im = lag/(1.0-lag);} else {il=i+1; im = (lag-(i+1.0))/(1.0-lag);} 
		  
		      des_mat.mdfaMatrixSet( i, il + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, 1 + j*L, im); 

		    } 
		    else {
		      
		      if(i < lag+1) {il=i; im = lag + 1.0 - (i+1.0);} 
		      else {il=i+1; im = lag - (i+1);} 
		  
		      des_mat.mdfaMatrixSet( i, il + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, (int)lag+1 + j*L, im);  
		     
	        }
	      }
	   }
	      
	   for (j=1;j<nseries;j++) {
		for (i=0;i<L-1;i++)  {    
	       
		    if (lag<1) {
		        if(i < 1) {il=i; im = lag/(1.0-lag);} else {il=i+1; im = (lag-(i+1.0))/(1.0-lag);}
		        des_mat.mdfaMatrixSet( i + j*(L-1), il, -1.0); 
		        des_mat.mdfaMatrixSet( i + j*(L-1), 1, -im);   
	        } 
	        else {
			  if(i < lag+1) {il=i; im = lag+1.0-(i+1.0);} else {il=i+1; im = (lag-(i+1.0));}
			  des_mat.mdfaMatrixSet( i + j*(L-1), il, -1.0);
			  des_mat.mdfaMatrixSet( i + j*(L-1), (int)lag+1, -im); 
	        }
 
		    if (lag<1) {
			
		        if(i < 1) {il=i; im = lag/(1.0-lag);} else {il=i+1; im = (lag-(i+1.0))/(1.0-lag);} 
		        des_mat.mdfaMatrixSet( i+j*(L-1), il + j*L, 1.0); 
		        des_mat.mdfaMatrixSet( i+j*(L-1), 1 + j*L, im);  
			           
	        } 
	        else {
			  
	        	if(i < lag+1) {il=i; im = lag+1.0-(i+1.0);} else {il=i+1; im = (lag-(i+1.0));}
			    des_mat.mdfaMatrixSet( i+j*(L-1), il + j*L, 1.0); 
		        des_mat.mdfaMatrixSet( i+j*(L-1), (int)lag+1 + j*L, im);  
			
	        }                        
	      }
	    }
	  }
	}
	else {
	  if (i1==1)  {
	      
	      for(i=0; i < L-1; i++)  {
		    for(j=0;j<nseries;j++) {

	         if (lag<1) {
		      des_mat.mdfaMatrixSet( i, i + 1 + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, j*L, -1.0);     
	         } 
	         else {
		      if(i >= lag) {il = i+1;} else {il = i;} 
		      des_mat.mdfaMatrixSet( i, il + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, (int)lag + j*L, -1.0);     
	         }
		   }
	     }
	     for (j=1;j<nseries;j++) {
		    start = j*(L-1);
	        for (i=0;i<L-1;i++) {
	          
	          if (lag<1) {    
		      
	              des_mat.mdfaMatrixSet( i+start, i+1, -1.0); 
	              des_mat.mdfaMatrixSet( i+start, 0, 1.0); 
	              des_mat.mdfaMatrixSet( i+start, i+1+j*L, 1.0);  
	              des_mat.mdfaMatrixSet( i+start, j*L, -1.0); 
	          } 
	          else {
		      
		       if(i >= lag) {il = i+1;} else {il = i;} 
	            des_mat.mdfaMatrixSet( i+start, il, -1.0);
	            des_mat.mdfaMatrixSet( i+start, (int)lag, 1.0); 
	            des_mat.mdfaMatrixSet( i+start, il + j*L, 1.0);  
	            des_mat.mdfaMatrixSet( i+start, (int)lag + j*L, -1.0); 
	          }
	         }
	     }             
	    }
	    else {
	    	
	     for(j=0;j<nseries;j++) { 
	      start = j*L; 
	      for (i=0;i<L;i++) {
	        des_mat.mdfaMatrixSet( i, i + j*L, 1.0);
	      }
	     }
	     for(j=1;j<nseries;j++) {
	       start = j*L;
	       for (i=0;i<L;i++) {
	           des_mat.mdfaMatrixSet( i+start, i, -1.0); 
	           des_mat.mdfaMatrixSet( i+start, i+start, 1.0); 
	       } 
	     }
	    
	    }      
	  }
	    
	  if(nseries > 1) {
	       
		  MdfaMatrix Q_cdev = getQDeviation();
		  MdfaMatrix cross_dev = Q_cdev.mdfaMatrixMultTransB(des_mat);  
		  cross_dev.transpose(des_mat);
		  
		  Q_smooth.mdfaMatrixAdd(Q_decay);
		  Q_smooth.mdfaMatrixAdd(Q_cross);	        
	  }
	  else {
		  Q_smooth.mdfaMatrixAdd(Q_decay);
	  }  
	       
	}
	


    public void adjustRegularizationMatrices() {
		
		
		double cross_cor      = 100*Math.tan(Math.min(anyMDFA.getCrossCorr(),0.999999)*Math.PI/2.0);
		double decay_length   = 100*Math.tan(Math.min(anyMDFA.getDecayStart(),0.999999)*Math.PI/2.0);
		double decay_strength = 100*Math.tan(Math.min(anyMDFA.getDecayStrength(),0.999999)*Math.PI/2.0);
		double smooth         = 100*Math.tan(Math.min(anyMDFA.getSmooth(),0.999999)*Math.PI/2.0);
		
		
		int L = anyMDFA.getFilterLength(); 
		int i1 = anyMDFA.getI1();
		int i2 = anyMDFA.getI2(); 
		double lag = anyMDFA.getLag(); 
		int nseries = anyMDFA.getNSeries();
		
		

		//---  create dimensions of regularization matrices
		int i,j,k,start;
		int ncols2 = L*nseries;
		int ncols = L*nseries;
		int il; 
		double im; 
		double in;
		
		if(L > 2) {
		  if(i2 == 1) {
		     if(i1 == 0) ncols2 = (L-1)*nseries;
		     else ncols2 = (L-2)*nseries;
		  }
		  else {
		     if(i1 == 0) ncols2 = L*nseries;
		     else ncols2 = (L-1)*nseries;
		  }
		}

		Q_smooth.zero();  
		des_mat.zero();
		Q_decay.zero();    
		Q_cross.zero();
		
		MdfaMatrix _Q_smooth = new MdfaMatrix(L,L);                
		MdfaMatrix _Q_decay = new MdfaMatrix(L,L); 


	    //--- set initial values -------
		if(L > 2) {
			
			_Q_smooth.mdfaMatrixSet(0,0,  1.0*smooth); 
		    _Q_smooth.mdfaMatrixSet(0,1, -2.0*smooth);  
		    _Q_smooth.mdfaMatrixSet(0,2,  1.0*smooth);  
		    _Q_decay.mdfaMatrixSet( 0, 0, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(0.0-lag)))); 
		 
		    _Q_smooth.mdfaMatrixSet(1,0,  -2.0*smooth); 
		    _Q_smooth.mdfaMatrixSet(1,1,   5.0*smooth);
		    _Q_smooth.mdfaMatrixSet(1,2,  -4.0*smooth);
		    _Q_smooth.mdfaMatrixSet(1,3,   1.0*smooth);
		    _Q_decay.mdfaMatrixSet( 1, 1, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(1.0-lag)))); 

		    i=L-1;
		    _Q_smooth.mdfaMatrixSet(i,i-2,  1.0*smooth);        
		    _Q_smooth.mdfaMatrixSet(i,i-1, -2.0*smooth);     
		    _Q_smooth.mdfaMatrixSet(i,i,    1.0*smooth);    
		    _Q_decay.mdfaMatrixSet( i, i, decay_strength*Math.pow(1.0 + decay_length, (2.0*Math.abs(i-lag))));        

		    i=L-2;
		    _Q_smooth.mdfaMatrixSet(i,i-2,  1.0*smooth); 
		    _Q_smooth.mdfaMatrixSet(i,i-1, -4.0*smooth);
		    _Q_smooth.mdfaMatrixSet(i,i,    5.0*smooth);
		    _Q_smooth.mdfaMatrixSet(i,i+1, -2.0*smooth);
		    _Q_decay.mdfaMatrixSet( i, i, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(i-lag))));

		    
		    //------------ Now do the rest -------------------------
		    for(i=2;i<L-2;i++)
		    {     
		      _Q_decay.mdfaMatrixSet( i, i, decay_strength*Math.pow(1.0 + decay_length,  (2.0*Math.abs(1.0*i-lag))));                  
		      _Q_smooth.mdfaMatrixSet(i,i-2,  1.0*smooth); 
		      _Q_smooth.mdfaMatrixSet(i,i-1, -4.0*smooth);
		      _Q_smooth.mdfaMatrixSet(i,i,    6.0*smooth);
		      _Q_smooth.mdfaMatrixSet(i,i+1, -4.0*smooth);
		      _Q_smooth.mdfaMatrixSet(i,i+2, 1.0*smooth);
		    }


		    for(j=0;j<nseries;j++)
		    {
		      start = j*L;
		      for(i=0;i<L;i++)
		      { 
		        for(k=0;k<L;k++)
		        {
		           Q_smooth.mdfaMatrixSet(start + i, start + k, _Q_smooth.mdfaMatrixGet(i,k));
		           Q_decay.mdfaMatrixSet(start + i, start + k, _Q_decay.mdfaMatrixGet(i,k));
		        }
		      }
		    }
		 }
		
		//---- set cross -------------------------------------------------------------
	    if(nseries > 1) {
	      for(i = 0; i < ncols; i++) Q_cross.mdfaMatrixSet(i, i, 1.0);	    
	    }
	    
	    for(i=0;i<nseries;i++) {
	      for(j=0;j<L;j++) {
		    for(k=0;k<nseries;k++) {
		    	double val = Q_cross.mdfaMatrixGet( i*L+j, k*L+j) - 1.0/(1.0*nseries);   
		    	Q_cross.mdfaMatrixSet(i*L+j, j + k*L, val);
		    }
	      }
	    }
	    
	    //----------- 
	    double trace=0.0; double strace = 0.0; double ctrace = 0.0;
	    for(i=0;i<L;i++) 
	    {
	      trace = trace + Q_decay.mdfaMatrixGet(i,i);  
	      strace = strace + Q_smooth.mdfaMatrixGet(i,i);
	      ctrace = ctrace + Q_cross.mdfaMatrixGet(i,i);
	    }   


	    if(decay_strength > 0) 
	    {Q_decay.mdfaMatrixScale(decay_strength/(nseries*trace));}
	    if(smooth > 0) 
	    {Q_smooth.mdfaMatrixScale(smooth/(nseries*strace));}
	    if(cross_cor > 0.0) 
	    {Q_cross.mdfaMatrixScale( cross_cor/(nseries*ctrace));}
	 
	    Q_cross.mdfaMatrixScale(cross_cor);		
		
			
	  if(i2 == 1) {    
		
		  if(i1 == 1) {
	      for(i=0;i<L-2;i++) {       
	       
	       if(lag<1) {
	         
	          for(j=0;j<nseries; j++) {          
		       start = j*L;
	           des_mat.mdfaMatrixSet(i, i + 2 + start, 1.0); 
	           des_mat.mdfaMatrixSet(i, start        , i+1); 
	           des_mat.mdfaMatrixSet(i, 1 + start   ,-(i+2)); 
	          }         
	       }
	       else {
	    	   if(i >= lag) {il=i+2; im = i-lag+1.0;  in = -1.0*(i-lag+2.0);} 
	    	   else {il = i; im = -(lag+2.0-(i+1)); in = 1.0*(lag+2.0-i);}
		  
	           for(j=0;j<nseries; j++) {        
		         start = j*L; 
	             des_mat.mdfaMatrixSet(i, il + start     , 1.0); 
	             des_mat.mdfaMatrixSet(i, (int)lag+ start     , im);  
	             des_mat.mdfaMatrixSet(i, (int)lag + 1 + start, in);    
	          }        
	       }
	     }
	     for(j=1;j<nseries;j++) {
	    	 
	       start = j*(L-2); 
	       for (i=0;i<L-2;i++)  {
		   if (lag<1) {
		        
		         des_mat.mdfaMatrixSet(i + start, i + 2,  -1.0);  
	             des_mat.mdfaMatrixSet(i + start, 0,    -(i+1));  
	             des_mat.mdfaMatrixSet(i + start, 1,    (i+2));                  
	       } 
	       else {
		     
		        if(i >= lag) {il=i+2; im = i-lag+1.0; in = -1.0*(i-lag+2.0);} 
		        else {il = i; im = -(lag+2.0-(i+1.0)); in = lag+1.0-(i+1.0);}
		       
		        des_mat.mdfaMatrixSet( i + start, il, -1.0);
		        des_mat.mdfaMatrixSet( i + start, (int)lag, -im);  
		        des_mat.mdfaMatrixSet( i + start, (int)lag+1, -in);   
	       }
           if (lag<1) {
		        
		         des_mat.mdfaMatrixSet(i + start, i + 2 + j*L, 1.0);  
	             des_mat.mdfaMatrixSet(i + start, j*L,   (i+1.0));  
	             des_mat.mdfaMatrixSet(i + start, 1 + j*L, -(i+2.0));          
	       } 
	       else {
		     
		        if(i >= lag) {il=i+2; im = i-lag+1.0; in = -1.0*(i-lag+2.0);} 
		        else {il = i; im = -(lag+2.0-(i+1.0)); in = lag+1.0-(i+1.0);}
		       
		        des_mat.mdfaMatrixSet( i + start, il + j*L, -1.0); 
		        des_mat.mdfaMatrixSet( i + start, (int)lag + j*L, im);  
		        des_mat.mdfaMatrixSet( i + start, (int)lag+1 + j*L, in); 
	       }  
	     }
	   }
	  }
	  else {
	    for (i=0;i<L-1;i++) {

		  for(j=0;j<nseries;j++)
		  {
		    	    	    
		    if(lag<1)  {
		      
		      if(i < 1) {il=i; im = lag/(1.0-lag);} else {il=i+1; im = (lag-(i+1.0))/(1.0-lag);} 
		  
		      des_mat.mdfaMatrixSet( i, il + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, 1 + j*L, im); 

		    } 
		    else {
		      
		      if(i < lag+1) {il=i; im = lag + 1.0 - (i+1.0);} 
		      else {il=i+1; im = lag - (i+1);} 
		  
		      des_mat.mdfaMatrixSet( i, il + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, (int)lag+1 + j*L, im);  
		     
	        }
	      }
	   }
	      
	   for (j=1;j<nseries;j++) {
		for (i=0;i<L-1;i++)  {    
	       
		    if (lag<1) {
		        if(i < 1) {il=i; im = lag/(1.0-lag);} else {il=i+1; im = (lag-(i+1.0))/(1.0-lag);}
		        des_mat.mdfaMatrixSet( i + j*(L-1), il, -1.0); 
		        des_mat.mdfaMatrixSet( i + j*(L-1), 1, -im);   
	        } 
	        else {
			  if(i < lag+1) {il=i; im = lag+1.0-(i+1.0);} else {il=i+1; im = (lag-(i+1.0));}
			  des_mat.mdfaMatrixSet( i + j*(L-1), il, -1.0);
			  des_mat.mdfaMatrixSet( i + j*(L-1), (int)lag+1, -im); 
	        }
 
		    if (lag<1) {
			
		        if(i < 1) {il=i; im = lag/(1.0-lag);} else {il=i+1; im = (lag-(i+1.0))/(1.0-lag);} 
		        des_mat.mdfaMatrixSet( i+j*(L-1), il + j*L, 1.0); 
		        des_mat.mdfaMatrixSet( i+j*(L-1), 1 + j*L, im);  
			           
	        } 
	        else {
			  
	        	if(i < lag+1) {il=i; im = lag+1.0-(i+1.0);} else {il=i+1; im = (lag-(i+1.0));}
			    des_mat.mdfaMatrixSet( i+j*(L-1), il + j*L, 1.0); 
		        des_mat.mdfaMatrixSet( i+j*(L-1), (int)lag+1 + j*L, im);  
			
	        }                        
	      }
	    }
	  }
	}
	else {
	  if (i1==1)  {
	      
	      for(i=0; i < L-1; i++)  {
		    for(j=0;j<nseries;j++) {

	         if (lag<1) {
		      des_mat.mdfaMatrixSet( i, i + 1 + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, j*L, -1.0);     
	         } 
	         else {
		      if(i >= lag) {il = i+1;} else {il = i;} 
		      des_mat.mdfaMatrixSet( i, il + j*L, 1.0); 
		      des_mat.mdfaMatrixSet( i, (int)lag + j*L, -1.0);     
	         }
		   }
	     }
	     for (j=1;j<nseries;j++) {
		    start = j*(L-1);
	        for (i=0;i<L-1;i++) {
	          
	          if (lag<1) {    
		      
	              des_mat.mdfaMatrixSet( i+start, i+1, -1.0); 
	              des_mat.mdfaMatrixSet( i+start, 0, 1.0); 
	              des_mat.mdfaMatrixSet( i+start, i+1+j*L, 1.0);  
	              des_mat.mdfaMatrixSet( i+start, j*L, -1.0); 
	          } 
	          else {
		      
		       if(i >= lag) {il = i+1;} else {il = i;} 
	            des_mat.mdfaMatrixSet( i+start, il, -1.0);
	            des_mat.mdfaMatrixSet( i+start, (int)lag, 1.0); 
	            des_mat.mdfaMatrixSet( i+start, il + j*L, 1.0);  
	            des_mat.mdfaMatrixSet( i+start, (int)lag + j*L, -1.0); 
	          }
	         }
	     }             
	    }
	    else {
	    	
	     for(j=0;j<nseries;j++) { 
	      start = j*L; 
	      for (i=0;i<L;i++) {
	        des_mat.mdfaMatrixSet( i, i + j*L, 1.0);
	      }
	     }
	     for(j=1;j<nseries;j++) {
	       start = j*L;
	       for (i=0;i<L;i++) {
	           des_mat.mdfaMatrixSet( i+start, i, -1.0); 
	           des_mat.mdfaMatrixSet( i+start, i+start, 1.0); 
	       } 
	     }
	    
	    }      
	  }
	    
	  if(nseries > 1) {
	       
		  MdfaMatrix Q_cdev = getQDeviation();
		  MdfaMatrix cross_dev = Q_cdev.mdfaMatrixMultTransB(des_mat);  
		  cross_dev.transpose(des_mat);
		  
		  Q_smooth.mdfaMatrixAdd(Q_decay);
		  Q_smooth.mdfaMatrixAdd(Q_cross);	        
	  }
	  else {
		  Q_smooth.mdfaMatrixAdd(Q_decay);
	  }  
	  computeWeightMatrix();     
	}
	
	
	
	private MdfaMatrix getQDeviation() {
		
		   int ncols,start;	      
		   int L = anyMDFA.getFilterLength();
		   int nseries = anyMDFA.getNSeries();
		   
		   ncols = L*nseries;
		   MdfaMatrix Q_cdev_orig = new MdfaMatrix(ncols, ncols);
		   MdfaMatrix eye = new MdfaMatrix(ncols, ncols);
		   
		   for(int i = 0; i < ncols; i++) {
			   eye.mdfaMatrixSet(i, i, 1.0);
		   }
		   
		   for(int i=0; i < L; i++) {
			   
			   Q_cdev_orig.mdfaMatrixSet(i, i, 1.0);
			   Q_cdev_orig.mdfaMatrixSet(i, L+i, -1.0);
		   }
		   	 
		   for(int j=1; j < nseries; j++) { 
		     
			 start = j*L; 
		     for(int i=0; i < L; i++) {
		    	 
		    	 Q_cdev_orig.mdfaMatrixSet(start + i, i, 1.0);       
		    	 Q_cdev_orig.mdfaMatrixSet(start + i, start + i, 1.0); 
		    	 Q_cdev_orig.mdfaMatrixSet(i, start + i, -1.0);      
		     } 
		   }		 
		   Q_cdev_orig.mdfaSolve(eye);		 
		   return eye;
	}
	
	
}