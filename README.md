# Einleitung

**Lunchify** ist eine Desktop-Anwendung mit Datenbankanbindung, geschrieben in Java. Die Anwendung wurde als Pojekt für eine Linzer Firma für internen Gebrauch geschrieben. Mitarbeiter können ihre täglichen Essens-Rechnungen hochladen und bekommen einen bestimmten Betrag retourniert. Administratoren kümmern sich um die Freigabe oder Ablehnung der Rückerstattungsanträge, geben die Daten an die Lohnverrechnung weiter und pflegen die Stammdaten.

# Umgesetzte Anforderungen
Es wurden in 3 Sprints alle Anforderungen lt. [Anforderungskatalog](../docs/2025-SS-Anforderungen Lunchify.pdf) umgesetzt.

## Key Features
- **User Authentication**: Email and password-based login with role distinction.
- **Rechnung hochladen**: Upload and Klassifizierung von Rechnungen, automatisiert durch OCR.
- **Berechnung Rückerstattungsbeträge**: Automatische Berechnung von Rückerstattungsbeträgen nach Klassifizierung und Rechnungsbetrag.
- **Übermittlungsbestätigung**: PopUp Nachricht und Benachrichtigung, wenn ein Antrag erfolgreich eingereicht wurde plus Benachrichtigungen, wenn Änderungen durchgeführt wurden.
- **Verlauf und Korrektur**: Benutzer können eigene Rechnungen anzeigen und innerhalb des Monats ändern oder löschen, Administratoren für alle User. Administratoren können Rechnungen auch freigeben oder ablehnen.
- **Admin Dashboard, Suchen und Filtern**: Es gibt visuelle und Tabellarische Übersichten, Export der Daten .pdf ist möglich. Administratoren können zusätlich .csv Dateien erstellen.
- **Gehaltsabrechnung**: Administratoren können für die Gehaltsabrechnung nötige Daten als .xml oder .json exportieren.
- **Konfiguration**: Administratoren können Benutzer hinzufügen/ändern und den Rückerstattungsbetrag ändern.
- **Anomalieerkennung**: Die Software erkennt nach gewissen Regeln potentiell missbräuchlichen Gebrauch.

Die Verantwortlichkeiten im Team wurden fair und nach Bedarf geteilt, größere, komplexere UserStories wurden im PairProgramming bewältigt.
Die Zeit wurde mittels [clockify](https://app.clockify.me/tracker) aufgezeichnet.
Stundenausmaß: xxx

# Überblick über die Applikation aus Benutzersicht  
[Benutzerdokumentation](../main/docs/UserDocumentation.md)

# Überblick über die Applikation aus Entwicklersicht  
System [SystemDocumentation](../main/docs/SystemDocumentation.md)

## Entwurf

### Überblick über die Applikation
* [UML Diagramm](uml/UML_Release0.3.0) mit Erläuterungen
* Verwendete Design Muster (z.B. Model-View-Controller)

### Wichtige Design Entscheidungen
* Beschreibung der 3-5 wichtigsten Design Entscheidungen nach folgenden Schema
    * Entscheidung:
    * Begründung: 
    * Alternativen, die in Betracht gezogen wurden:
    * Annahmen: 
    * Konsequenzen:

## Implementierung
Beschreibung wichtiger Aspekte der Implementierung (eventuell mit ausgewählten 
Codestücken), Projektstruktur, Abhängigkeiten, verwendete Bibliotheken.

## Code Qualität
Beschreibung der Verwendung von PMD, Beschreibung der Findings und welche davon 
behoben wurden.

## Testen
Überblick über erstellte JUnit Tests (eventuell mit ausgewählten Tests), Testabdeckung
Beschreibung der Akzeptanztests für 3 ausgewählte Requirements.

# JavaDoc für wichtige Klassen, Interfaces und Methoden

# Installationsanleitung
//ODER LINK? @Silvia: Installation habe ich auch in der UserDoc angepasst, kann auch gerne verlinkt werden! 
1. JavaFX Setup: laden Sie sich JavaFX 21.0.2 herunter: https://jdk.java.net/javafx21/
2. Setzen Sie in den Umgebungsvariablen den Pfad auf den lib-Ordner des JavaFX Folders z.B: C:\Program Files\javafx-sdk-21.0.2\lib

-------------------------------------------------------------------------------------------------------
## Prototyping
### our Prototype
Figma: [PrototypeFigma](https://www.figma.com/team_invite/redeem/lGgdMsUEp53sQxzIHljWXv)
### Tools for and   how to Prototyping
Documentation [DokuPrototyping](../main/docs/ToolsForPrototyping.md)



