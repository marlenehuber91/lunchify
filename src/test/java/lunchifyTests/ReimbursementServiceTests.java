package lunchifyTests;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.logic.ReimbursementService;
import backend.logic.UserService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import database.DatabaseConnection;

@ExtendWith(MockitoExtension.class)
public class ReimbursementServiceTests {
	
	@Mock
	private Connection mockConnection;
	
	@Mock
	private PreparedStatement mockStatement;
	
	@Mock
	private ResultSet mockResultSet;
	
	ReimbursementService service;
	User mockUser;
	
	@BeforeEach
	public void setUp() throws Exception {
		lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
	    lenient().when(mockStatement.executeQuery()).thenReturn(mockResultSet);
	    lenient().when(mockStatement.executeUpdate()).thenReturn(1);
	    lenient().when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);

	    UserService.setConnectionProvider(() -> mockConnection);

	    mockUser = new User(1, "mockUser", "mock@mail", UserRole.ADMIN, UserState.ACTIVE);
	    service = new ReimbursementService(mockUser);
	}
	

	@Test
	public void testDefaultRestaurantLimit() {
		assertEquals(3.0f, service.getLimit(InvoiceCategory.RESTAURANT), 0.001);
	}
	
	@Test
	public void testDefaultSupermarketLimit() {
		assertEquals(2.5f, service.getLimit(InvoiceCategory.SUPERMARKET), 0.001);
	}
	
	@Test
	public void testGetTotalReimbursement() {
		Invoice invoice = new Invoice();
		invoice.setDate(LocalDate.now());
		
		Reimbursement r1 = new Reimbursement();
		r1.setApprovedAmount(2.5f);
		r1.setInvoice(invoice);
		
		Reimbursement r2 = new Reimbursement();
		r2.setApprovedAmount(3f);
		r2.setInvoice(invoice);
		
		Reimbursement r3 = new Reimbursement();
		r3.setApprovedAmount(2.5f);
		r3.setStatus(ReimbursementState.REJECTED); //Rejected should not be counted
		r3.setInvoice(invoice);
		
		List<Reimbursement> list = new ArrayList();
		list.add(r1);
		list.add(r2);
		list.add(r3);
		float total = service.getTotalReimbursement(list);
		
		assertEquals(5.5f, total, 0.001);
		
	}
	
	@Test
	public void testGetTotalReimbursement_FilteredByState () {
		List<Reimbursement> list = new ArrayList<>();
		
		Reimbursement r1 = new Reimbursement();
		r1.setApprovedAmount(3f);
		r1.setStatus(ReimbursementState.PENDING);
		
		Reimbursement r2 = new Reimbursement();
		r2.setApprovedAmount(2.5f);
		r2.setStatus(ReimbursementState.PENDING);
		
		list.add(r1);
		list.add(r2);
		
		float total = service.getTotalReimbursement(list, ReimbursementState.PENDING);
		assertEquals(5.5f, total, 0.001f);		
	}
	
	@Test
	public void testConvertMonthToNumberValid() {
		assertEquals("1", service.convertMonthToNumber("Jänner"));
		assertEquals("12", service.convertMonthToNumber("Dezember"));
	}
	
	@Test
	public void testConverMonthToNumberAlle() {
		assertNull(service.convertMonthToNumber("alle"));
	}
	
	@Test
	public void testConvertMonthToNumberInvalid () {
	    assertThrows(IllegalArgumentException.class, () -> {
	        service.convertMonthToNumber("Fakemonth");
	    });
	}
	//TODO Silvia: Add TestCases for modifying ReimbursementAmount
}
