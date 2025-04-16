```sql

DROP TABLE AnomalyDetection;
DROP TABLE SystemConfiguration;
DROP TABLE Reimbursements;
DROP TABLE Invoices;
DROP TABLE Users;
DROP TABLE ReimbursementAmount;
DROP TYPE ReimbursementState;
DROP TYPE InvoiceCategory;
DROP TYPE UserState;
DROP TYPE UserRole;

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

CREATE TYPE ReimbursementState AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED');
CREATE TYPE InvoiceCategory AS ENUM ('RESTAURANT', 'SUPERMARKET');

CREATE TABLE Invoices (
id SERIAL PRIMARY KEY,
date DATE NOT NULL,
amount FLOAT NOT NULL,
category InvoiceCategory NOT NULL,
user_id INT NOT NULL,
file BYTEA,
flag BOOLEAN,
text TEXT,
FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE Reimbursements (
id SERIAL PRIMARY KEY,
invoice_id INT UNIQUE NOT NULL,
approved_amount FLOAT NOT NULL,
processed_date DATE NOT NULL,
status ReimbursementState NOT NULL DEFAULT 'PENDING',
FOREIGN KEY (invoice_id) REFERENCES Invoices(id) ON DELETE CASCADE
);

CREATE TABLE AnomalyDetection (
id SERIAL PRIMARY KEY,
detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
suspicious_invoices JSONB NOT NULL
);

CREATE TABLE ReimbursementAmount (
id SERIAL PRIMARY KEY,
category InvoiceCategory NOT NULL,
amount FLOAT NOT NULL
);

ALTER TABLE Reimbursements
ADD CONSTRAINT fk_reimbursement_invoice FOREIGN KEY (invoice_id) REFERENCES Invoices(id) ON DELETE CASCADE;

ALTER TABLE Invoices
ADD CONSTRAINT fk_invoice_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE;

-- Insert Statements for table users
INSERT INTO users (name, email, password, role, state) VALUES ('Ali Baba', 'ali.baba@lunch.at', '$2a$12$/8Ef1TarZReYVFyrzUWe1.LA8h1H2EDkbVUNW/Qn735QxnbljCRNW', 'EMPLOYEE', 'SUSPENDED');
INSERT INTO users (name, email, password, role, state) VALUES ('Barbara Hummer', 'barbara.hummer@lunch.at', '$2a$12$9D.SowePxYOokt1/4t.40.PI/8rbVjHWK.EY31m/X7dx7VXZDz4ta', 'EMPLOYEE', 'INACTIVE');
INSERT INTO users (name, email, password, role, state) VALUES ('Martin Lechner', 'martin.lechner@lunch.at', '$2a$12$Qzcsnm8KYIIwPrgUCa25L.l0/p9fUxWNc8s1C23NxI2h0daNDITt6', 'ADMIN', 'ACTIVE');
INSERT INTO users (name, email, password, role, state) VALUES ('Sarah Maier', 'sarah.maier@lunch.at', '$2a$12$iYEddmZggCRh5bTiPAw5pevD5uN3HAWrKNqCMmyoQQMUcnxNo3vQ2', 'EMPLOYEE', 'ACTIVE');
INSERT INTO users (name, email, password, role, state) VALUES ('e (testEmployee)', 'e@lunch.at', '$2a$12$K.H4vlY15VhZ08TYvQHwb.AwE.K/WfQkoHGshF/ig6pqi0wMmqasW', 'EMPLOYEE', 'ACTIVE');
INSERT INTO users (name, email, password, role, state) VALUES ('a (testAdmin)', 'a@lunch.at', '$2a$12$Xzzl7nc6PKRxVbpd4nRLleuwcSCgtcQIRvSKkRTKzVPm.Iy9bbpqG', 'ADMIN', 'ACTIVE');

--Insert Statements for initial ReimbursementAmount
INSERT INTO ReimbursementAmount (category, amount)
VALUES
('SUPERMARKET', 2.5),
('RESTAURANT', 3);
