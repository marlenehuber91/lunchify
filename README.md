# Praktikum Software Engineering: Lunchify
[Prototyping.pptx](https://github.com/user-attachments/files/19247546/Prototyping.pptx)


### Verbindung SupaBase:
Verbunden via GitHub mit Silvia
- **Passwort:** !!LunchTeam4
- **ProjectID:** tlvtutujpyclacwydynx
- **Project:** Lunchify

### Verbindungslink
postgresql://postgres:!!LunchTeam4@db.tlvtutujpyclacwydynx.supabase.co:5432/postgres


### Database Script
**Ohne OCR vorerst, das wird zu späterer Zeit noch ergänzt**

CREATE TYPE UserRole AS ENUM ('EMPLOYEE', 'ADMIN');
CREATE TYPE UserState AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');

CREATE TABLE Users (
id SERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
email VARCHAR(255) UNIQUE NOT NULL,
password TEXT NOT NULL,
role UserRole NOT NULL DEFAULT 'EMPLOYEE',
state UserState NOT NULL DEFAULT 'ACTIVE'
);

CREATE TYPE InvoiceState AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE InvoiceCategory AS ENUM ('RESTAURANT', 'SUPERMARKET');

CREATE TABLE Invoices (
id SERIAL PRIMARY KEY,
date DATE NOT NULL,
amount FLOAT NOT NULL,
category InvoiceCategory NOT NULL,
status InvoiceState NOT NULL DEFAULT 'PENDING',
user_id INT NOT NULL,
file BYTEA,
FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE Reimbursements (
id SERIAL PRIMARY KEY,
invoice_id INT UNIQUE NOT NULL,
approved_amount FLOAT NOT NULL,
processed_date DATE NOT NULL,
FOREIGN KEY (invoice_id) REFERENCES Invoices(id) ON DELETE CASCADE
);

CREATE TABLE SystemConfiguration (
id SERIAL PRIMARY KEY,
reimbursement_rates JSONB NOT NULL
);

CREATE TABLE AnomalyDetection (
id SERIAL PRIMARY KEY,
detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
suspicious_invoices JSONB NOT NULL
);

ALTER TABLE Reimbursements
ADD CONSTRAINT fk_reimbursement_invoice FOREIGN KEY (invoice_id) REFERENCES Invoices(id) ON DELETE CASCADE;

ALTER TABLE Invoices
ADD CONSTRAINT fk_invoice_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE;
