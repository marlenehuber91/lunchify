# Einleitung

**Lunchify** ist eine Desktop-Anwendung mit Datenbankanbindung, geschrieben in Java. Die Anwendung wurde als Projekt für eine Linzer Firma für internen Gebrauch entwickelt. Mitarbeiter können ihre täglichen Essens-Rechnungen hochladen und bekommen einen bestimmten Betrag über die Lohnverrechnung retourniert. Administratoren kümmern sich um die Freigabe oder Ablehnung der Rückerstattungsanträge, geben die Daten an die Lohnverrechnung weiter und pflegen die Stammdaten.

# Umgesetzte Anforderungen
Es wurden in 3 Sprints alle Anforderungen lt. [Anforderungskatalog](/docs/2025-SS-Anforderungen-Lunchify.pdf) umgesetzt, weitere Anforderungen ergaben sich aus dem Sprint und wurden nach Backlog abgearbeitet.

## Key Features
- **User Authentication**: Email und password-based login, Rollen Admin und Employee umgesetzt.
- **Rechnung hochladen**: Upload and Klassifizierung von Rechnungen, automatisiert durch OCR, editierbar durch User.
- **Berechnung Rückerstattungsbeträge**: Automatische Berechnung von Rückerstattungsbeträgen je nach Klassifizierung und Rechnungsbetrag.
- **Übermittlungsbestätigung**: PopUp Nachricht und Benachrichtigung, wenn ein Antrag erfolgreich eingereicht wurde plus Benachrichtigungen, wenn Änderungen durchgeführt wurden.
- **Verlauf und Korrektur**: Benutzer können eigene Rechnungen anzeigen und innerhalb des Monats ändern oder löschen, Administratoren für alle User. Administratoren können Rechnungen auch freigeben oder ablehnen. Missbräuchliche Verwendung durch User ist abgesichert, eigene Rechnungen können nicht abgeändert werden, keine Abänderung von bereits freigegebenen Rechnungen, keine Freigabe von anomalie-geflaggten Rechnungen durch User selbst möglich, etc.
- **Admin Dashboard, Suchen und Filtern**: Grafische und tabellarische Übersichten, Export der Daten als .pdf und .csv (zweiteres nur für Admins) möglich.
- **Gehaltsabrechnung**: Admins können für die Gehaltsabrechnung nötige Daten als .xml oder .json exportieren.
- **Konfiguration**: Admins können Benutzer hinzufügen/ändern und den Rückerstattungsbetrag ändern.
- **Anomalieerkennung**: Die Software erkennt nach gewissen Regeln potentiell missbräuchlichen Gebrauch.

