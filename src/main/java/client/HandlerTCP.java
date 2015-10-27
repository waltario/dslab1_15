package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

import chatserver.ChatServerListenerTCP;

public class HandlerTCP implements Runnable {

	private static final Logger log = Logger.getLogger(HandlerTCP.class.getName());

	private PrintWriter writer;
	private BufferedReader reader;
	private Socket clientSocket;
	private boolean isClosed;
	
	public HandlerTCP(Socket clientSocket) {
	
		this.clientSocket = clientSocket;
		this.writer = null;
		this.reader = null;
		this.isClosed = false;
		
	}
	
	
	public void close(){
		
		this.isClosed = true;
		
		writer.close();
		try {
			reader.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	public String login(String username, String password) throws IOException{
		
		String messageToServer= "!login " + username + " " + password;
		log.info(messageToServer);
		
		writer.println(messageToServer);
		//writer.write(messageToServer + "\n");
		//writer.flush();
		return reader.readLine();	
	}
	
	public String logout() throws IOException{
		
		String messageToServer= "!logout";
		log.info(messageToServer);
		//writer.write(messageToServer + "\n");
		//writer.flush();
		writer.println(messageToServer);
		return reader.readLine();		
	}
	
	
	@Override
	public void run() {
		
		log.info("Client HandlerTCP running");
		
		//start Streams
		try {
			
			this.writer = new PrintWriter(this.clientSocket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			
		
			while(!this.isClosed){
						
			}
		
		} catch (IOException e) {	//if chatserver gets down handle exception
			
			e.printStackTrace();
		} finally {
			if (clientSocket != null && !clientSocket.isClosed())
				try {
					clientSocket.close();
				} catch (IOException e) {
					// Ignored because we cannot handle it
				}

		}
		
	}

}
