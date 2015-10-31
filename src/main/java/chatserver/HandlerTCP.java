package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HandlerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	private boolean isClosed;	//false = HandlerTCP running
	private String name;		//save user name after login
	private Socket tcpSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	private ChatServerData chatServerData;	//Singleton object to access central user data
	private boolean isSingleMessage;
	
	
	public HandlerTCP(Socket tcpSocket) {
		this.tcpSocket = tcpSocket;
		this.isClosed = false;
		this.isSingleMessage = false;
		this.chatServerData = ChatServerData.getChatSeverDataSingleton();
		
		try {
			this.writer = new PrintWriter(this.tcpSocket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeToClient(String s){
		this.writer.println(s);
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
	public String getName(){
		return this.name;
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
						if(!this.isSingleMessage)
							writer.println(messageToClient);
					
						
				} catch (IOException e) {
					//e.printStackTrace();
					this.isClosed = true;	//if exeception occurs -> kill while(true) -> 
				}
			
				//normal receive <-> send messages
				this.isSingleMessage = false;
		}
	}
	
	public String checkCommand(String command){
		
		//TODO return with error if not !messages
		
		
		if(command.startsWith("!send")){
			
			log.info("!send message detected");
			this.isSingleMessage = true;
			String message = command.substring(6);	       //cuts off !send 
			String user_message = name + ": " + message;   //name of sender + message
			
			//send to all online clients
			
			for(HandlerTCP item : this.chatServerData.getAllOnlineTCPHandler(this.name)){
				
				//TODO return to all clients, except sender, the public message
				log.info("send to client" + item.getName());
				item.writeToClient(user_message);
			}
			
		}
		
		String retMessage = "";
		String[] splittedStirings=null;
		splittedStirings =  command.split(" ");
		
		
		log.info("String split [0] = " + splittedStirings[0]);
		
		switch (splittedStirings[0]){
		
		case "!login":
			
			log.info("String split [1][2] = " + splittedStirings[1] + "  " + splittedStirings[2]);
			if(this.chatServerData.checkLogin(splittedStirings[1], splittedStirings[2])){
				this.name = splittedStirings[1];
				retMessage = "Successfully logged in.";
			}
			else
				retMessage = "Wrong username or password.";
			break;
	
		case "!logout":
			
			log.info(this.chatServerData.getAllUsers());
			log.info("username to be logged out:" + this.name);
			this.chatServerData.logout(this.name);
			log.info("should be logged out");
			log.info(this.chatServerData.getAllUsers());
			retMessage = "Successfully logged out.";
			break;
		}
		
		return retMessage;
	
	}

}
