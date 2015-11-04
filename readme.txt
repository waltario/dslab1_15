Reflect about your solution!

Summary:

1. ChatServer - liest die Konfigurationsdateien aus (Ports) und initalisiert die User/Passwort -Maps
			  - startet die 2 Threads -> ChatServerListenerTCP
									  -> ChatServerListenerUDP
									  
			  - führt die Kommandos !users und !exit aus


	1.1 ChatServerListenerTCP (Runnable)  - mit .accept() Methode wartet auf incoming TCP Connections -> uebergibt Arbeit an HandlerTCP
											initalisiert FixedThreadPool für max 25 HandlerTCP Objekte.
								
	1.2 HandlerTCP:Server(Runnable) - Kommunikation der TCP Connections, Kommandos zwischen   HandlerTCP(Server) <-> HandlerTCP(Client)
									  empfängt Kommandos vom Client und führt diese aus,
							   
	1.3 ChatServerListenerUDP(Runnable)	- datagramSocket.receive(packet) wartet auf incoming UDP Packete -> uebergibt Arbeit an HandlerUDP
										- initalisiert FixedThreadPool für max 25 HandlerUDP Objekte.
	
	1.4 HandlerUDP(Runnable)	- liest Packet aus und antwortet auf Request	
					
	
	

2. Cllient - führt die Kommandos von der Konsole aus
		   - bei Start init UDP Connection wegen !list command 			 -> Thread 1x ClientHandlerUDP()
		   - erst nach !login Kommando wird die TCP Connection gestartet -> Thread 1x HandlerTCP(socket) 
	
	2.1 ClientHandlerUDP - erzeugt ein UDP Packet und sendet es an eine festgelegt ip + port Nummer
	                       mit dem !list Kommando
						
	2.2 HandlerTCP:Client(Runnable)- 	fuehrt die Kommunikation mit dem Server TCPHandler durch - übermittel die Befehle mittels String
									    erzeugt newFixedThreadPool(1) x3 fuer   1x ClientResponseHandler (public message(tcp) or req/resp. tcp commandos) 
										1x HandlerPrivateTCP -> fuer !msg send Kommando und 1x ClientPrivatServer -> startet nach erfolgreichem !register kommando
										und wartet auf incoming !msg Kommandos
										I/O Streams 
										
	2.3 HandlerPrivateTCP (Runnable) - writer / reader - sended !msg an PrivateMsgHandler der dann mit einem !ack antwortet 
	
	2.4 PrivateMsgHandler (Runnable) - bei incoming TCP Connection nach !register am Client HandlerTCP wird Arbeit an PrivateMsgHandler übergeben
									   dieser antwortet mit !ack auf ein erfolgreiches !msg
										

3. ChatServerData(Singelton) - speichert User Credentials in Maps, speichert TCP ConnectionList, online /offline Map.
                               Ausgabe von Userlisten in verschiedenen Formaten 