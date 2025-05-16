package lunchifyTests;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import backend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import backend.logic.StatisticsService;

class StatisticsServiceTest { //created by AI, changed by the team

	
	private StatisticsService statisticsService;

	    @BeforeEach
	    void setUp() {	    	
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 50.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 30.0f, InvoiceCategory.SUPERMARKET),
	            createReimbursement(LocalDate.of(2024, 5, 5), 20.0f, InvoiceCategory.UNDETECTABLE)
	        );

	        statisticsService = new StatisticsService(testReimbursements);
	    }

	private Reimbursement createReimbursement(LocalDate date, float amount, InvoiceCategory category) {
		// Dummydaten für User
		User user = new User(); // ggf. mit Parametern anpassen

		Invoice invoice = new Invoice(date, amount, category, null, user);
		return new Reimbursement(invoice, amount, new Date(), ReimbursementState.PENDING);
	}


	@Test
	    void testGetSumByCategory() {
	        Map<InvoiceCategory, Double> result = statisticsService.getSumByCategory();

	        assertEquals(50.0, result.get(InvoiceCategory.RESTAURANT));
	        assertEquals(30.0, result.get(InvoiceCategory.SUPERMARKET));
	        assertEquals(20.0, result.get(InvoiceCategory.UNDETECTABLE));
	    }

	    @Test
	    void testGetCountByCategory() {
	        Map<InvoiceCategory, Double> result = statisticsService.getCountByCategory();

	        assertEquals(1.0, result.get(InvoiceCategory.RESTAURANT));
	        assertEquals(1.0, result.get(InvoiceCategory.SUPERMARKET));
	        assertEquals(1.0, result.get(InvoiceCategory.UNDETECTABLE));
	    }

	    @Test
	    void testGetReimbursementsFromLast12Months() {
	        List<Reimbursement> filtered = statisticsService.getReimbursementsFromLast12Months();
	        assertEquals(2, filtered.size()); // Sollte nur die letzten 12 Monate umfassen
	    }
	    
	    @Test
	    void testEmptyReimbursements() {
	        statisticsService = new StatisticsService(List.of());

	        Map<InvoiceCategory, Double> sumByCategory = statisticsService.getSumByCategory();
	        Map<InvoiceCategory, Double> countByCategory = statisticsService.getCountByCategory();

	        assertTrue(sumByCategory.isEmpty());
	        assertTrue(countByCategory.isEmpty());

	        List<Reimbursement> filtered = statisticsService.getReimbursementsFromLast12Months();
	        assertTrue(filtered.isEmpty());
	    }
	    
	    @Test
	    void testGetReimbursementsFromMoreThan12Months() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2023, 3, 10), 50.0f, InvoiceCategory.RESTAURANT), // Mehr als 12 Monate alt
	            createReimbursement(LocalDate.of(2024, 10, 10), 30.0f, InvoiceCategory.SUPERMARKET) // Genau 12 Monate alt
	        );

	        statisticsService = new StatisticsService(testReimbursements);
	        List<Reimbursement> filtered = statisticsService.getReimbursementsFromLast12Months();

	        assertEquals(1, filtered.size()); 
	        assertTrue(filtered.stream().anyMatch(r -> r.getInvoice().getDate().equals(LocalDate.of(2024, 10, 10))));
	    }
	    
	    @Test
	    void testGetSumAndCountByCategoryWithMultipleEntries() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 3.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 3.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2024, 5, 5), 3.0f, InvoiceCategory.RESTAURANT)
	        );

	        statisticsService = new StatisticsService(testReimbursements);
	        
	        Map<InvoiceCategory, Double> sumByCategory = statisticsService.getSumByCategory();
	        Map<InvoiceCategory, Double> countByCategory = statisticsService.getCountByCategory();
	        
	        assertEquals(9.0, sumByCategory.get(InvoiceCategory.RESTAURANT)); 
	        assertEquals(3.0, countByCategory.get(InvoiceCategory.RESTAURANT));
	    }
	    
	    @Test
	    void testSingleUserReimbursement() {
	        User user = new User(); 
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 3.0f, InvoiceCategory.RESTAURANT)
	        );

	        statisticsService = new StatisticsService(testReimbursements);
	        
	        Map<InvoiceCategory, Double> sumByCategory = statisticsService.getSumByCategory();
	        Map<InvoiceCategory, Double> countByCategory = statisticsService.getCountByCategory();

	        assertEquals(3.0, sumByCategory.get(InvoiceCategory.RESTAURANT)); 
	        assertEquals(1.0, countByCategory.get(InvoiceCategory.RESTAURANT));
	    }
	    
	    @Test
	    void testGetTotalAmount() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 3.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 2.5f, InvoiceCategory.SUPERMARKET)
	        );

	        statisticsService = new StatisticsService(testReimbursements);

	        double totalAmount = statisticsService.getSumByCategory().values().stream().mapToDouble(Double::doubleValue).sum();
	        assertEquals(5.5, totalAmount); // Gesamtbetrag sollte 100.0 betragen
	    }
	    
	    @Test
	    void testGetSumBySpecificCategory() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 3.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 3.0f, InvoiceCategory.RESTAURANT)
	        );

	        statisticsService = new StatisticsService(testReimbursements);
	        
	        Map<InvoiceCategory, Double> sumByCategory = statisticsService.getSumByCategory();
	        
	        assertEquals(6.0, sumByCategory.get(InvoiceCategory.RESTAURANT)); // Summe für RESTAURANT sollte 80.0 betragen
	    }
	    
	    @Test
	    void testGetCategoryDistribution() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 50.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 30.0f, InvoiceCategory.SUPERMARKET),
	            createReimbursement(LocalDate.of(2024, 5, 5), 20.0f, InvoiceCategory.RESTAURANT)
	        );

	        statisticsService.setReimbursements(testReimbursements);

	        Map<String, Integer> result = statisticsService.getCategoryDistribution(testReimbursements);

	        assertEquals(2, result.get(InvoiceCategory.RESTAURANT.name()));
	        assertEquals(1, result.get(InvoiceCategory.SUPERMARKET.name()));
	    }
	    
	    @Test
	    void testGetReimbursementSumPerMonthLastYear() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 50.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 30.0f, InvoiceCategory.SUPERMARKET),
	            createReimbursement(LocalDate.of(2024, 5, 5), 20.0f, InvoiceCategory.RESTAURANT)
	        );

	        statisticsService.setReimbursements(testReimbursements);

	        Map<String, Double> result = statisticsService.getReimbursementSumPerMonthLastYear();

	        // Beispielhafte Prüfung für den Monat April 2025
	        assertEquals(50.0, result.get("Apr. 2025"));
	        assertEquals(30.0, result.get("März 2025"));
	    }
	    
	    @Test
	    void testGetInvoiceCountLastYear() {
	        List<Reimbursement> testReimbursements = List.of(
	            createReimbursement(LocalDate.of(2025, 4, 15), 50.0f, InvoiceCategory.RESTAURANT),
	            createReimbursement(LocalDate.of(2025, 3, 10), 30.0f, InvoiceCategory.SUPERMARKET),
	            createReimbursement(LocalDate.of(2024, 5, 5), 20.0f, InvoiceCategory.RESTAURANT)
	        );

	        statisticsService.setReimbursements(testReimbursements);

	        Map<String, Integer> result = statisticsService.getInvoiceCountLastYear();
	        
	        assertNotNull(result.get("Apr. 2025"), "Apr. 2025 should not be null");
	        assertEquals(1, result.get("Apr. 2025"), "Apr. 2025 count should be 1");

	        assertNotNull(result.get("März 2025"), "März 2025 should not be null");
	        assertEquals(1, result.get("März 2025"), "März 2025 count should be 1");

	        assertNull(result.get("Mai 2024"), "Mai 2024 should not be present");
	    }
	    
	    /*
	     * getAverageInvoicesPerUserLastYear is not tested because we would have to mock the connection, the connection provider, the stmt, and more
	     * same for getUserCount()
	     * formatYearMontLabel is private
	     */
}
