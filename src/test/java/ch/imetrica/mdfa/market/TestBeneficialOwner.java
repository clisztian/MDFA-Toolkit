package ch.imetrica.mdfa.market;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestBeneficialOwner {

	@Test
	public void testBeneficialOwner() {
		
		BenificialOwner owner = new BenificialOwner();
		

		owner.addCrossStrategy("2019-01-01", 10.0, 11.0, -1.0);
		owner.addCrossStrategy("2019-01-02", 10.0, 11.0, -1.0);
		owner.addCrossStrategy("2019-01-03", 10.0, 11.0, -1.0);
		owner.addCrossStrategy("2019-01-04", 10.0, 11.0, 1.0);
		owner.addCrossStrategy("2019-01-05", 11.0, 12.0, 1.0);
		owner.addCrossStrategy("2019-01-06", 12.0, 13.0, -1.0);
		owner.addCrossStrategy("2019-01-07", 13.0, 14.0, -1.0);
		owner.addCrossStrategy("2019-01-08", 14.0, 15.0, -1.0);
		owner.addCrossStrategy("2019-01-09", 9.0, 20.0, 1.0);
		owner.addCrossStrategy("2019-01-10", 13.0, 14.0, -1.0);
		owner.addCrossStrategy("2019-01-11", 14.0, 19.0, -1.0);
		owner.addCrossStrategy("2019-01-12", 2.0, 10.0, 1.0);
		owner.addCrossStrategy("2019-01-13", 5.0, 15.0, 1.0);
		owner.addCrossStrategy("2019-01-14", 1.0, 1.0, -1.0);
		
		assertEquals(0.0, owner.getUnrealized().get(0).getValue(), 0.000001);
		assertEquals(0.0, owner.getUnrealized().get(1).getValue(), 0.000001);
		assertEquals(0.0, owner.getUnrealized().get(2).getValue(), 0.000001);
		assertEquals(1.0, owner.getUnrealized().get(3).getValue(), 0.000001);
		assertEquals(2.0, owner.getUnrealized().get(4).getValue(), 0.000001);
		assertEquals(-1.0, owner.getUnrealized().get(5).getValue(), 0.000001);
		assertEquals(-2.0, owner.getUnrealized().get(6).getValue(), 0.000001);
		assertEquals(-3.0, owner.getUnrealized().get(7).getValue(), 0.000001);
		assertEquals(11.0, owner.getUnrealized().get(8).getValue(), 0.000001);	
		assertEquals(-1.0, owner.getUnrealized().get(9).getValue(), 0.000001);
		assertEquals(-6.0, owner.getUnrealized().get(10).getValue(), 0.000001);
		assertEquals(8.0, owner.getUnrealized().get(11).getValue(), 0.000001);
		assertEquals(13.0, owner.getUnrealized().get(12).getValue(), 0.000001);
		assertEquals(0.0, owner.getUnrealized().get(13).getValue(), 0.000001);
		
		assertEquals(0.0, owner.getRealized().get(0).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(1).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(2).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(3).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(4).getValue(), 0.000001);
		assertEquals(2.0, owner.getRealized().get(5).getValue(), 0.000001);
		assertEquals(2.0, owner.getRealized().get(6).getValue(), 0.000001);
		assertEquals(2.0, owner.getRealized().get(7).getValue(), 0.000001);
		assertEquals(5.0, owner.getRealized().get(8).getValue(), 0.000001);
		assertEquals(9.0, owner.getRealized().get(9).getValue(), 0.000001);
		assertEquals(9.0, owner.getRealized().get(10).getValue(), 0.000001);
		assertEquals(20.0, owner.getRealized().get(11).getValue(), 0.000001);	
		assertEquals(20.0, owner.getRealized().get(12).getValue(), 0.000001);
		assertEquals(19.0, owner.getRealized().get(13).getValue(), 0.000001);
	
	}
	
	@Test
	public void testBeneficialOwnerStrenght() {
		
		BenificialOwner owner = new BenificialOwner();
		

		owner.addSignalStrengthStrategy("2019-01-01", 10.0, 11.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-02", 10.0, 11.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-03", 10.0, 11.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-04", 10.0, 11.0, 1.0);
		owner.addSignalStrengthStrategy("2019-01-05", 11.0, 12.0, 1.0);
		owner.addSignalStrengthStrategy("2019-01-06", 12.0, 13.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-07", 13.0, 14.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-08", 14.0, 15.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-09", 9.0, 20.0, 1.0);
		owner.addSignalStrengthStrategy("2019-01-10", 13.0, 14.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-11", 14.0, 19.0, -1.0);
		owner.addSignalStrengthStrategy("2019-01-12", 2.0, 10.0, 1.0);
		owner.addSignalStrengthStrategy("2019-01-13", 5.0, 15.0, 1.0);
		owner.addSignalStrengthStrategy("2019-01-14", 1.0, 1.0, -1.0);
		
		assertEquals(-1.0, owner.getUnrealized().get(0).getValue(), 0.000001);
		assertEquals(-1.0, owner.getUnrealized().get(1).getValue(), 0.000001);
		assertEquals(-1.0, owner.getUnrealized().get(2).getValue(), 0.000001);
		assertEquals(1.0, owner.getUnrealized().get(3).getValue(), 0.000001);
		assertEquals(2.0, owner.getUnrealized().get(4).getValue(), 0.000001);
		assertEquals(-1.0, owner.getUnrealized().get(5).getValue(), 0.000001);
		assertEquals(-2.0, owner.getUnrealized().get(6).getValue(), 0.000001);
		assertEquals(-3.0, owner.getUnrealized().get(7).getValue(), 0.000001);
		assertEquals(11.0, owner.getUnrealized().get(8).getValue(), 0.000001);	
		assertEquals(-1.0, owner.getUnrealized().get(9).getValue(), 0.000001);
		assertEquals(-6.0, owner.getUnrealized().get(10).getValue(), 0.000001);
		assertEquals(8.0, owner.getUnrealized().get(11).getValue(), 0.000001);
		assertEquals(13.0, owner.getUnrealized().get(12).getValue(), 0.000001);
		assertEquals(0.0, owner.getUnrealized().get(13).getValue(), 0.000001);
		
		assertEquals(0.0, owner.getRealized().get(0).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(1).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(2).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(3).getValue(), 0.000001);
		assertEquals(0.0, owner.getRealized().get(4).getValue(), 0.000001);
		assertEquals(2.0, owner.getRealized().get(5).getValue(), 0.000001);
		assertEquals(2.0, owner.getRealized().get(6).getValue(), 0.000001);
		assertEquals(2.0, owner.getRealized().get(7).getValue(), 0.000001);
		assertEquals(5.0, owner.getRealized().get(8).getValue(), 0.000001);
		assertEquals(9.0, owner.getRealized().get(9).getValue(), 0.000001);
		assertEquals(9.0, owner.getRealized().get(10).getValue(), 0.000001);
		assertEquals(20.0, owner.getRealized().get(11).getValue(), 0.000001);	
		assertEquals(20.0, owner.getRealized().get(12).getValue(), 0.000001);
		assertEquals(19.0, owner.getRealized().get(13).getValue(), 0.000001);
	
	}

}
