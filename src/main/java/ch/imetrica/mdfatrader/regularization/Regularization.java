package ch.imetrica.mdfatrader.regularization;

import ch.imetrica.mdfatrader.matrix.MdfaMatrixGPU;

public class Regularization {

	
	
	double smooth; 
	double decay_strength;
	double decay_length;
	double cross_cor; 
	double shift_constraint;
	double lag; 
	double[] weight_constraint;
	
	int L; 
	int i1;
	int i2; 
	
	int nseries;
	
	MdfaMatrixGPU Q_smooth; 
	MdfaMatrixGPU Q_decay; 
	MdfaMatrixGPU Q_cross;
	MdfaMatrixGPU des_mat; 
	MdfaMatrixGPU w_eight;
	MdfaMatrixGPU Q_cdev;
	
	Regularization(int nseries,				/*Number of explanatory series, nseries = 1 if univariate */ 
			int L,  						/*Length of the filter */
			int i1, 						/*first derivative constraint */ 
			int i2, 						/*second derivative constraint */
			double lag, 					/*forecast or smoothing lag*/
			double smooth,                  /*coefficient smoothing regularization */
			double decay_strength,          /*decay strength of coefficients */
			double decay_length,            /*decay starting point */
			double cross_cor,               /*for multivariate, cross-regularization */
			double shift_constraint,        /*shifting constraint */
			double[] weight_constraint)     /*probably not needed */
	{
		
		this.cross_cor = 100*Math.tan(Math.min(cross_cor,0.999999)*Math.PI/2.0);
		this.decay_length = 100*Math.tan(Math.min(decay_length,0.999999)*Math.PI/2.0);
		this.decay_strength = 100*Math.tan(Math.min(decay_strength,0.999999)*Math.PI/2.0);
		this.smooth = 100*Math.tan(Math.min(smooth,0.999999)*Math.PI/2.0);
		this.shift_constraint = shift_constraint;
		this.weight_constraint = weight_constraint;
		
		this.L = L; 
		this.i1 = i1;
		this.i2 = i2; 
		this.lag = lag; 
		this.nseries = nseries;
		
		
	}
	
	
	private void computeWeightMatrix() {
		
		int j;
		
	    if(i1 == 1) {
	    	
	      if(i2 == 1)  {
	         
	        for(j=0; j<nseries; j++) {
	        	
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
	      else
	      {	      
	        for(j=0;j<nseries; j++)
	        {
	          if (lag<1) {
	        	  w_eight.mdfaVectorSet( j*L, weight_constraint[j]); 	       
	          } 
	          else {
	        	  w_eight.mdfaVectorSet( (int)lag + j*L,weight_constraint[j]); 
	          }
	        }
	      }
	  }    
	  else
	  {
	    if(i2 == 1)
	    {
	      for(j=0;j<nseries; j++)
	      {
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

		MdfaMatrixGPU _Q_smooth = new MdfaMatrixGPU(L,L);                
		MdfaMatrixGPU _Q_decay = new MdfaMatrixGPU(L,L); 
		MdfaMatrixGPU cross_dev = new MdfaMatrixGPU(ncols, ncols2);
		
		Q_smooth = new MdfaMatrixGPU(ncols, ncols);
		Q_decay = new MdfaMatrixGPU(ncols,ncols);          
		Q_cross = new MdfaMatrixGPU(ncols,ncols);    
		des_mat = new MdfaMatrixGPU(ncols2,ncols);   

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
	    
	  if(nseries > 1){
	       
		  cross_dev = Q_cdev.mdfaMatrixMult(des_mat);  
		  Q_smooth.mdfaMatrixAdd(Q_decay);
		  Q_smooth.mdfaMatrixAdd(Q_cross);	        
	  }
	  else 
	  {Q_smooth.mdfaMatrixAdd(Q_decay);}  
	    
	    
	    
	    
	    
	}
	
	
}