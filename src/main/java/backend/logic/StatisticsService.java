package backend.logic;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import backend.interfaces.ConnectionProvider;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.User;

public class StatisticsService {

	private static ConnectionProvider connectionProvider;

	private List<Reimbursement> reimbursements;

	public StatisticsService() {

	}

	public StatisticsService(List<Reimbursement> reimbursements) {
		this.reimbursements = reimbursements;
	}

	public static void setConnectionProvider(ConnectionProvider provider) {
		connectionProvider = provider;
	}

	/*
	 * calculates sum of all reimbursements per category created by AI
	 */

	public Map<InvoiceCategory, Double> getSumByCategory() {
		return reimbursements.stream().collect(Collectors.groupingBy(r -> r.getInvoice().getCategory(),
				Collectors.summingDouble(r -> (double) r.getApprovedAmount())));
	}

	public Map<String, Integer> getInvoiceCountPerMonth(List<Reimbursement> reimbursements) {
		Map<String, Integer> invoiceCountPerMonth = new HashMap<>();

		for (Reimbursement reimbursement : reimbursements) {
			LocalDate invoiceDate = reimbursement.getInvoice().getDate();
			String monthYear = invoiceDate.getMonth() + " " + invoiceDate.getYear(); // Format: "Month Year"

			invoiceCountPerMonth.put(monthYear, invoiceCountPerMonth.getOrDefault(monthYear, 0) + 1);
		}

		return invoiceCountPerMonth;
	}

	public Map<String, Double> getAverageInvoicesPerUserPerMonth(List<Reimbursement> reimbursements) {
		Map<String, Integer> userMonthCount = new HashMap<>();
		Map<String, Integer> userMonthTotalInvoices = new HashMap<>();

		for (Reimbursement reimbursement : reimbursements) {
			User user = reimbursement.getInvoice().getUser();
			LocalDate invoiceDate = reimbursement.getInvoice().getDate();
			String userMonth = user.getId() + "-" + invoiceDate.getMonth() + "-" + invoiceDate.getYear(); // Format:
																											// "UserId-Month-Year"

			userMonthCount.put(userMonth, userMonthCount.getOrDefault(userMonth, 0) + 1);
			userMonthTotalInvoices.put(userMonth, userMonthTotalInvoices.getOrDefault(userMonth, 0) + 1);
		}

		Map<String, Double> averageInvoicesPerUserPerMonth = new HashMap<>();
		for (String userMonth : userMonthCount.keySet()) {
			double average = (double) userMonthTotalInvoices.get(userMonth) / userMonthCount.get(userMonth);
			averageInvoicesPerUserPerMonth.put(userMonth, average);
		}

		return averageInvoicesPerUserPerMonth;
	}

	public Map<String, Integer> getCategoryDistribution(List<Reimbursement> reimbursements) {
		Map<String, Integer> categoryDistribution = new HashMap<>();

		for (Reimbursement reimbursement : reimbursements) {
			String category = reimbursement.getInvoice().getCategory().name();

			categoryDistribution.put(category, categoryDistribution.getOrDefault(category, 0) + 1);
		}

		return categoryDistribution;
	}

	public void setReimbursements(List<Reimbursement> reimb) {
		this.reimbursements = reimb;
	}
}
