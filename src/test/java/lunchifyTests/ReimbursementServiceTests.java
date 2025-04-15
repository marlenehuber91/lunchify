package lunchifyTests;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import backend.logic.ReimbursementService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

public class ReimbursementServiceTests {
	
	private ReimbursementService service;
    private User user;

	@Before
	public void setUp(){
		user = new User(999, "mockUser", "mock@lunch.at", UserRole.EMPLOYEE, UserState.ACTIVE);
		service = new ReimbursementService(user);
	}

	@Test
	public void testDefaultRestaurantLimit() {
		assertEquals(3.0f, service.getLimit(InvoiceCategory.RESTAURANT), 0.001);
	}
	
	@Test
	public void testDefaultSupermarketLimit() {
		assertEquals(2.5f, service.getLimit(InvoiceCategory.SUPERMARKET), 0.001);
	}
	
	@Test(expected = IllegalArgumentException.class)
    public void testModifyLimitNegativeValueThrowsException() {
        service.modifyLimits(InvoiceCategory.SUPERMARKET, -1.0f);
    }
	
	@Test
	public void testGetTotalReimbursement() {
		Invoice invoice = new Invoice();
		invoice.setDate(LocalDate.now());
		
		Reimbursement r1 = new Reimbursement();
		r1.setApprovedAmount(10.5f);
		r1.setInvoice(invoice);
		
		Reimbursement r2 = new Reimbursement();
		r2.setApprovedAmount(4.5f);
		r2.setInvoice(invoice);
		
		List<Reimbursement> list = new ArrayList();
		list.add(r1);
		list.add(r2);
		float total = service.getTotalReimbursement(list);
		
		assertEquals(15.0f, total, 0.001);
		
	}
	//TODO Silvia: Add TestCases for modifying ReimbursementAmount
}
