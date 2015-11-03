package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class HandlerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	private String name;					//save user name after login
	private Socket tcpSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	private ChatServerData chatServerData;	//Singleton object to access central user data
	private boolean isSingleMessage;		//false = is a pulbic message send to all online Clients, true = send 1 message tcp response
	private boolean isClosed;				//false = HandlerTCP running
	
	
	public HandlerTCP(Socket tcpSocket) {
		this.tcpSocket = tcpSocket;
		this.writer = null;
		this.reader = null;
		this.isClosed = false;
		this.isSingleMessage = false;
		this.chatServerData = ChatServerData.getChatSeverDataSingleton();
		
		//create Streams
		try {
			this.writer = new PrintWriter(this.tcpSocket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));
		
		} catch (IOException e) {
		}
	}
	
	public void writeToClient(String s){
		this.writer.println(s);
	}
	
	public void shutdown(){
		
		this.isClosed= true;	
		if(this.name != null)
			this.chatServerData.setUserOffline(this.name);
			//TODO delete from list chatseverdata
		
		//TODO logout user if destroyed? enough
		
		try {
			if(writer != null)
				writer.close();
			if(reader != null)
				reader.close();
			if(tcpSocket != null && !tcpSocket.isClosed())
				tcpSocket.close();
			
		} catch (IOException e) {

		}
		
		
	}
	public String getName(){
		return this.name;
	}
	
	
	@Override
	public void run() {
		
		log.info("HandlerTCP - Server running...");
			
		String message =null;
		
		while(!isClosed){
						
				try {
					
					log.info("HandlerTCP - Server Wait for incoming command");
					//while( (message = reader.readLine()) != null){
					
					message = reader.readLine();
					
						if (message == null)
							break;
						
						log.info("message received from client");
						String messageToClient = this.checkCommand(message);	//check incoming request and create response message
						log.info(messageToClient);
						if(!this.isSingleMessage)								//false = normal response to client 1 message
							writer.println(messageToClient);
						
						//normal receive <-> send messages
						this.isSingleMessage = false;
						
				} catch (IOException e) {
				
					//this.chatServerData.setUserOffline(this.name);
					//this.isClosed = true;	//if exeception occurs -> kill while(true) -> 
				}	finally {
					this.shutdown();
				}
		}
	}
	
	public String checkCommand(String command){
		
		//TODO return with error if not !messages
		
		if(command.startsWith("!send")){
			
			log.info("!send message detected");
			this.isSingleMessage = true;					//message has to be send to all online clients -> public message 
			String message = command.substring(6);	       //cuts off !send 
			String user_message = name + ": " + message;   //name of sender + message
			
			//write to all online clients except to sender
			//TODO all !exit clients deleted?
			for(HandlerTCP item : this.chatServerData.getAllOnlineTCPHandler(this.name)){
				
				//TODO return to all clients, except sender, the public message -> already done
				log.info("send to client" + item.getName());
				if(this.name.matches(item.getName())){
					item.writeToClient(user_message);
				}
				
			}
			//TODO added return null
			return null;
		}
		else{
			
			//split incoming request and check commands and parameter
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
					
					//TODO -> delete user from list
					log.info(this.chatServerData.getAllUsers());
					log.info("username to be logged out:" + this.name);
					this.chatServerData.logout(this.name);
					log.info("should be logged out");
					log.info(this.chatServerData.getAllUsers());
					retMessage = "Successfully logged out.";
					break;
				
		
				case "!register":
					
					log.info("!register command: " + splittedStirings[0] + " " + splittedStirings[1]);
					
					if(this.chatServerData.register(splittedStirings[1], this.name)){
						
						log.info("### ALL REG USERS ### "+this.chatServerData.getAllRegUsers());
						retMessage = "Successfully registered adress for "+ this.name + ".";
					}
					else
						retMessage = "User with that name already registered";
					break;
					
				case "!lookup":
					log.info("!lookup command: " + splittedStirings[0] + " " + splittedStirings[1]);
					
					log.info("+++ all reg Users +++\n)");
					log.info(this.chatServerData.getAllRegUsers());
					
					retMessage = this.chatServerData.lookup(splittedStirings[1]);
					log.info("return String !lookup: " + retMessage);
					break;
					
				default: 
					retMessage = "### ERROR - Not Valid Command ###";
					 break;
			}
				
			return retMessage;
		}
	}

}
