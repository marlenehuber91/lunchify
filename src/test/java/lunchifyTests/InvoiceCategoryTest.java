package lunchifyTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import backend.model.InvoiceCategory;

public class InvoiceCategoryTest { //created with AI, changed by the team

	@Test
	public void testRestaurantCalculateReimbursementBelowLimit() {
		float amount = 1.5f;
		float limit = 2.5f;
		float result = InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit);
		assertEquals(1.5f, result, 0.001);
	}
	
	@Test
	public void testRestaurantCalculateReimbursementAboveLimit() {
		float amount = 14.5f;
		float limit = 3.0f;
		float result = InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit);
		assertEquals(3.0f, result, 0.001f);
	}
	
	 @Test
	    public void testSupermarketCalculateReimbursementAboveLimit() {
	        float amount = 30.0f;
	        float limit = 2.5f;
	        float result = InvoiceCategory.SUPERMARKET.calculateReimbursement(amount, limit);
	        assertEquals(2.50, result, 0.001);
	    }

	    @Test
	    public void testCalculateReimbursementExactLimit() {
	        float amount = 2.0f;
	        float limit = 2.0f;
	        assertEquals(2.0f, InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit), 0.001);
	        assertEquals(2.0f, InvoiceCategory.SUPERMARKET.calculateReimbursement(amount, limit), 0.001);
	    }

	    @Test
	    public void testCalculateReimbursementZero() {
	        float amount = 0.0f;
	        float limit = 20.0f;
	        assertEquals(0.0f, InvoiceCategory.RESTAURANT.calculateReimbursement(amount, limit), 0.001);
	        assertEquals(0.0f, InvoiceCategory.SUPERMARKET.calculateReimbursement(amount, limit), 0.001);
	    }
	    
	    @Test
	    public void testUndedectabeleCategory () {
	    	float amount = 3.0f;
	    	float limit = 2.5f;
	        assertEquals(0.0f, InvoiceCategory.UNDETECTABLE.calculateReimbursement(amount, limit), 0.001);
	    }
}
