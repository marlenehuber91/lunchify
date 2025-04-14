package lunchifyTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import backend.model.InvoiceCategory;

public class InvoiceCategoryTest { //created with AI, changed by the team

	@Test
	public void testRestaurantCalculateReimbursementBelowLimit() {
		float amount = 15.0f;
		float limit = 20.0f;
		float result = InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit);
		assertEquals(15.0f, result, 0.001);
	}
	
	@Test
	public void testRestaurantCalculateReimbursementAboveLimit() {
		float amount = 25.0f;
		float limit = 20.0f;
		float result = InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit);
		assertEquals(20.0f, result, 0.001f);
	}
	
	 @Test
	    public void testSupermarketCalculateReimbursementAboveLimit() {
	        float amount = 30.0f;
	        float limit = 25.0f;
	        float result = InvoiceCategory.SUPERMARKET.calculateReimbursement(amount, limit);
	        assertEquals(25.0f, result, 0.001);
	    }

	    @Test
	    public void testCalculateReimbursementExactLimit() {
	        float amount = 20.0f;
	        float limit = 20.0f;
	        assertEquals(20.0f, InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit), 0.001);
	        assertEquals(20.0f, InvoiceCategory.SUPERMARKET.calculateReimbursement(amount, limit), 0.001);
	    }

	    @Test
	    public void testCalculateReimbursementZero() {
	        float amount = 0.0f;
	        float limit = 20.0f;
	        assertEquals(0.0f, InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit), 0.001);
	        assertEquals(0.0f, InvoiceCategory.SUPERMARKET.calculateReimbursement(amount, limit), 0.001);
	    }	
	
}
