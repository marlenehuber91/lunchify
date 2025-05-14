Praktikum Software Engineering
-----------------------------
# Lunchify

**Lunchify** is a Java-based desktop application developed for internal use at a Linz-based software company. The application enables employees to submit receipts for lunch expenses and request daily reimbursements efficiently. Employees can upload receipts (JPEG, PNG, PDF), classify them, and receive automated reimbursements according to predefined rules. Administrators can manage users, monitor submission statistics, and export monthly data for payroll processing.

Lunchify features secure login with role-based access for employees and administrators, OCR-based receipt analysis, automatic refund calculation, and anomaly detection. The app is built using Java 21 and JavaFX, follows the MVC design pattern, and persists data in a PostgreSQL database.

## Key Features

- **User Authentication**: Email and password-based login with role distinction.
- **Receipt Submission**: Upload and classify lunch receipts, with auto-recognition via OCR.
- **Reimbursement Rules**: Automatic calculation of fixed daily refunds based on receipt type and amount.
- **History and Corrections**: View past submissions, edit or delete until the month's end.
- **Admin Dashboard**: Visual and tabular overviews, data exports (CSV, PDF, JSON/XML), and anomaly detection.
- **Payroll Integration**: Structured data export for monthly salary refunds.
- **Customization**: Admins can adjust reimbursement rates and manage users.

Lunchify is developed in an agile process to continuously incorporate stakeholder feedback and ensure practical, secure, and user-friendly functionality.


## Documentations
- User [Benutzerdokumentation](../main/docs/UserDocumentation.md)
- System [SystemDocumentation](../main/docs/SystemDocumentation.md)

Gr4: Marlene Huber, Johanna Ferstl, Silvia Mahringer  
-----------------------------
## Clockify 
Time is tracked: [clockify](https://app.clockify.me/tracker)

## Prototyping
### our Prototype
Figma: [PrototypeFigma](https://www.figma.com/team_invite/redeem/lGgdMsUEp53sQxzIHljWXv)
### Tools for and   how to Prototyping
Documentation [DokuPrototyping](../main/docs/ToolsForPrototyping.md)



