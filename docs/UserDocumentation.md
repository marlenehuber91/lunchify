# WELCOME to Lunchify

## Inhaltsverzeichnis

- [Einleitung](#einleitung)
- [Installation und Start](#installation-und-start)
- [Anmeldung](#anmeldung)
- [Funktionen für Mitarbeiter:innen](#funktionen-für-mitarbeiterinnen)
- [Funktionen für Administrator:innen](#funktionen-für-administratorinnen)

---

## Einleitung

**Lunchify** ist eine Desktop-Anwendung, mit der Mitarbeiter:innen ihre Rechnungen von Restaurants oder Supermärkten digital einreichen können, um eine tägliche Rückvergütung zu erhalten. Administrator:innen können die Einreichungen verwalten, Statistiken einsehen und Daten für die Gehaltsabrechnung exportieren.

---

## Datenbank 
Vor der ersten Anmeldung muss eine Datenbank für die Anwendung erreichbar sein, die Daten sind hard-coded:
```java
    private static final String URL="jdbc:postgresql://localhost:5432/postgres";
    private static final String USER="postgres";
    private static final String PASSWORD="!!Lunch4";
```
Es muss somit sichergestellt sein dass die Datenbank "postgres" heißt, auf 5432 erreichbar ist und das UserPasswort !!Lunch4 ist.  
Unter [Datenbankskript](Database.md) ist das Skript zu finden, das für den erst-Start ausgeführt werden muss, damit die Anwendung funktionieren kann.

## Installation und Start
1. JavaFX Setup: JavaFX 21.0.2 [hier](https://jdk.java.net/javafx21/) herunterladen.
2. In Umgebungsvariablen den Pfad auf den lib-Ordner des JavaFX Folders setzen z.B: C:\Program Files\javafx-sdk-21.0.2\lib.
3. OCR (Tesseract) herunterladen. Wähle dazu die ZIP Datei aus folgendem [GitHub Release](https://github.com/nguyenq/tess4j/releases/tag/tess4j-5.15.0)
4. - Lunchify ZIP-Datei aus dem Release aktuellen Release v.1.0.1 herunterladen.
   - Einen Ordner Lunchify anlegen. 
   - Ordner "tessdata" aus dieser ZIP-Datei (liegt in src/main/resources) kopieren.
   - Keinesfalls den Namen des Ordners tessdata oder seinen Inhalt verändern. 
5. Executable lunchify-1.0.0.jar aus dem aktuellen Release v1.0.1 herunterladen und ebenfalls im Ordner Lunchify ablegen.
6. Das .jar und der Ordner tessdata befinden sich nun auf selber Ebene.
7. lunchify-1.0.0.jar aus dem Ordner Lunchify heraus starten. 
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

- Rechnungen von Usern, die als auffällig gekennzeichnet sind, werden ausnahmslos "zur Prüfung" unter Anomalien angeführt. Admins können User wieder manuell als nicht auffällig kennzeichnen. Dazu dient der Button **"auffällige User"**.

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
- Neue Benutzer:innen hinzufügen oder aktive auf inaktiv setzen bzw. inaktive auf aktiv
- Rollen zuweisen (Benutzer:in oder Administrator:in)

![Benutzerverwaltung](/docs/Screenshots/AdminUserAnlegen.png)

---

### Konfiguration der Rückerstattung

- Klicke auf **"Rückerstattungsbetrag ändern"**
- Im Admin-Bereich kannst du die Rückerstattungsbeträge anpassen.
- Diese Änderungen gelten ab dem nächsten Antrag.

![Konfiguration](/docs/Screenshots/AdminRueckerstattungAendern.png)
---
