package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HandlerTCP implements Runnable{

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
		
		try {
			
			this.writer = new PrintWriter(this.tcpSocket.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));	
			
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		String message =null;
		
		while(!isClosed){
						
				try {
					
					while( (message = reader.readLine()) != null){
						
						this.checkCommand(message);
						
					}
				} catch (IOException e) {
					//e.printStackTrace();
					this.isClosed = true;	//if exeception occurs -> kill while(true) -> 
				}
			
				
		}
	}
	
	public String checkCommand(String message){
		
		String retMessage = "";
		String[] splittedStirings=null;
		splittedStirings =  message.split(" ");
		
		switch (splittedStirings[0]){
		
		case "!login":
			
			if(this.chatServerData.checkLogin(splittedStirings[1], splittedStirings[2])){
				retMessage = "Successfully logged in.";
			}
			else
				retMessage = "Wrong username or password.";
			
	
		case "!logout":
			
			this.chatServerData.logout(this.name);
			retMessage = "Successfully logged out.";
		
		}
		
		return retMessage;
	}

}
