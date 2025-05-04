package backend.logic;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

	public StatisticsService(List<Reimbursement> reimbursement) {
		this.reimbursements = reimbursement;
	}

	public static void setConnectionProvider(ConnectionProvider provider) {
		connectionProvider = provider;
	}

	public void setReimbursements(List<Reimbursement> reimb) {
		this.reimbursements = reimb;
	}

	public List<Reimbursement> getReimbursements() {
		return this.reimbursements;
	}

	public Map<InvoiceCategory, Double> getSumByCategory() {
		return reimbursements.stream().filter(r -> r.getInvoice() != null && r.getInvoice().getCategory() != null)
				.collect(Collectors.groupingBy(r -> r.getInvoice().getCategory(),
						Collectors.summingDouble(Reimbursement::getApprovedAmount)));
	}

	public Map<InvoiceCategory, Double> getCountByCategory() {
		return reimbursements.stream().filter(r -> r.getInvoice() != null && r.getInvoice().getCategory() != null)
				.collect(Collectors.groupingBy(r -> r.getInvoice().getCategory(), Collectors.counting())).entrySet()
				.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
	}


	public Map<String, Integer> getCategoryDistribution(List<Reimbursement> reimbursements) {
		Map<String, Integer> categoryDistribution = new HashMap<>();

		for (Reimbursement reimbursement : reimbursements) {
			String category = reimbursement.getInvoice().getCategory().name();

			categoryDistribution.put(category, categoryDistribution.getOrDefault(category, 0) + 1);
		}

		return categoryDistribution;
	}

	public Map<String, Integer> getInvoiceCountLastYear() {
		Map<String, Integer> invoiceCountPerMonth = new LinkedHashMap<>();
		Map<String, YearMonth> last12Months = generateLast12MonthsMap();

		for (Map.Entry<String, YearMonth> entry : last12Months.entrySet()) {
			String label = entry.getKey();
			YearMonth ym = entry.getValue();

			long count = reimbursements.stream().filter(r -> isInMonth(r, ym)).count();

			invoiceCountPerMonth.put(label, (int) count);
		}

		return invoiceCountPerMonth;
	}

	public Map<String, Double> getAverageInvoicesPerUserLastYear() {
		Map<String, Double> result = new LinkedHashMap<>();
		LocalDate now = LocalDate.now();

		UserService userService = new UserService();

		List<User> allUsers = userService.getAllUsers();

		for (int i = 11; i >= 0; i--) {
			LocalDate month = now.minusMonths(i);
			String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN) + " " + month.getYear();

			List<Reimbursement> monthly = reimbursements.stream().filter(r -> {
				if (r.getInvoice() == null || r.getInvoice().getDate() == null)
					return false;
				LocalDate date = r.getInvoice().getDate();
				return date.getMonthValue() == month.getMonthValue() && date.getYear() == month.getYear();
			}).toList();
			long activeUsersInMonth = allUsers.stream().count();
			double avg = activeUsersInMonth == 0 ? 0.0 : (double) monthly.size() / activeUsersInMonth;

			result.put(label, avg);
		}

		return result;
	}

	public Map<String, Double> getReimbursementSumPerMonthLastYear() {
		Map<String, Double> monthlySums = new LinkedHashMap<>();
		Map<String, YearMonth> last12Months = generateLast12MonthsMap();

		for (Map.Entry<String, YearMonth> entry : last12Months.entrySet()) {
			String label = entry.getKey();
			YearMonth ym = entry.getValue();

			double sum = reimbursements.stream().filter(r -> isInMonth(r, ym))
					.mapToDouble(Reimbursement::getApprovedAmount).sum();

			monthlySums.put(label, sum);
		}

		return monthlySums;
	}

	public List<Reimbursement> getReimbursementsFromLast12Months() {
		LocalDate now = LocalDate.now();
		LocalDate startDate = now.minusMonths(11).withDayOfMonth(1); // erster Tag des Monats vor 11 Monaten

		List<Reimbursement> list = reimbursements.stream().filter(r -> {
			if (r.getInvoice() == null || r.getInvoice().getDate() == null)
				return false;
			LocalDate invoiceDate = r.getInvoice().getDate();
			return !invoiceDate.isBefore(startDate) && !invoiceDate.isAfter(now);
		}).collect(Collectors.toList());
		return list;
	}

	public Map<String, YearMonth> generateLast12MonthsMap() {
		Map<String, YearMonth> last12Months = new LinkedHashMap<>();
		YearMonth current = YearMonth.now();

		for (int i = 11; i >= 0; i--) {
			YearMonth month = current.minusMonths(i);
			String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN) + " " + month.getYear();
			last12Months.put(label, month);
		}

		return last12Months;
	}

	private String formatYearMonthLabel(YearMonth ym) {
		return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN) + " " + ym.getYear();
	}

	private long getUserCount() {
		return new UserService().getAllUsers().size();
	}

	private boolean isInMonth(Reimbursement r, YearMonth ym) {
		if (r.getInvoice() == null || r.getInvoice().getDate() == null)
			return false;
		LocalDate date = r.getInvoice().getDate();
		return date.getMonthValue() == ym.getMonthValue() && date.getYear() == ym.getYear();
	}
}
