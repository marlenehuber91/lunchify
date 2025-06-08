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

/**
 * Service class for generating statistical reports and analytics about reimbursements.
 * Provides various methods to analyze reimbursement data by category, time period, and user metrics.
 */
public class StatisticsService {

	private static ConnectionProvider connectionProvider;

	private List<Reimbursement> reimbursements;

	/**
	 * Constructs an empty StatisticsService.
	 */
	public StatisticsService() {

	}
	/**
	 * Constructs a StatisticsService with a predefined list of reimbursements.
	 *
	 * @param reimbursement Initial list of reimbursements to analyze
	 */
	public StatisticsService(List<Reimbursement> reimbursement) {
		this.reimbursements = reimbursement;
	}

	/**
	 * Sets the connection provider for database operations.
	 *
	 * @param provider The connection provider implementation
	 */
	public static void setConnectionProvider(ConnectionProvider provider) {
		connectionProvider = provider;
	}


	public void setReimbursements(List<Reimbursement> reimb) {
		this.reimbursements = reimb;
	}

	public List<Reimbursement> getReimbursements() {
		return this.reimbursements;
	}

	/**
	 * Calculates the total approved amount by invoice category.
	 *
	 * @return Map with categories as keys and summed amounts as values
	 */
	public Map<InvoiceCategory, Double> getSumByCategory() {
		return reimbursements.stream().filter(r -> r.getInvoice() != null && r.getInvoice().getCategory() != null)
				.collect(Collectors.groupingBy(r -> r.getInvoice().getCategory(),
						Collectors.summingDouble(Reimbursement::getApprovedAmount)));
	}

	/**
	 * Counts reimbursements by invoice category.
	 *
	 * @return Map with categories as keys and count of reimbursements as values
	 */
	public Map<InvoiceCategory, Double> getCountByCategory() {
		return reimbursements.stream().filter(r -> r.getInvoice() != null && r.getInvoice().getCategory() != null)
				.collect(Collectors.groupingBy(r -> r.getInvoice().getCategory(), Collectors.counting())).entrySet()
				.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
	}

	/**
	 * Generates a distribution of reimbursements by category.
	 *
	 * @param reimbursements List of reimbursements to analyze
	 * @return Map with category names as keys and counts as values
	 */
	public Map<String, Integer> getCategoryDistribution(List<Reimbursement> reimbursements) {
		Map<String, Integer> categoryDistribution = new HashMap<>();

		for (Reimbursement reimbursement : reimbursements) {
			String category = reimbursement.getInvoice().getCategory().name();

			categoryDistribution.put(category, categoryDistribution.getOrDefault(category, 0) + 1);
		}

		return categoryDistribution;
	}

	/**
	 * Counts invoices per month for the last 12 months.
	 *
	 * @return Map with month labels (e.g., "May 2025") as keys and invoice counts as values
	 */
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

	/**
	 * Calculates the average number of invoices per user for each of the last 12 months.
	 *
	 * @return Map with month labels as keys and average invoice counts as values
	 */
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

	/**
	 * Calculates the total reimbursement amount per month for the last 12 months.
	 *
	 * @return Map with month labels as keys and total amounts as values
	 */
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

	/**
	 * Gets all reimbursements from the last 12 months.
	 *
	 * @return List of reimbursements from the last year
	 */
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

	/**
	 * Generates a map of the last 12 months with formatted labels.
	 *
	 * @return Map with formatted month labels (e.g., "May 2025") as keys and YearMonth objects as values
	 */
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

	/**
	 * Formats a YearMonth object into a localized short month name with year.
	 *
	 * @param ym The YearMonth to format
	 * @return Formatted string (e.g., "May 2025")
	 */
	private String formatYearMonthLabel(YearMonth ym) {
		return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN) + " " + ym.getYear();
	}

	/**
	 * Gets the total count of users in the system.
	 *
	 * @return Total number of users
	 */
	private long getUserCount() {
		return new UserService().getAllUsers().size();
	}

	/**
	 * Checks if a reimbursement's invoice date falls within a specific month.
	 *
	 * @param r The reimbursement to check
	 * @param ym The YearMonth to check against
	 * @return true if the reimbursement's invoice date is in the specified month, false otherwise
	 */
	private boolean isInMonth(Reimbursement r, YearMonth ym) {
		if (r.getInvoice() == null || r.getInvoice().getDate() == null)
			return false;
		LocalDate date = r.getInvoice().getDate();
		return date.getMonthValue() == ym.getMonthValue() && date.getYear() == ym.getYear();
	}
}
