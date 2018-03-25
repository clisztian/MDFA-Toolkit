package ch.imetrica.mdfa.kca;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * 
 * Construct a Kinetic Component decomposition 
 * of the time series into position, velocity, 
 * and acceleration components. Can then be used 
 * in conjunction with MDFA for detecting turning
 * points in trends. 
 * 
 * The KCA is based of the paper Kinetic Component Decomposition 
 * by Marcos Lopez de Prado and Richard Rebonato (2014)
 * 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public class KineticComponent {

	
	private double covarianceQ;


	public KineticComponent(double q) {
		
		this.covarianceQ = q;
		
	}
	
	
	public void createKalmanMatrices() {
		
		double dt = .1;
		
		final RealMatrix A = MatrixUtils.createRealMatrix(new double[][] {
            { 1, dt, 0,  0 },
            { 0,  1, 0,  0 },
            { 0,  0, 1, dt },
            { 0,  0, 0,  1 }       
        });
		
		final RealMatrix H = MatrixUtils.createRealIdentityMatrix(3);
		H.scalarMultiply(covarianceQ);
		
		
	}
	
	
}
