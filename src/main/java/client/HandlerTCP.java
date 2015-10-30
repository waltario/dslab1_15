package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import chatserver.ChatServerListenerTCP;

public class HandlerTCP implements Runnable {

	private static final Logger log = Logger.getLogger(HandlerTCP.class.getName());

	private PrintWriter writer;
	private BufferedReader reader;
	private Socket clientSocket;
	private boolean isClosed;
	private boolean loggedIn;
	//private Thread t_MessageChecker;
	private ExecutorService executor;
	private ClientResponseHandler responseHandler;
	
	public HandlerTCP(Socket clientSocket) {
	
		this.clientSocket = clientSocket;
		this.writer = null;
		this.reader = null;
		init();
		this.isClosed = false;
		this.loggedIn = false;
		executor = Executors.newFixedThreadPool(1);	
	}
	
	public void init(){
		try {
			this.writer = new PrintWriter(this.clientSocket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void close(){
		
		this.isClosed = true;
		executor.shutdown();			//dont accept any incoming threads
		writer.close();
		try {
			reader.close();
			clientSocket.close();
			this.responseHandler.close();
			//this.t_MessageChecker.
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
		     // Wait a while for existing tasks to terminate
		     if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
		    	 executor.shutdownNow(); // Cancel currently executing tasks
		       // Wait a while for tasks to respond to being cancelled
		       if (!executor.awaitTermination(5, TimeUnit.SECONDS))
		           System.err.println("Pool did not terminate");
		     }
		   } catch (InterruptedException ie) {
		     // (Re-)Cancel if current thread also interrupted
			   executor.shutdownNow();
		     // Preserve interrupt status
		     Thread.currentThread().interrupt();
		   }
	
	}
	
	public String login(String username, String password) throws IOException{
		
		String messageToServer= "!login " + username + " " + password;
		log.info(messageToServer);
		
		writer.println(messageToServer);
		
		//try1
		String ret = reader.readLine();	//check if login was successfully
		
		if(ret.startsWith("Successfully")){
			log.info("Loing successfully");
			this.responseHandler = new ClientResponseHandler(reader);
			executor.execute(responseHandler);
			//t_MessageChecker = new Thread(responseHandler);
			//t_MessageChecker.start();
			this.loggedIn = true;
			return ret;
		}
		
		return ret;
			
		//start thread to check incoming responses from server TODO DONE
		
		//return reader.readLine();	
	}
	
	public String logout() throws IOException{
		
		String messageToServer= "!logout";
		log.info(messageToServer);
		writer.println(messageToServer);
		
		//check if there some return string waiting
		String s = this.responseHandler.getResult();
		if(s.startsWith("Successfully"))
			this.loggedIn  = false;		//logged out -> stop waitung for message from server
		//TODO check if successful logged out
		return s;
		//return reader.readLine();	
	}
	
	public String msg(String username, String message){
		
		
		return null;
	}
	
	public String lastMsg(){
		//TODO implement
		return null;
	}
	
	public String send(String message) throws IOException{
		
		String messageToServer= "!send " + message;
		writer.println(messageToServer);
		return null;
		//return  reader.readLine();
	}
	
	public String lookup(){
		//TODO implement
		return null;
	}
	
	public String register(){
		//TODO implement
		return null;
	}
	
	
	@Override
	public void run() {
		
		log.info("Client HandlerTCP running");
		
		//start Streams
		try {
			
			
		
			
			while(!this.isClosed){
					
				/*
					//try1 			
					if(this.loggedIn){					//only accept incoming responses from server if logged in
						log.info("logged in: waiting for any messages");
						sendMessage = reader.readLine();
						log.info("received any message -> add to queue");
						this.responseHandler.add(sendMessage);
					}
				*/			
			}
		
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
