package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class HandlerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	private boolean isClosed;	//false = HandlerTCP running
	private String name;		//save user name after login
	private Socket tcpSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	private ChatServerData chatServerData;	//Singleton object to access central user data
	
	
	public HandlerTCP(Socket tcpSocket) {
		this.tcpSocket = tcpSocket;
		this.isClosed = false;
		this.chatServerData = ChatServerData.getChatSeverDataSingleton();
		
		try {
			this.writer = new PrintWriter(this.tcpSocket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shutdown(){
		
		this.isClosed= true;	
		writer.close();
		try {
			reader.close();
			tcpSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public void run() {
		
		log.info("HandlerTCP - Server running");
			
		String message =null;
		
		while(!isClosed){
						
				try {
					
					log.info("HandlerTCP - Server Wait for incoming command");
					//while( (message = reader.readLine()) != null){
					
					message = reader.readLine();
					
						if (message == null)
							break;
						
						log.info("message received from client");
						String messageToClient = this.checkCommand(message);
						log.info(messageToClient);
						writer.println(messageToClient);
					
						
				} catch (IOException e) {
					//e.printStackTrace();
					this.isClosed = true;	//if exeception occurs -> kill while(true) -> 
				}
			
				
		}
	}
	
	public String checkCommand(String message){
		
		//TODO return with error if not !messages
		
		String retMessage = "";
		String[] splittedStirings=null;
		splittedStirings =  message.split(" ");
		
		log.info("String split [0] = " + splittedStirings[0]);
		
		switch (splittedStirings[0]){
		
		case "!login":
			
			log.info("String split [1][2] = " + splittedStirings[1] + "  " + splittedStirings[2]);
			if(this.chatServerData.checkLogin(splittedStirings[1], splittedStirings[2])){
				retMessage = "Successfully logged in.";
			}
			else
				retMessage = "Wrong username or password.";
			break;
	
		case "!logout":
			
			this.chatServerData.logout(this.name);
			retMessage = "Successfully logged out.";
			break;
		}
		
		return retMessage;
	
	}

}
