# Software Engineering I - Teilaufgabe 1 (Anforderungsanalyse und Planungsphase)

## Abgabedokument - Teilaufgabe 1 (Anforderungsanalyse und Planungsphase)

### Persönliche Daten, bitte vollständig ausfüllen:

- Nachname, Vorname: Kostariev Dmytro 
- Matrikelnummer: 11848552




## Aufgabe 1: Anforderungsanalyse



**Anforderung 1**

- **Anforderung**: [Spielerregistrierung MUSS moeglich sein – Ein Client MUSS sich nach Spielstart beim Server registrieren, um am Spiel teilzunehmen]
- **Bezugsquelle**: [Spielidee: „Nach Start des Clients registrieren sich die KIs fuer das Spiel am Server …“ (ca. Z. 11–13)]

**Anforderung 2**

- **Anforderung**: [Kartenhaelfte MUSS uebertragen werden - Der Client MUSS nach erfolgreicher Registrierung eine zufaellig erzeugte Kartenhaelfte an den Server senden.]
- **Bezugsquelle**: [Spielidee: „… erstellen/tauschen danach mit dem Server Kartenhaelften aus.“ (ca. Z. 13–14)
]

**Anforderung 3**

- **Anforderung**: [Spielfigur MUSS bewegt werden koennen – Die KI MUSS in der Lage sein, ihre Spielfigur ueber die Spielkarte zu bewegen, um Felder aufzudecken.]
- **Bezugsquelle**: [Spielidee: „Um den Schatz zu finden, bewegen beide KIs ihre Spielfigur ueber die Karte und decken dabei … Kartenfelder auf.“ (ca. Z. 34–37)]

### Typ der Anforderung: nicht funktional

**Anforderung 4**

- **Anforderung**: [Rundenzeit DARF NICHT ueberschritten werden – Die KI DARF NICHT laenger als 5 Sekunden fuer eine Spielaktion benoetigen.]
- **Bezugsquelle**: [Spielidee: „Fuer jede dieser rundenbasierten Spielaktionen hat die KI maximal 5 Sekunden Bedenkzeit.“ (ca. Z. 85–86)]

**Anforderung 5**

- **Anforderung**: [Maximale Spielzeit MUSS eingehalten werden – Ein Spiel MUSS nach maximal 320 Spielaktionen oder 10 Minuten beendet werden.]
- **Bezugsquelle**: [Spielidee: „… dass ein Spiel insgesamt nicht laenger als 320 Spielaktionen (und damit 320 Runden) dauern darf … Insgesamt dauert ein Spiel maximal 10 Minuten.“ (ca Z. 80–88)]

**Anforderung 6**

- **Anforderung**: [Client MUSS serverkompatibel sein – Der Client MUSS ausschliesslich die vom Protokoll definierten Nachrichtenstrukturen verwenden, um Kompatibilitaet sicherzustellen]
- **Bezugsquelle**: [Netzwerkprotokoll: „… standardisiertes Netzwerkprotokoll … ermoeglicht Ihnen … gegen andere Studierende anzutreten.“ (ca. Z. 10–15)]

### Typ der Anforderung: Designbedingung

**Anforderung 7**

