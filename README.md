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
Stundenausmaß: xxx (Marlene %, Johanna %, Silvia %)  
Das Projekt wurde nach agilem Projektmanagement abgearbeitet, angelehnt an Scrum fanden mindestens wöchentlich virtuelle Meetings statt. Der formelle Informationsaustausch wurde via issues in GitHub sichergestellt, der informellere Informationsaustausch in einem Discord-Channel.

# Überblick über die Applikation aus Benutzersicht  
[Benutzerdokumentation](../main/docs/UserDocumentation.md)

# Überblick über die Applikation aus Entwicklersicht  
[SystemDocumentation](../main/docs/SystemDocumentation.md)

## Entwurf

### Überblick über die Applikation
* [UML Diagramm](uml/UML_Release0.3.0) mit Erläuterungen

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

## Implementierung 
> ⚠️ **Achtung:** Dieses Kapitel ist noch unvollständig.
Beschreibung wichtiger Aspekte der Implementierung (eventuell mit ausgewählten 
Codestücken), Projektstruktur, Abhängigkeiten, verwendete Bibliotheken.

## Code Qualität
Die Codequalität wurde bei jedem Merge sowie lokal  vor dem Push in den IDEs der Teammitglieder mit PMD geprüft. Die meisten errors bezogen sich auf Verstöße gegen Namenskonventionen oder die Verwendung von System.out.println() zur Fehlersuche. Alle durch PMD gemeldeten Probleme wurden behoben, indem Variablen- und Methodennamen angepasst und Debug-Ausgaben entfernt wurden.

## Testen
> ⚠️ **Achtung:** Dieses Kapitel ist noch unvollständig.
> Überblick über erstellte JUnit Tests (eventuell mit ausgewählten Tests), Testabdeckung
Beschreibung der Akzeptanztests für 3 ausgewählte Requirements
 
Das Testen stellte im Projekt eine besondere Herausforderung dar, da jedes Teammitglied mit einer eigenen lokalen Datenbank arbeitete. Beim Hochladen von Code auf GitHub wären Tests fehlgeschlagen, da dort keine lokale Datenbank vorhanden ist. Dieses Problem lösten wir durch den Einsatz von Mocking. Allerdings bringt Mocking auch Einschränkungen mit sich – insbesondere lässt sich der Aufruf eines Konstruktors nicht mocken. Da einige unserer Klassen bereits im Konstruktor auf die Datenbank zugreifen, war dies ein Problem.

Unsere Tests gliedern sich in drei Bereiche:  
- Unit-Tests zur Überprüfung einzelner Methoden und Klassen,
- UI-Tests zur Validierung der Benutzeroberfläche,
- ein [Testplan](docs/Testplan.md), der strukturierte manuelle Tests dokumentiert.

# JavaDoc für wichtige Klassen, Interfaces und Methoden
> ⚠️ **Achtung:** Dieses Kapitel ist noch unvollständig.

# Installationsanleitung
1. JavaFX Setup: JavaFX 21.0.2 herunterladen: https://jdk.java.net/javafx21/.
2. in Umgebungsvariablen den Pfad auf den lib-Ordner des JavaFX Folders setzen z.B: C:\Program Files\javafx-sdk-21.0.2\lib.
3. Datei 'lunchify-1.0.0' herunterladen und abspeichern.
4. 'Lunchify.jar' starten.

-------------------------------------------------------------------------------------------------------
## Prototyping
### our Prototype
Figma: [PrototypeFigma](https://www.figma.com/team_invite/redeem/lGgdMsUEp53sQxzIHljWXv)
### Tools for and   how to Prototyping
Documentation [DokuPrototyping](../main/docs/ToolsForPrototyping.md)



