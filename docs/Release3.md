Team 4  
Marlene Huber, Johanna Ferstl, Silvia Mahringer  
# Release 3, Milestone 0.3.0

---
## aktueller Stand
### Implementierung inkl. Demo
- release-v0.2.0  13 User Stories umgesetzt, zusätzlich 17 weitere Issues (Tasks, Bug fixes etc.) umgesetzt, Probleme im Testbereich, Sprint 2 abgeschlossen, keine Issues verschoben
- release-v0.3.0  
--> Demo Applikation

### Tests
- Testabdeckung
- welche Sonderfälle werden geprüft: 
  - Sonderfall 1 (OCRServiceTests): Hochladen einer Rechnung, die weder Restaurant noch Supermarkt ist 
  - Sonderfall 2 (UserServiceTests): Test auf inaktive/suspendierte User die einen Login versuchen

### Analyse der Codequalität
- PMD
- Beispiele von Findings:
  - Beispiel 1: AnomalyDetectionService naming_convention_violation: permanent_flag -> Geändert in permanentFlag
  - Beispiel 2: InvoiceService sysout -> Debugging lines mussten entfernt werden
  
### Projektdokus (System, Benutzerdoku)
---

## Sprint Retro
- xxx h (xxx h gesamt) gebuchte Stunden 30.4.-20.5.
- gröbere Probleme: 
  1. Eher zeitliche Probleme (weniger Zeit aufgrund einer Klausur in einer andern LVA)
  2. Mittlerweile viele Klassen, viel Code tlw. auch Codeverdoppelung -> effizienter wäre gewesen anfangs eine Klassenhierarchei (Abstrakte Klassen, Interfaces und Vererbungsstruktur durchzuplanen)
- Learnings:
  1. Vorbereitung: In den Osterferien haben wir bereits etwas vorgearbeitet -> Hat sich bewährt.  
  2. Projektvorbereitung: Anfangs mehr Zeit in die Projektstruktur investieren. 

## Einsatz von KI
- ...
- ...

## Planung finale Abgabe
- offene issues inkl. deren Priorisierung