- **Anforderung**: [Die Implementierung MUSS in Java erfolgen – Die KI-Implementierung MUSS vollstaendig in der vom Institut vorgegebenen Programmiersprache Java erfolgen. Andere Programmiersprachen oder Frameworks DÜRFEN NICHT verwendet werden.]
- **Bezugsquelle**: [Technische Uebungsvorgaben / Netzwerkbibliothek: https://homepage.univie.ac.at/kristof.boehmer/SE1/Network_Library/Documentation/]




## Aufgabe 2: Anforderungsdokumentation




### Anforderungsüberblick

- **Name**: [Spielfigur bewegen]
- **Beschreibung und Priorität**: [Der Client MUSS Bewegungskommandos fuer die eigene Spielfigur absetzen koennen, um sich orthogonal feldweise ueber die Karte zu bewegen und dabei Felder aufzudecken. Die Anforderung ist Hoch, da ohne Bewegung weder Schatz noch gegnerische Burg gefunden werden koennen.]
- **Relevante Anforderungen**:
   - A1 Registrierung MUSS moeglich sein (Zugriff auf Spiel ueberhaupt).
   - A2 Kartenhaelfte MUSS uebertragen werden (Karte entsteht aus beiden Haelften).
   - A4 Rundenzeit DARF NICHT ueberschritten werden (<= 5 s/Aktion).
   - A5 Maximale Spielzeit MUSS eingehalten werden (<= 320 Aktionen/10 min).
   - A6 Client MUSS serverkompatibel sein (Nachrichtenformat/Protokoll).
- **Relevante Business Rules**: 

   - Rundenbasiert: Pro Runde genau eine Aktion; Aktion nur, wenn „ an der Reihe “.
   - Zeitlimits: <= 5 s Bedenkzeit je Aktion; Spiel <= 320 Aktionen/<= 10 min.
   - Bewegungsrestriktionen: nur horizontal/vertikal zu benachbarten Feldern; Wasser DARF NICHT betreten werden (sofortige Niederlage); Kartenflucht (Off-Map) fuehrt ebenfalls zum Verlust.
   - Terrainkosten & Effekte: Wiese 1 Aktion (aufdecken/ggf. Schatz aufnehmen); Berg 2 Aktionen hinein + 2 hinaus; beim Betreten Berg werden versteckte Schaetze/Burgen in Reichweite 1 (auch diagonal) aufgedeckt.
   - Sichtbarkeit: Zu Beginn sind Terrain, eigene Burg, Spielerpositionen sichtbar; Schatz & gegnerische Burg werden erst beim Aufdecken sichtbar. Gegnerposition ist in den ersten 8 eigenen Aktionen zufaellig.
   - Statuspruefung: Client MUSS vor Aktionsversand pruefen, ob er an der Reihe ist (Status-/Turn-Nachricht). 

### Impuls/Ergebnis - Typisches Szenario



**Vorbedingungen:**

- Client ist registriert und Teil eines laufenden Spiels (SpielID bekannt).
- Karte wurde erzeugt (Kartenhaelften uebertragen/zusammengefuegt).
- Client ist laut Status an der Reihe.

**Hauptsächlicher Ablauf:**

1. Impuls: Client fragt Spielstatus ab (Zugrecht).
   Ergebnis: Server bestaetigt „an der Reihe“.

2. Impuls: Client sendet Bewegung „rechts“ (orthogonal, 1 Feld).
   Ergebnis: Server validiert Zug (Zeitlimit, Feld erreichbar, kein Wasser/Off-Map). Bei Erfolg wird Position intern aktualisiert.

3. Impuls: Server wertet neue Position aus.
   Ergebnis: Feld wird aufgedeckt; bei Wiese: evtl. Schatz/Burg sichtbar/aufgenommen; bei Berg: zusaetzliche Aufdeckung in Reichweite 1.

4. Impuls: Server sendet aktualisierte Sicht-/Positionsdaten (z. B. via ResponseEnvelope).
   Ergebnis: Client aktualisiert Karte im CLI. Runde endet, Gegenspieler ist am Zug.

**Nachbedingungen:**

- Client hat die neue Spielerposition, aufgedeckte Felder und ggf. Inventarstatus (Schatz aufgenommen) lokal aktualisiert.

### Impuls/Ergebnis - Alternativszenario


**Vorbedingungen:**

- Client ist registriert und Teil eines laufenden Spiels (SpielID bekannt).
- Karte wurde erzeugt (Kartenhaelften uebertragen/zusammengefuegt).
- Client ist laut Status an der Reihe.
- Es wurden bereits mehrere Bewegungsbefehle in eine Richtung gesendet (z. B. nach links), die Ausfuehrung ist aber noch nicht - vollstaendig abgeschlossen.
- Es sind < 8 eigene Aktionen absolviert (Gegnerposition kann noch zufaellig wirken).

**Hauptsächlicher Ablauf:**

1. Impuls: Client fragt Spielstatus ab (Zugrecht).
   Ergebnis: Server bestätigt „an der Reihe“.

2. Impuls: Client ändert die Bewegungsrichtung (z. B. statt „links“ jetzt „oben“) und sendet den neuen Bewegungsbefehl.
   Ergebnis: Alle zuvor gesendeten, aber noch nicht vollständig ausgeführten Bewegungsbefehle verfallen; die dafür aufgewendeten Spielrunden zählen trotzdem als verbraucht. Die neue Richtung wird ab jetzt gezählt/bewertet.

3. Impuls: Server validiert die neue Bewegung (Zeitlimit, Feld erreichbar, kein Wasser/Off-Map).
   Ergebnis: Bei Erfolg wird die Position entsprechend aktualisiert; das Zielfeld wird aufgedeckt (bei Wiese: ggf. Schatz/Burg sichtbar/aufgenommen; bei Berg: zusätzlicher Aufdeck-Effekt in Reichweite 1).

4. Impuls: Server sendet Sicht-/Positionsupdate.
   Ergebnis: Client aktualisiert die Karte im CLI. Gegnerposition kann in den ersten 8 eigenen Aktionen zufällig „springen“ und wird dennoch angezeigt. Runde endet, Gegenspieler ist am Zug.


**Nachbedingungen:**

- Rundenanzahl hat sich auch durch die verfallenen Bewegungen erhoeht; der Client hat dadurch Aktionsbudget verloren, ohne die urspruenglich geplante Wegstrecke vollstaendig zurueckgelegt zu haben.

- Karte/Sicht wurden gemaess neuem Zug aktualisiert; ggf. Inventar (Schatz aufgenommen) angepasst.

- Im CLI ist die (ggf. zufaellige) Gegnerposition konsistent dargestellt (fruehe Spielphase).

### Impuls/Ergebnis - Fehlerfall


**Vorbedingungen:**

- Client ist registriert (SpielID bekannt) und mit dem Server verbunden.
- Eine Karte liegt vor (Kartenhaelften wurden uebertragen/zusammengefuegt).
- Der Client verfuegt ueber eine gueltige, letzte Statusantwort (Turn-Info).


- Fehlerfall A: Bewegung wuerde Wasser betreten (oder Off-Map verlassen)

**Hauptsächlicher Ablauf:**

   1.   Impuls: Client sendet eine Bewegung, deren Zielfeld Wasser oder ausserhalb der Karte ist.
        Ergebnis: Server erkennt Regelverstoss. Betritt die Spielfigur Wasser bzw. verlaesst die Karte, verliert die KI sofort.
   
   2.   Impuls: Server sendet Abschlussmeldung (Sieg/Niederlage) im Response; der Gegenspieler wird ueber seinen Sieg informiert.
        Ergebnis: Client zeigt im CLI den Endzustand und terminiert sich gemaess Vorgabe; es werden keine weiteren Aktionen (Kartenhaelfte, Bewegungen) mehr gesendet.

**Nachbedingungen:**

- Spiel beendet; Verlierer: die regelverletzende KI. Client beendet sich selbststaendig.



- Fehlerfall B: Client sendet Aktion, obwohl er nicht an der Reihe ist

**Hauptsächlicher Ablauf:**

  1. Impuls: Client ueberspringt die Statuspruefung oder ignoriert sie und sendet eine Bewegung, obwohl der Gegner an der Reihe ist.
     Ergebnis: Server stellt Regelverstoss fest; Clients, die diese und andere Spielregeln nicht einhalten, werden bestraft (Verlust).
  
  2. Impuls: Server sendet Abschlussmeldung (Niederlage) im Response.
     Ergebnis: Client zeigt im CLI den Endzustand und terminiert sich.

**Nachbedingungen:**
- Spiel beendet; Verlierer: der Client, der ohne Zugrecht agiert hat.


- Fehlerfall C: Zeitlimit pro Aktion (5s) wird ueberschritten

**Hauptsächlicher Ablauf:**
  1. Impuls: Client benoetigt fuer die naechste Bewegungsentscheidung/-sendung laenger als 5 Sekunden.
     Ergebnis: Die aktive KI verliert automatisch (Zeitlimitverletzung).
  
  2. Impuls: Server sendet Abschlussmeldung (Niederlage) an den Client; der andere Spieler wird ueber den Sieg informiert.
     Ergebnis: Client zeigt im CLI den Endzustand und terminiert sich.

**Nachbedingungen:** 
 - Spiel beendet; Verlierer: aktive KI wegen Zeitueberschreitung. Keine weiteren Aktionen werden gesendet.
 
### Benutzergeschichten

- Als Client-KI moechte ich meine Spielfigur orthogonal ueber die Karte bewegen, um Felder aufzudecken 🧭 und den Schatz zu finden 💰.

- Als Server moechte ich jede Bewegungsaktion der KI validieren ✅ (Zugrecht, Zeitlimit, Terrainregeln), um Spielregeln korrekt durchzusetzen ⚖️ und die Sichtinformationen aktuell zu halten.

- Als menschlicher Anwender 👤 moechte ich ueber die CLI den aktuellen Kartenstatus, die Position der Spielfigur und aufgedeckte Felder erkennen

### Benutzerschnittstelle


|   | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |
|---|---|---|---|---|---|---|---|---|---|---|
| 0 | 🏰 | 🟩 | 🟩 | 🟩 | ❌ | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |
| 1 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🗻 | 🟩 | 🟩 | 🟩 |
| 2 | 🟩 | 🟩 | 🤖 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 💰 | 🟩 |
| 3 | 🟩 | 🟩 | 🟩 | 🟩 | ❌ | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |
| 4 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |




Legende:
🏰 = eigene Burg

❌ = Wasser (verboten)

🟩 = Wiese

🗻 = Berg

💰 = Schatz (falls aufgedeckt)

🤖 = Spielfigur




### Externe Schnittstellen

- Netzwerkkommunikation erfolgt ueber eine REST-aehnliche Schnittstelle (HTTP, XML) gemaess dem bereitgestellten Netzwerkprotokoll.
  - Anfrage (Client): Bewegung an Server senden (POST) → Endpunkt 
    /games/{GameID}/players/{PlayerID}/move
   → Inhalt: Bewegungsrichtung (UP, DOWN, LEFT, RIGHT), Metadaten (PlayerID, GameID)
  - Antwort (Server): ResponseEnvelope (XML) mit aktualisierten Sichtdaten (z. B. Position, neue Felder, Spielstatus).

- Protokolleigenschaften:
  - Transport: HTTP
  - Datenformat: XML
  - Statuscode: immer 200 (vereinfachtes Handling)
  - Server validiert Bewegung (Zugrecht, Terrain, Zeitlimit) → Client aktualisiert CLI.

- Abhaengigkeiten / Einschraenkungen:
  - Nur orthogonale Bewegungen erlaubt.
  - Wasser- oder Off-Map-Bewegungen fuehren zur sofortigen Niederlage.
  - Aktionen nur waehrend des eigenen Zugs zulaessig.

 ### Quellen dokumentieren - Aufgabe 2: Anforderungsdokumentation

- Bewegung & Aufdecken der Felder (Ziel: Schatz → gegnerische Burg)
- Bezugsquelle: Spielidee (ca. Z. 34–45).

- Rundenbasiert, genau 1 Aktion pro Zug; Zugrecht pruefen
- Bezugsquelle: Spielidee (ca. Z. 90–110).

- Zeitregeln: ≤ 5 s pro Aktion; Spiel ≤ 320 Aktionen bzw. ≤ 10 Min
- Bezugsquelle: Spielidee (ca. Z. 120–140).

- Kartenaufbau: 2 × (5×10) → Server fuegt zu 10×10 oder 5×20; Terrain: Wasser/Wiese/Berg
- Bezugsquelle: Spielidee (ca. Z. 150–185).

- Regeln: nur orthogonal; Wasser/Off-Map ⇒ sofortige Niederlage; Burg/Schatz nur Wiese
- Bezugsquelle: Spielidee (ca. Z. 185–215).

- Sichtbarkeit: Terrain/Burg/Startpos sichtbar; Schatz & gegnerische Burg versteckt; Gegnerpos in ersten 8 eigenen Aktionen zufaellig
- Bezugsquelle: Spielidee (ca. Z. 55–85 + Hinweis „Zufaellige Position des Gegners…“).

- Registrierung der Clients mit Spiel-ID (Start durch Menschen; danach vollautomatisch)
- Bezugsquelle: Spielidee (ca. Z. 10–25).

- Netzwerkprotokoll: HTTP + XML; „REST-aehnlich“; Server antwortet (vereinfachend) i. d. R. mit 200
- Bezugsquelle: Netzwerkprotokoll / Einleitung + Allgemeines (Dok-Beginn, Abschn. 1 & 4).

- Use-Cases/Methodenmodellierung: Registrieren, Kartenhaelfte senden, Status pruefen, Bewegungen; ResponseEnvelope als Antwort-Wrapper
- Bezugsquelle: Netzwerkprotokoll / Modellierung + Use-Case-Leitfaden (Abschn. 2–3).

- Spielerzeugung (Game-ID), Registrierung (POST /games/{id}/players), Statusabfrage vor Aktionen
- Bezugsquelle: Netzwerkprotokoll / Absch. 5–6 + Status-Hinweise (Endpoints & Nachrichten).


## Aufgabe 3: Architektur entwerfen, modellieren und validieren

### Quellen dokumentieren - Aufgabe 3: Architektur entwerfen, modellieren und validieren

- **Kurzbeschreibung der Übernommenen Teile**: *Was & Wo im Projekt, In welchem Umfang (Idee, Konzept, Texte, Grafik etc.) mit und ohne Anpassungen, etc.*
- **Quellen der Übernommenen Teile**: *Folien, Bücher, Namen der Quell-Studierenden, URLs zu Webseiten, KI Prompts, etc.*

### Klassendiagramm

[Klassendiagramm hier samt, bei Bedarf, Beschreibung beziehungsweise Erläuterung einfügen]
![Klassendiagramm](TemplateImages/Klassendiagramm.svg)

### Sequenzdiagramm 1

[Sequenzdiagramm 1 hier samt, bei Bedarf, Beschreibung beziehungsweise Erläuterung einfügen]

### Sequenzdiagramm 2

[Sequenzdiagramm 2 hier samt, bei Bedarf, Beschreibung beziehungsweise Erläuterung einfügen]



## Aufgabe 4: Quellen dokumentieren

Dokumentieren Sie Ihre Quellen. Dies ist für Sie wichtig, um die Einstufung einer Arbeit als Plagiat zu vermeiden. Geben Sie hierzu Ihre Quellen in den jeweils vorgesehenen Bereichen direkt bei Beginn der jeweiligen Aufgaben an. Inhalte, die direkt aus dem Moodle Kurs dieses Semesters der LV Software Engineering 1 stammen, können zur Vereinfachung weggelassen werden. Alle anderen Inhalte sind zu zitieren. Die Vorgabe des Studienpräses der Universität Wien lautet: *"Alle fremden Gedanken, die in die eigene Arbeit einfließen, müssen durch Quellenangaben belegt werden."* 


