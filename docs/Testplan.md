# Lunchify Test Plan

This test plan outlines five core test cases to validate the critical functionality of the Lunchify application. Each test includes a title, description, preconditions, test steps, expected results, and the associated user role.

---

## Test Case 1: User Login with Valid Credentials

**Description:**  
Verify that a registered user can log in using a valid email and password.

**Preconditions:**  
- User is registered in the system with valid credentials.

**Test Steps:**  
1. Open the Lunchify application.  
2. Enter a valid email address.  
3. Enter the corresponding password.  
4. Click the "Login" button.

**Expected Result:**  
- User is successfully logged in and redirected to the main dashboard.

**Role:**  
Employee / Administrator

---

## Test Case 2: Submit a Valid Restaurant Invoice

**Description:**  
Test if a user can upload a valid invoice from a restaurant and receive the correct refund amount.

**Preconditions:**  
- User is logged in.  
- A valid invoice image is available.

**Test Steps:**  
1. Click on "Rechnung einreichen".  
2. Select a restaurant invoice file.  
3. Wait until OCR is reading.  
4. If some Boxes are empty, fill them in manually or correct it, if OCR read incorrect.  
5. Submit the invoice "Rechnung einreichen".

**Expected Result:**  
- Submission confirmation with a refund of €3.00 (fixed rate for restaurants).  
- Receipt is added to the user's history with correct classification.

**Role:**  
Employee

---

## Test Case 3: Admin Views Monthly Reimbursement Summary

**Description:**  
Ensure the admin can access and view monthly statistics and reimbursement totals.

**Preconditions:**   
- At least one receipt has been submitted by employees this month.

**Test Steps:**  
1. Login as Admin.  
2. Navigate to the "Statistik" or "alle Rechnungen" section.

**Expected Result:**  
- Statistik shows the total number of invoices, average per user, and total reimbursement value.

**Role:**  
Administrator

---

## Test Case 4: Edit a Invoice Before End of Month

**Description:**  
Test that a user can edit a submitted invoice within the current month.

**Preconditions:**  
- User has already submitted a invoice in the current month.

**Test Steps:**  
1. Go to "aktuelle Rechungen".  
2. Select a submitted invoice that is NOT "genehmigt".    
3. Change the amount or classification.  
4. Save changes.

**Expected Result:**  
- Invoice is updated successfully.  
- New reimbursement amount is recalculated if applicable.

**Role:**  
Employee

---

## Test Case 5: Detect Anomaly – Mismatched Amount

**Description:**  
Verify that the system detects a mismatch between the entered amount and the OCR-detected amount.

**Preconditions:**  
- A invoice file with a clearly printed date is available.  
- User intentionally enters a different date.

**Test Steps:**  
1. Upload the invoice file.  
2. Enter a mismatching date manually.  
3. Submit the invoice.

**Expected Result:**  
- The system flags the submission as anomalous.  
- Admins can view the anomaly in the dashboard.

**Role:**  
Employee (detected by system for Admin)
