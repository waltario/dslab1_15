package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HandlerTCP implements Runnable {

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
		/*
		try {
			
			this.clientSocket.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}*/
	}
	
	public String login(String username, String password) throws IOException{
		
		String messageToServer= username + password;
		writer.println(messageToServer);
		return reader.readLine();	
	}
	
	
	@Override
	public void run() {
	
		//start Streams
		try {
			
			this.writer = new PrintWriter(this.clientSocket.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			
			//wait for closing signal
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
