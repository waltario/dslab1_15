package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
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
	private boolean isLoggedOut;
	
	
	public HandlerTCP(Socket tcpSocket) {
	
		log.setLevel(Level.OFF);
		
		this.tcpSocket = tcpSocket;
		this.writer = null;
		this.reader = null;
		this.isClosed = false;
		this.isLoggedOut = false;
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
		log.info("name = "+ this.name);
		if(this.name != null){
			//logout set user offline and deletes TCPHandler from list and clis
			log.info("try to logout via shutdown");
			chatServerData.logout(this.name);
			log.info("try to remove");
			chatServerData.removeTCPHandler(this.name);
			log.info("removed");
			//TODO delete from list chatseverdata
		}
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
		
		
						
				try {
					
					while(!isClosed){
					
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
						if(this.isLoggedOut)									//shutdown TCP Connection
							this.shutdown();
						
					}
						
				} catch (IOException e) {
				
					//this.chatServerData.setUserOffline(this.name);
					//this.isClosed = true;	//if exeception occurs -> kill while(true) -> 
				}	finally {
					this.shutdown();
					log.info("tcp handler closed");
					
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
				log.info("send to client " + item.getName());
				if(!this.name.equals(item.getName())){
					log.info("try to send: " + item.getName() +" message: " +   user_message);
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
						//TODO right?
						this.chatServerData.addTCPHandler(this);
						retMessage = "Successfully logged in.";
					}
					else{
						retMessage = "Wrong username or password.";
						//TODOD Rright? not registerd -> close tcp connection
						this.isLoggedOut = true;
					}
					break;
			
				case "!logout":
					
					//TODO -> delete user from list
					log.info(this.chatServerData.getAllUsers());
					log.info("username to be logged out:" + this.name);
					
					if(this.chatServerData.logout(this.name)){				//logout user
						this.chatServerData.removeTCPHandler(this.name);	// remove user from tcpHanlder List
						this.isLoggedOut =true;								//send successfully logout but destroy TCP after
						this.name = null;									//in shutdown function -> logout/remove user not twice done
						retMessage = "Successfully logged out.";
						
					}
					else
						retMessage = "### ERROR - Not able to logout User / User not available ###";
					
					log.info("should be logged out");
					log.info(this.chatServerData.getAllUsers());
					
					break;
				
		
				case "!register":
					
					log.info("!register command: " + splittedStirings[0] + " " + splittedStirings[1]);
					
					if(splittedStirings[1].matches("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})")){
						this.chatServerData.register(splittedStirings[1], this.name);
						retMessage = "Successfully registered adress for "+ this.name + ".";
					}
					else
						retMessage = "### Error IP not in a valid form e.g. 192.168.0.10:10982";
						
					
					/*
					if(this.chatServerData.register(splittedStirings[1], this.name)){
						
						log.info("### ALL REG USERS ### "+this.chatServerData.getAllRegUsers());
						retMessage = "Successfully registered adress for "+ this.name + ".";
					}
					else
						retMessage = "User with name already registered";
					*/
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