Die Verantwortlichkeiten im Team wurden fair und nach Bedarf geteilt, größere, komplexere UserStories wurden im PairProgramming bewältigt.
Die Zeit wurde mittels [clockify](https://app.clockify.me/tracker) aufgezeichnet.  
Stundenausmaß: 400 h (Marlene 35%, Johanna 34%, Silvia 31%) Stand 9.6.2025 
Das Projekt wurde nach agilem Projektmanagement abgearbeitet, angelehnt an Scrum fanden mindestens wöchentlich virtuelle Meetings statt. Der formelle Informationsaustausch wurde via issues in GitHub sichergestellt, der informellere Informationsaustausch in einem Discord-Channel.

# Überblick über die Applikation aus Benutzersicht  
[Benutzerdokumentation](../main/docs/UserDocumentation.md)

# Überblick über die Applikation aus Entwicklersicht  
[SystemDocumentation](../main/docs/SystemDocumentation.md)

## Entwurf

### Überblick über die Applikation
* [UML Diagramm](uml/UML_Release0.3.0/Architektur_UML.png)

### Wichtige Design Entscheidungen
Entscheidung: Einsatz von JavaFX für die Benutzeroberfläche  
* Begründung: Moderne UI für Desktop, gute Java-Integration, unterstützt MVC.  
* Alternativen: Swing, Android, Web mit React/Spring Boot.  
* Annahmen: Zielplattform ist Desktop, Team kennt Java, gute Community für JavaFX.  
* Konsequenzen: Gute Desktop-UX, aber nicht mobil einsetzbar ohne großen Mehraufwand.  
  
Entscheidung: JDBC mit lokaler PostgreSQL für Datenpersistenz  
* Begründung: zuverlässige relationale DB, einfache Integration in Java  
* Alternativen: online-DB PostgreSQL.  
* Annahmen: SQL-Kompetenz vorhanden, strukturierte, persistente Daten notwendig.  
* Konsequenzen: Direkte SQL-Kontrolle, manuelles DB-Handling.  
  
Entscheidung: Authentifizierung mit E-Mail und Passwort (BCrypt)  
* Begründung: Vertraut und sicher, schützt vor Passwort-Leaks.  
* Alternativen: Benutzername, andere Verschlüsselungsverfahren.  
* Annahmen: Internes System mit sensiblen Daten.  
* Konsequenzen: Benutzerverwaltung notwendig, sicheres Hashing erforderlich.  
  
Entscheidung: Änderungen von Rechnungen nur bis Monatsende
* Begründung: Schutz der Abrechnungslogik, Manipulation vermeiden.  
* Alternativen: Änderungen jederzeit, 5d nach Monatsende oder mit Admin-Freigabe.  
* Annahmen: Monatliche Gehaltsabrechnung, feste Abgabefrist sinnvoll.  
* Konsequenzen: Klare Bearbeitungsfrist, logische Einschränkung im UI nötig.  

# Implementierung 
## Projektstruktur

Die Projektstruktur orientiert sich an einer typischen mehrschichtigen Architektur mit den folgenden Hauptpaketen:
* backend.logic: Enthält die Geschäftslogik und stellt die Verbindung zwischen Datenbank und UI dar.
* backend.model: Enthält die Datenmodelle. 
* backend.interfaces: Definiert eine Schnittstelle für den Connection Provider.
* backend.configs: Initialisiert die Datenbankverbindung für verschiedene Services zentral.
* backend.exceptions: Enthält benutzerdefinierte Exceptions.
* database: Enthält die Verbindung zur Datenbank.
* frontend: Enthält die Main Klassen und das Controller Package.
* frontend.controller: Enthält alle UI-Komponenten sowie die zugehörige Frontend-Logik.

* Die Quellcode-Dateien sind in einem Maven-Standardverzeichnis src/main/java organisiert.

### Verwendete Bibliotheken und Frameworks
* Tess4J: Für die Texterkennung in Bildern und PDFs.
* JavaFX: Für die Benutzeroberfläche.
* ControlsFX: Zusätzliche JavaFX-Komponenten.
* JFreeChart: Für Diagramme und Charts.
* Log4j: Framework für das Logging. 
* Mindrot JBCrypt: Für das Passwort-Hashing.
* PostgreSQL Treiber: Datenbank.
* Java Servlet API: Für das Session-Management.
* Apache PDFBox: Zur Verarbeitung und Darstellung von PDF-Dateien.
* JDBC: Für den Datenbankzugriff.
* JUnit 5: Für Unit-Tests.
* TestFX: Für UI Tests.
* Mockito: Für Test-Mocking.
* Jackson: Für JSON Verarbeitung. 
* Jakarta XML Bind (JAXB): Für XML-Bindung.

### Maven-Plugins
* Maven Compiler Plugin: Kompilierung der Java-Quellcode-Dateien.
* Maven Shade-Plugin: Erstellung eines Fat-JARs.
* Maven Surefire-Plugin: Steuerung der Testausführung.
* Maven PMD-Plugin: Für die Codeanalyse.
* JavaFX-Maven-Plugin: FÜr die JavaFX-Integration.
* Maven Javadoc-Plugin: Generierung der JavaDoc-Dateien.
* Maven Resources Plugin: Kopieren der Ressourcendateien.

## Wichtige Aspekte der Implementierung
### OCRService
Die Klasse OCRService ermöglicht das Extrahieren von Text aus Bildern und PDF-Dokumenten. Im Falle eines PDFs wird PDFBox verwendet, um Seiten als Bilder zu rendern, bevor Tesseract die Texterkennung übernimmt.

Automatischer Bilderkennung mit Tesseract OCR (Code-Snippet)
```java
private String extractTextFromImage(File imageFile) throws IOException, TesseractException {
    BufferedImage image = ImageIO.read(imageFile);
    if (image == null) {
        throw new IOException("Could not read image file: " + imageFile.getName());
    }
    return tesseract.doOCR(image);
}
```

Automatische PDF Erkennung mit Tesseract OCR (Code-Snippet)
```java
private String extractTextFromPDF(File pdfFile) throws IOException, TesseractException {
    StringBuilder result = new StringBuilder();
    try (PDDocument document = loadPDF(pdfFile)) {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB); // 300 DPI für bessere OCR
            String pageText = tesseract.doOCR(image);
            result.append(pageText).append("\n");
        }
    }
    return result.toString();
}
```
### SessionManager
Die Klasse kümmert sich darum, dass eine Sitzung immer eindeutig einem eingeloggten User zugeordnet wird.

### UserService
Kümmert sich um das Passwort-Hashing und eine sichere Authentifizierung.

### InvoiceService
Bildet die Basis der Applikation indem sie das Hochladen von Bildern und PDFs verarbeitet. Die Klasse nutzt zudem den OCRService. 
Hier werden Anomalien einer Rechnung automatisch erkannt und in der Datenbank gespeichert.

Anomalieerkennung und Logging beim Hinzufügen einer Rechnung (Code-Snippet - unvollstöndiger Auszug aud der Methode addInvoice)
```java
	if (invoice.isFlagged()) {
					detectAnomaliesAndLog(invoice);
					FlaggedUser flaggedUser = detectFlaggedUser(invoice.getUserId());
					flaggedUser.setNoFlaggs(flaggedUser.getNoFlaggs() + 1);
					if (!flaggedUser.isPermanentFlag() && flaggedUser.getNoFlaggs() > 9) {
						flaggedUser.setPermanentFlag(true);
					}
					addOrUpdateFlaggedUser(flaggedUser);
				}
```

## Code Qualität
Die Codequalität wurde bei jedem Merge sowie lokal vor dem Push in den IDEs der Teammitglieder mit PMD geprüft. Die meisten errors bezogen sich auf Verstöße gegen Namenskonventionen oder die Verwendung von System.out.println() zur Fehlersuche. Alle durch PMD gemeldeten Probleme wurden behoben, indem Variablen- und Methodennamen angepasst und Debug-Ausgaben entfernt wurden.

## Testen
Im Rahmen der Qualitätssicherung wurde die Anwendung umfassend getestet. Dabei kamen verschiedene Testarten zum Einsatz, die sich in drei Hauptkategorien gliedern:

Unsere Tests gliedern sich in drei Bereiche:  
- Unit-Tests zur Überprüfung einzelner Methoden und Klassen,
- UI-Tests zur Validierung der Benutzeroberfläche,
- ein [Testplan](docs/Testplan.md), der strukturierte manuelle Tests dokumentiert.

Fokus auf Backend-Tests
Der Schwerpunkt der Teststrategie liegt auf den Unit-Tests im Backend. Diese Tests decken zentrale Funktionalitäten des Systems ab, zum Beispiel:

- Anlegen, Bearbeiten und Löschen von Rückerstattungen
- Filterung und Aggregation von Rückerstattungsdaten
- Validierung von Eingaben und fachlichen Regeln
- Fehlerbehandlung bei ungültigen oder unvollständigen Eingaben
- Absicherung gegen fehlerhafte Zustände in der Datenbank oder fehlschlagende Verbindungen

Dabei wurden gezielt auch Randfälle und potenzielle Fehlerquellen berücksichtigt – etwa ungültige Monatsnamen, null-Werte oder fehlschlagende SQL-Operationen. 

Die Tests wurden nach dem Test-After-Ansatz entwickelt, d. h. nach der Implementierung der jeweiligen Funktionalität. Die Testmethoden basieren überwiegend auf JUnit 5 und verwenden Mockito für das Mocking von Datenbankverbindungen und Abfragen.

Beispielhafte Testfälle:
- Validierung von gültigen Rechnungsbeträgen
```java
@Test
    public void testIsValidFloat() {
        assertTrue(invoiceService.isValidFloat("123.45"));
        assertTrue(invoiceService.isValidFloat("100"));
        assertFalse(invoiceService.isValidFloat("12,34"));
        assertFalse(invoiceService.isValidFloat("abc"));
        assertFalse(invoiceService.isValidFloat("-1"));
    }
```
- Authentifikation eines gültigen Nutzers unter Verwendung von Mockito
```java
 @Test
    public void authenticateValidUser() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getString("password"))
                .thenReturn(BCrypt.hashpw("sarah123", BCrypt.gensalt()));
        when(mockResultSet.getString("role")).thenReturn("EMPLOYEE");
        when(mockResultSet.getString("name")).thenReturn("Sarah Maier");
        when(mockResultSet.getInt("id")).thenReturn(1);

        User user = UserService.authenticate("sarah.maier@lunch.at", "sarah123");

        assertNotNull(user);
        assertEquals("Sarah Maier", user.getName());
        assertEquals(UserRole.EMPLOYEE, user.getRole());
        assertEquals(UserState.ACTIVE, user.getState());
    }
```
Die im Rahmen der User Stories definierten Akzeptanzkriterien wurden im Testprozess systematisch überprüft.

Beispielhafte Akzeptanzkriterien für die Suche von User:

 - Search for a user by name → System returns correct results.
 - Search for user by email -> System returns correct result.
 - Search for nonexisting user -> System displays error message
 - Login as user and try to find the search button -> there should be no possibility on the user-dashboard

Beispielhafte Akzeptanzkriterien für die Übersicht aller Rückerstattungsanträge:
 - View past invoices → A correct list of past invoices is displayed.
 - Filter by month → Only the selected month's data is shown.
 - Check total reimbursement → Total sum is correctly calculated.

Beispielhafte Akzeptanzkriterien für die automatische Erkennung von Rechnungsdetails:
- Upload a clear invoice image → OCR extracts correct amount and category.
- Upload a blurry invoice image → System prompts user to enter details manually.
- Upload a supermarket invoice → System correctly classifies it as "Supermarkt".
- Upload a restaurant invoice → System correctly classifies it as "Restaurant".
- Modify automatically extracted details → System allows correction before submission.
 
Das Testen stellte im Projekt eine besondere Herausforderung dar, da jedes Teammitglied mit einer eigenen lokalen Datenbank arbeitete. Beim Hochladen von Code auf GitHub wären Tests fehlgeschlagen, da dort keine lokale Datenbank vorhanden ist. Dieses Problem lösten wir durch den Einsatz von Mocking. Allerdings bringt Mocking auch Einschränkungen mit sich – insbesondere lässt sich der Aufruf eines Konstruktors nicht mocken. Da einige unserer Klassen bereits im Konstruktor auf die Datenbank zugreifen, war dies ein Problem.


# JavaDoc für wichtige Klassen, Interfaces und Methoden
> ## 📄 JavaDoc Service Klassen (Logik)
- [AnomalyDetectionService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/AnomalyDetectionService.html)
- [ExportService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/ExportService.html)
- [FlaggedUserService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/FlaggedUserService.html)
- [InvoiceService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/InvoiceService.html)
- [LocalDateAdapter](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/LocalDateAdapter.html)
- [NotificationService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/NotificationService.html)
- [OCRService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/OCRService.html)
- [ReimbursementService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/ReimbursementService.html)
- [SearchService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/SearchService.html)
- [SessionManager](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/SessionManager.html)
- [StatisticsService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/StatisticsService.html)
- [UserService](https://jku-win-se.github.io/teaching-2025.ss.prse.braeuer.team4/JavaDoc/backend/logic/UserService.html)

# Installationsanleitung
1. JavaFX Setup: JavaFX 21.0.2 herunterladen: https://jdk.java.net/javafx21/.
2. In Umgebungsvariablen den Pfad auf den lib-Ordner des JavaFX Folders setzen z.B: C:\Program Files\javafx-sdk-21.0.2\lib.
3. OCR (Tesseract) herunterladen. Wähle dazu die ZIP Datei aus folgendem GitHub Release: https://github.com/nguyenq/tess4j/releases/tag/tess4j-5.15.0
4. - OCR Trainingsdaten als ZIP-Datei aus GitHub Release ⚠️(GITHUB LINK ZU RELEASE 1.0.0 einfügen) herunterladen.
   - Ordner "tessdata" aus dieser ZIP-Datei (liegt in src/main/resources) kopieren.
5. Ordner "Lunchify" auf lokalem PC erstellen und Ordner "tessdata" hineinkopieren.
6. Keinesfalls den Namen des Ordners tessdata oder seinen Inhalt verändern. 
7. Executabel lunchify-1.0.0.jar herunterladen ⚠️LINK ZU RELEASE 1.0.0 einfügen und im lokalen Ordner "Lunchify" ablegen.
8. Das .jar und der Ordner tessdata befinden sich nun auf selber Ebene.
9. lunchify-1.0.0.jar aus dem Ordner Lunchify heraus starten. 

-------------------------------------------------------------------------------------------------------
## Prototyping
### our Prototype
Figma: [PrototypeFigma](https://www.figma.com/team_invite/redeem/lGgdMsUEp53sQxzIHljWXv)
### Tools for and   how to Prototyping
Documentation [DokuPrototyping](../main/docs/ToolsForPrototyping.md)



