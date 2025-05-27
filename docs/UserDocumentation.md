# WELCOME to Lunchify

## Inhaltsverzeichnis

- [Einleitung](#einleitung)
- [Installation und Start](#installation-und-start)
- [Anmeldung](#anmeldung)
- [Funktionen für Mitarbeiter:innen](#funktionen-für-mitarbeiterinnen)
- [Funktionen für Administrator:innen](#funktionen-für-administratorinnen)
- [Fehlermeldungen und Hinweise](#fehlermeldungen-und-hinweise)

---

## Einleitung

**Lunchify** ist eine Desktop-Anwendung, mit der Mitarbeiter:innen ihre Rechnungen von Restaurants oder Supermärkten digital einreichen können, um eine tägliche Rückvergütung zu erhalten. Administrator:innen können die Einreichungen verwalten, Statistiken einsehen und Daten für die Gehaltsabrechnung exportieren.

---

## Installation und Start
1. JavaFX Setup: lade JavaFX 21.0.2 herunter: https://jdk.java.net/javafx21/
2. Setze in den Umgebungsvariablen den Pfad auf den lib-Ordner des JavaFX Folders z.B: C:\Program Files\javafx-sdk-21.0.2\lib
3. lade die 'Lunchify.jar' herunter und speichere sie ab
5. Starten die Anwendung durch Doppelklick auf die Datei `Lunchify.jar`.

> ⚠️ Hinweis: Die Datenbankverbindung muss vorher korrekt eingerichtet sein. Dieser Vorgang ist in der [Systemdokumentation](/docs/Systemdocumentation.md) erklärt.

---

## Anmeldung

Beim Start der Anwendung erscheint der Login-Bildschirm:

- Gib deine E-Mail-Adresse und dein Passwort ein.
- Nach erfolgreicher Anmeldung wirst du entsprechend deiner Rolle weitergeleitet.
![LoginPage](/docs/Screenshots/loginPage.png)

---

## Funktionen für Mitarbeiter:innen

### Rechnung hochladen

1. Klicke auf **"Rechnung einreichen"**.
2. Lade ein Bild oder eine PDF deiner Mittagsrechnung hoch.
    - Unterstützte Formate: `.jpg`, `.png`, `.pdf`
![Upload](/docs/Screenshots/RechnungEinreichen.png)

### Rechnung klassifizieren & Betrag eingeben

- Die App erkennt automatisch:
    - Datum
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

---

### Übersicht und Änderungen

- Unter **"alle Rechnungen"** siehst du alle deine bisher eingereichten Rechungen mit Status, Datum, Betrag und Klassifikation.
- Unter **"aktuelle Rechnungen"** siehst du eine Liste deiner Einreichungen dieses Monats mit Status, Datum, Betrag und Klassifikation.
- **offene** Rechnungen können **bis zum Monatsende** bearbeitet oder gelöscht werden. Sobald sie genehmigt wurden, können sie nicht mehr geändert werden.

---

### Statistik

- unter **"Statistik"** siehst du eine Übersicht deiner bisher eingereichten Rechnungen nach verschiedenen Kriterien.
- die Ansicht kann als .pdf abgespeichert werden, nutze den Button rechts
![Statistik](/docs/Screenshots/Statistik.png)

---

### Benachrichtigungen
- ein roter Punkt neben der Glocke links oben zeigt, dass du eine neue Benachrichtigung erhalten hast. Dies ist der Fall wenn eine deiner Rechungen genehmigt, abgelehnt oder geändert wurde.

---

### Logout

- bei Klick auf den Avatar kannst du Logout wählen
![UserDashboard](/docs/Screenshots/UserDashboard.png)
---


## Funktionen für Administrator:innen

- Du hast als Admin alle Funktionen, die auch ein Mitarbeiter hat - siehe oben.   Zusätzliche Funktionen für dich:

![Dashboard](/docs/Screenshots/AdminDashboard.png)

---

### Rechnungen freigeben oder ablehnen

Unter **alle Rechnungen** kannst du Rechnungen freigeben oder ablehnen, löschen oder ändern.
- offene Rechnungen können bis zum Monatsende freigegeben, editiert, gelöscht oder abgelehnt werden
- davor abgelehnte Rechnungen können bis zum Monatsende freigegeben, editiert, gelöscht oder werden
- bereits freigegebene Rechnungen können nicht mehr geändert werden
- Rechnungen "in Prüfung": diese aufen unter der Anomalieerkennung, können sie hier **nicht** freigegeben werden. --> Wechsel zu [Anomalieerkennung](#Anomalieerkennung-und-Auswirkungen)
- ACHTUNG: einmal freigebene Rechnungen können nicht mehr geändert werden.

![alleRechnungen](/docs/Screenshots/AdminAlleRechnungen.png)

### Export für Gehaltsabrechnung

- Klicke auf **"alle Rechnungen"**
- klicke auf **"Lohnverrechnung"** siehe Screenshot rot eingekreist

### Export für Auswertungen

- Klicke auf **"alle Rechnungen"**
- filtere nach Wunsch
- klicke auf **"Exportieren"** siehe Screenshot rot eingekreist

---

### Anomalieerkennung und Auswirkungen

- **Lunchify** erkennt potentiell missbräuchliche Nutzung und flagged User und/oder Rechnungen
- Diese müssen vor Freigabe unter **"Anomalien"** gesondert kontrolliert und dann geändert, gelöscht, freigegeben oder abgelehnt werden
- edit: Rechnung ändern
- del: Rechnung löschen --> User könnte für diesen Tag eine neue Rechnung einreichen
- ok: Rechnung freigeben --> kann nicht rückgängig gemacht werden
- rej: Rechnung ablehnen

![Anomalie](/docs/Screenshots/AdminAnomalie.png)

---

### Übersicht & Statistik

- Klicke auf **"Statistik"**
- Zugriff auf eine tabellarische und grafische Gesamtübersicht:
    - Anzahl Rechnungen pro Monat
    - Durchschnitt pro Benutzer
    - Verteilung Restaurant/Supermarkt
    - Gesamtsumme der Rückvergütungen
![Statistik](/docs/Screenshots/AdminStatistik.png)
- die Ansicht kann als .pdf oder .csv heruntergeladen werden

---

### Suche

- Klicke auf **"Suche"**
- Suche nach bestimmten Kriterien wie zB Benutzer oder Monat

---

### Benutzerverwaltung

- Klicke auf **"Benutzer verwalten"**
- Neue Benutzer:innen hinzufügen oder bestehende löschen
- Rollen zuweisen (Benutzer:in oder Administrator:in)

![Benutzerverwaltung](/docs/Screenshots/AdminUserAnlegen.png)

---

### Konfiguration der Rückerstattung

- Klicke auf **"Rückerstattungsbetrag ändern"**
- Im Admin-Bereich kannst du die Rückerstattungsbeträge anpassen.
- Diese Änderungen gelten ab dem nächsten Antrag.

![Konfiguration](/docs/Screenshots/AdminRueckerstattungAendern.png)

---



## Fehlermeldungen und Hinweise

- **"Login fehlgeschlagen"** → E-Mail oder Passwort falsch
- **"Keine Datenbankverbindung"** → Prüfe deine Internetverbindung und Datenbankeinstellungen
- **"Dateiformat nicht unterstützt"** → Nur `.jpg`, `.png`, `.pdf` erlaubt
<!-- TODO hier müssen wir noch ergänzen -->

---
