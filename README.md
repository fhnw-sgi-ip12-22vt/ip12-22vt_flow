# Project FLOW

## Beschreibung

Die Primeo Energie AG ist ein Energieversorgungsunternehmen in Münchenstein und versorgt Teile von Basel-Landschaft, Solothurn und Saint-Louis (F) mit Strom. Primeo legt einen grossen Wert auf erneuerbare Energien.

Primeo hat bereits eine Lernwelt in Münchenstein und in Olten aufgebaut. In dieser Lernwelt werden Workshops angeboten, Experimente können durchgeführt werden und Führungen werden angeboten. Link: [primeo-energie.ch](https://www.primeo-energie.ch/ueber-uns/lernwelt-energie.html)

Mit dem Projekt Primeo Energie Kosmos wird ein neues Science- und Erlebniscenter in Münchenstein gebaut. Es soll eine Verbindung zwischen Klima und Energie geschaffen werden und erneuerbare Energien attraktiver gemacht werden.

### Grundidee
Grundidee unseres Produktes ist, Kindern und Jugendlichen nachhaltige Energieproduktionen und das Max Flow Problem spielerisch näher zu bringen. Die physischen Komponenten sollen bei den Personen ein Interesse wecken, damit sie von alleine auf das Produkt zugehen. Mithilfe von haptischen Elementen soll die Spielperson aktiver am Spiel teilnehmen können.  Eine kurze Spieldauer von 2-3min verhindert Langeweile und Frustration bei der spielenden Person. Um die Verweilzeit am Spiel zu verlängern und die Herausforderung für geschickte Spielpersonen zu erhöhen, können schwierigere Levels ausgewählt werden.



Ziel des Spiels ist es, von der Source (in Abb.1 die Windkraftanlage und der Staudamm) möglichst viel Strom in die Senke (die Fabrik) zu leiten. Dafür wählt die Spielperson die Verbindungslinie zwischen den Häusern mit einem Knopfdruck an und wählt auf dem Display die Menge an Stromeinheiten (0,1,2 oder 3), welche durch diese ausgewählte Kante fliessen soll, aus. Die rote Zahl ist die Maximalkapazität dieser Kante, das heisst die Spielperson kann nicht mehr Einheiten Strom legen, als die Maximalkapazität der Kante. Versucht die Spielperson dennoch einen höheren Wert zu legen, blinkt die Kante rot und nimmt den gelegten Wert nicht an.

Ein Haus kann nur soviel Einheiten Strom weitergeben, wie es auch erhalten hat (Input >= Output). Wird mit einem Spielzug der Input < Output eines Hauses, blinkt die manipulierte Kante und dann die Kontrollleuchte rot und der Wert wird nicht gespeichert. Eine Lösung der Spielperson wird akzeptiert, sobald mindestens 1 Einheit Strom in der Fabrik ankommt.

In schwierigeren Leveln weht kein Wind mehr und die Windkraftwerke produzieren keinen Strom mehr, oder es muss ein Zwischenziel mit Strom versorgt werden. Beim Zwischenziel leuchtet die Kontrollleuchte orange.

Wer nicht unnötig viel Einheiten Strom legt, wird am Ende mit einem höheren Score belohnt. Im Optimalfall sind also bei allen Häusern Input = Output.
## Requirements
[Verweis](Docu/Requirements_Flow.pdf) zu den Requirements.
## Usablity Dossier
[Verweis](Docu/Usability_Dossier.pdf) zu dem Usability Dossier.
## Teileliste
[Verweis](Docu/hardware/Hardware.adoc) zu der Teileliste.

## Visuals
 
Erste Skizze des Projekts: <br />
<img src="https://media.milanote.com/p/resized/1ONKCu1Q4No25K/1ONKCu1Q4No25K-TuAe9-huge.png" alt="Erste Skizze des Projekts" width="50%" height="50%" title="Erste Skizze"> <br />
Projektarbeit während der zweiten Projektwoche: <br />
<img src="https://app.milanote.com/media/p/images/1Q1mxx18HKTK5M/aTo/image.png?w=800&v=2&elementId=1Q1mxx18HKTK5M" alt="Arbeit an Projekt von zwei Projektmitgliedern" width="50%" height="50%" title="Arbeit an Projekt"> <br />
Erfolgreiches Leuchten der Verbindungslinien: <br />
<img src="https://app.milanote.com/media/p/images/1Q1mvc18HKTK5L/MvN/image.png?w=800&v=2&elementId=1Q1mvc18HKTK5L" alt="Projekt mit beleuchteten Plexiglas Stäben" width="50%" height="50%" title="Projekt Bild 1"> <br />


## Installation

### Vorinstallation

Link für das übernommene Setup: [pi4j.com/getting-started](https://pi4j.com/getting-started/set-up-a-new-raspberry-pi/)
JDK und I2C Konfigurationsänderungen sind im Verweis nicht enthalten.

- Raspberry Pi OS (früher Raspbian) von der offiziellen WebsiteImage auf eine Micro-SD-Karte laden mit Tool Raspberry Pi Imager.
- Micro-SD-Karte mit Tastatur, Maus, HDMI-Kabel, Ethernet-Kabel verbinden und schliesslich das Stromkabel an dem Raspberry Pi anschliessen.
- Raspberry Pi einschalten und warten bis das Raspberry Pi OS gestartet ist.
- Einrichtungsassistenten ausführen, um die Sprache, Zeitzone, Tastaturbelegung und Netzwerkverbindung einzustellen.
- Installiere Bellsoft Liberica full jdk 17 (https://bell-sw.com/pages/downloads/) auf dem Raspberry pi.
- Tool "raspi-config" verwenden, um Einstellungen anzupassen: Ausführen des Befehls "sudo raspi-config"
  - Pfeiltasten verwenden, um zu "Interfacing Options" oder "Schnittstellenoptionen" zu navigieren und Enter betätigen.
  - Navigation zu "I2C" und Enter drücken.
  - Nach Frage ob ARM I2C-Schnittstelle aktivieren werden sollte, "Ja" auswählen und Enter drücken.
- Anschliessen von Maus und Tastatur über den USB-A Anschluss des Raspberry Pi 4 
- Bedienen des Raspberry Pi über Maus und Tastatur 
- Starten des Programms "Terminal"
- Ausführen des Befehls: "cd /opt/flow". Nun befindet sich man im Verzeichnis des Projekts. 
- Das Projekt muss nun geklont werden: [git clone](git-repo-clone-url).

### Rahmenbedingungen
[Verweis](Docu/software(sad)/src/04_solution_strategy.adoc) zu den Rahmenbedingungen.


### Hardware Setup
Der physische Spielkasten beinhaltet alle Hardware Komponenten des Produkts "FLOW".
Folgende Schritte um die Hardware startbereit zu bringen, sind nötig:

- Das daran verbundene Stromkabel muss an einer Steckdose (Typ J) angeschlossen werden.
- Der Switch an dem Stromkabel muss betätigt werden.

Nun hat das Produkt ausreichend Strom um zu funktionieren.

### Software Setup
Um das Softwaresetup auszuführen wird das Hardwaresetup vorausgesetzt.
Die Software wird mithilfe maven gestartet.
Um die Applikation erfolgreich zu starten, werden folgende Schritte benötigt:

- Anschliessen von Maus und Tastatur über den USB-A Anschluss des Raspberry Pi 4
- Bedienen des Raspberry Pi über Maus und Tastatur
- Starten des Programms "Terminal"
- Ausführen des Befehls: "cd /opt/flow". Nun befindet sich man im Verzeichnis des Projekts.
- Ausführen des Befehls: "i2cset -y 1 0x70 0x4"
- Ausführen des Befehls: "export DISPLAY=:0 && xhost local:root && sudo mvn clean javafx:run" im Verzeichnis des Projekts.
- Alternativ kann der pi im selben netzwerk eines Computers sein, welcher mit ssh verbunden wird. In diesem fall kann das bash script: unter /assets/bash-script/run.sh verwendet werden und mit "./run.sh & disown" detached ausgeführt werden.


### Future Requirements
Future Requirements sind im Anhang des Dokuments [Requirements](Docu/Requirements_Flow.pdf) zu finden.


## Autoren und Anerkennung
- Tim H.
- Joshua B.
- Adrian M.
- Shane Z.
- Maik D.
- Erhan B.
- Ron G.

## License
Das Produkt steht unter "MIT License"