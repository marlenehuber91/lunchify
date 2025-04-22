--> Screenshots still missing

# WELCOME to Lunchify

## Inhaltsverzeichnis

- [Einleitung](#einleitung)
- [Installation und Start](#installation-und-start)
- [Anmeldung](#anmeldung)
- [Funktionen für Mitarbeiter:innen](#funktionen-für-mitarbeiterinnen)
    - [Rechnung hochladen](#rechnung-hochladen)
    - [Rechnung klassifizieren & Betrag eingeben](#rechnung-klassifizieren--betrag-eingeben)
    - [Rückerstattung einreichen](#rückerstattung-einreichen)
    - [Verlauf und Änderungen](#verlauf-und-änderungen)
- [Funktionen für Administrator:innen](#funktionen-für-administratorinnen)
    - [Übersicht & Statistik](#übersicht--statistik)
    - [Benutzerverwaltung](#benutzerverwaltung)
    - [Export für Gehaltsabrechnung](#export-für-gehaltsabrechnung)
    - [Konfiguration der Rückerstattung](#konfiguration-der-rückerstattung)
- [Fehlermeldungen und Hinweise](#fehlermeldungen-und-hinweise)
- [Support](#support)

---

## Einleitung

**Lunchify** ist eine Desktop-Anwendung, mit der Mitarbeiter:innen ihre Rechnungen von Restaurants oder Supermärkten digital einreichen können, um eine tägliche Rückvergütung zu erhalten. Administrator:innen können die Einreichungen verwalten, Statistiken einsehen und Daten für die Gehaltsabrechnung exportieren.

---

## Installation und Start

1. Stelle sicher, dass **Java 21** auf deinem System installiert ist.
//TODO: braucht es das wirklich??
2. Starte die Anwendung durch Doppelklick auf die Datei `Lunchify.jar`.

> ⚠️ Hinweis: Die Datenbankverbindung muss vorher korrekt eingerichtet sein.

---

## Anmeldung

Beim Start der Anwendung erscheint der Login-Bildschirm:

- Gib deine E-Mail-Adresse und dein Passwort ein.
- Nach erfolgreicher Anmeldung wirst du entsprechend deiner Rolle weitergeleitet.

<!-- Screenshot: Login-Bildschirm -->
<!-- ![Login](screenshots/login.png) -->

---

## Funktionen für Mitarbeiter:innen

### Rechnung hochladen

1. Klicke auf **"Rechnung einreichen"**.
2. Lade ein Bild oder eine PDF deiner Mittagsrechnung hoch.
    - Unterstützte Formate: `.jpg`, `.png`, `.pdf`

<!-- Screenshot: Upload-Bereich -->
<!-- ![Upload](screenshots/upload.png) -->

### Rechnung klassifizieren & Betrag eingeben

- Die App erkennt automatisch:
    - ob es sich um ein **Restaurant** oder **Supermarkt** handelt
    - den **Rechnungsbetrag**
- Du kannst diese Angaben, wenn nötig, manuell ändern.

---

### Rückerstattung einreichen

- Klicke auf **"Einreichen"**.
- Du erhältst eine Bestätigung mit dem Rückerstattungsbetrag.

**Beispielregel:**
- Restaurantrechnung 11 € → Rückvergütung: 3 €
- Restaurantrechnung 2,50€ → Rückvergütung: 2,50 €
- Supermarkt 5 € → Rückvergütung: 2,5 €
- Supermarkt 1,70 € → Rückvergütung: 1,70 €

<!-- Screenshot: Bestätigung -->
<!-- ![Bestätigung](screenshots/bestaetigung.png) -->

---

### Übersicht und Änderungen

- Unter **"alle Rechnungen"** siehst du alle deine bisher eingereichten Rechungen mit Status, Datum, Betrag und Klassifikation.
- Unter **"aktuelle Rechnungen"** siehst du eine Liste deiner Einreichungen dieses Monats mit Status, Datum, Betrag und Klassifikation.
- Rechnungen können **bis zum Monatsende** bearbeitet oder gelöscht werden.

<!-- Screenshot: Verlauf -->
<!-- ![Verlauf](screenshots/verlauf.png) -->

---

### Logout

- hier kannst du dich wieder ausloggen
<!-- Screenshot: Logout -->
<!-- ![Logout](screenshots/logout.png) -->

---


## Funktionen für Administrator:innen

- Du hast als Admin alle Funktionen, die auch ein Mitarbeiter hat - siehe oben. Zusätzliche Funktionen für dich:

<!-- Screenshot: Admin-Dashboard -->
<!-- ![Dashboard](screenshots/dashboard.png) -->

---

### Übersicht & Statistik

- Klicke auf **"Statistik"**
- Zugriff auf eine tabellarische und grafische Gesamtübersicht:
    - Anzahl Rechnungen pro Monat
    - Durchschnitt pro Benutzer
    - Verteilung Restaurant/Supermarkt
    - Gesamtsumme der Rückvergütungen

<!-- Screenshot: Statistik -->
<!-- ![Statistik](screenshots/statistik.png) -->

---

### Suche

- Klicke auf **"Suche"**
- Suche nach bestimmten Kriterien wie zB Benutzer oder Monat

<!-- Screenshot: Suche -->
<!-- ![Suche](screenshots/suche.png) -->

---

### Export für Gehaltsabrechnung

- - Klicke auf **"Export"**
- Exportiere Daten monatlich als **CSV**, **PDF**, **JSON** oder **XML**
- Diese Dateien enthalten alle Benutzer:innen mit der jeweiligen Monatssumme

---

### Benutzerverwaltung

- Klicke auf **"Benutzerverwaltung"**
- Neue Benutzer:innen hinzufügen oder bestehende löschen
- Rollen verwalten (Benutzer:in oder Administrator:in)

<!-- Screenshot: Benutzerverwaltung -->
<!-- ![Benutzerverwaltung](screenshots/benutzer.png) -->

---

### Konfiguration der Rückerstattung

- Klicke auf **"Rückerstattungsbetrag ändern"**
- Im Admin-Bereich kannst du die Rückerstattungsbeträge anpassen.
- Diese Änderungen gelten ab dem nächsten Antrag.

<!-- Screenshot: Konfiguration -->
<!-- ![Konfiguration](screenshots/konfiguration.png) -->

---



## Fehlermeldungen und Hinweise

- **"Login fehlgeschlagen"** → E-Mail oder Passwort falsch
- **"Keine Datenbankverbindung"** → Prüfe deine Internetverbindung und Datenbankeinstellungen
- **"Dateiformat nicht unterstützt"** → Nur `.jpg`, `.png`, `.pdf` erlaubt

---
