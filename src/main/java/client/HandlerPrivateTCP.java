package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandlerPrivateTCP implements Runnable{

	private static final Logger log = Logger.getLogger(HandlerPrivateTCP.class.getName());
	
	private Socket privateSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	private String message;
	private boolean isClosed;
	
	public HandlerPrivateTCP(Socket privateSocket) {
		log.setLevel(Level.OFF);
		this.privateSocket = privateSocket;
		this.isClosed = false;
		init();
	}
	
	private void init(){
		try {
			this.writer = new PrintWriter(this.privateSocket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(this.privateSocket.getInputStream()));
		} catch (IOException e) {
			
		}
		
	}
	
	public void close(){
		this.isClosed = true;
		
		if(this.writer != null)
			this.writer.close();
		if(this.reader != null)
			try {
				this.reader.close();
			} catch (IOException e) {
				
			}
		
		if(this.privateSocket != null && !this.privateSocket.isClosed()){
				try {
					this.privateSocket.close();
				} catch (IOException e1) {	
				}
		}
	}
		
	
	public boolean msg(String message){
	
		if(this.writer == null)
			log.info("writer null");
		if(this.reader == null)
			log.info("reader null");
		
		//send message to Client
		log.info("request: " + message);
		this.writer.println(message);
		String retMessage = null;
		try {
			retMessage = this.reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("response: " + retMessage);
		
		if(retMessage.matches("!ack"))
			return true;
		else
			return false;
		
	}
	
	@Override
	public void run() {
		
		log.info("HandlerPrivatetTCP running...");
		
		while(!this.isClosed){	
		}	
	}
	

}
