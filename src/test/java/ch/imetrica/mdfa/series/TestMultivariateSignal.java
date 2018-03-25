package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.imetrica.mdfa.mdfa.MDFABase;

public class TestMultivariateSignal {

	final double eps = .0000000001;
	
	@Test
	public void testBandPassGeneration() {
		
		MDFABase[] anyMDFAs = new MDFABase[3];
		
		anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/15.0);
		
		anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
				.setBandPassCutoff(Math.PI/15.0);
		
		anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
                .setBandPassCutoff(Math.PI/10.0);
				
		anyMDFAs[2].setBandPassCutoff(Math.PI/5.0);
		assertEquals(Math.PI/10.0, anyMDFAs[2].getBandPassCutoff(), eps);
		
		anyMDFAs[1].setLowpassCutoff(Math.PI/16.0);
		assertEquals(Math.PI/10.0, anyMDFAs[1].getLowPassCutoff(), eps);
		
		assertEquals(0, anyMDFAs[0].getBandPassCutoff(), eps);
		assertEquals(Math.PI/15.0, anyMDFAs[1].getBandPassCutoff(), eps);
		assertEquals(Math.PI/10.0, anyMDFAs[1].getLowPassCutoff(), eps);
		
		
		
	}

}
