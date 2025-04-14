package lunchifyTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import backend.logic.ReimbursementService;
import backend.model.InvoiceCategory;
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
	//TODO Silvia: Add TestCases for modifying ReimbursementAmount
}
