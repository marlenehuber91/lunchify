Team 4  
Marlene Huber, Johanna Ferstl, Silvia Mahringer  
# Release 3, Milestone 0.3.0

---
## aktueller Stand
### Implementierung inkl. Demo
- release-v0.2.0  13 User Stories umgesetzt, zusätzlich 17 weitere Issues (Tasks, Bug fixes etc.) umgesetzt, Probleme im Testbereich, Sprint 2 abgeschlossen, keine Issues verschoben
- release-v0.3.0  11 User Stories umgesetzt, zusätzlich 31 weitere Issues (Tasks, Bug fixes etc.), keine Verschiebungen, nur noch 1 Feature für letzten Sprint übrig
--> Demo Applikation

### Tests
- Testabdeckung
  - 106 "Backend"-Tests
  - 64 "UI"-Tests
  - bei Klassen die ohne Mocking getestet werden können relativ hohe Testabdeckung > 80 %
  - bei Klassen die nur mit Mocking getestet werden können niedriger --> 20-50 %
  - viele Klassen holen sich Listen aus der Datenbank, zusammengesetzt aus mehreren Objekten --> Filtermethoden könnten nur durch mocken von einer Vielzahl an Elementen getestet werden
  
- welche Sonderfälle werden geprüft: 
  - Sonderfall 1 (OCRServiceTests): Hochladen einer Rechnung, die weder Restaurant noch Supermarkt ist 
  - Sonderfall 2 (UserServiceTests): Test auf inaktive/suspendierte User die einen Login versuchen

### Analyse der Codequalität
- PMD
- Beispiele von Findings:
  - Beispiel 1: AnomalyDetectionService naming_convention_violation: permanent_flag -> Geändert in permanentFlag
  - Beispiel 2: InvoiceService sysout -> Debugging lines mussten entfernt werden
  
### Projektdokus
  - Systemdoku (inkl. Architekturdiagramm)
  - JavaDoc - how to use? [JavadocFile](maven-javadoc-plugin-stale-data.txt)
  - Benutzerdoku (.jar failing)
---

## Sprint Retro
- 82h (360h gesamt) gebuchte Stunden 30.4.-20.5.

1) gröbere Probleme:
   -  Zeitliche Probleme (weniger Zeit aufgrund einer Klausur in einer andern LVA)
   -  Mittlerweile viele Klassen, viel Code tlw. auch Codeverdoppelung -> effizienter wäre gewesen anfangs eine Klassenhierarchie (Abstrakte Klassen, Interfaces und Vererbungsstruktur durchzuplanen --> wobei schwierig im agilen Kontext)
2) Learnings:
   - Vorbereitung: In den Osterferien haben wir bereits etwas vorgearbeitet -> Hat sich bewährt.
   - Projektvorbereitung: Anfangs mehr Zeit in die Projektstruktur investieren. 

## Einsatz von KI
- Testplan, Formatierung/Links im .md
- UnitTests
- .pdf/.xml/.csv Export Vorgehen/Formatierung

## Planung finale Abgabe
- offene issues inkl. deren Priorisierung
