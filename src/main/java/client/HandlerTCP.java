package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HandlerTCP implements Runnable {

	private static final Logger log = Logger.getLogger(HandlerTCP.class.getName());

	private PrintWriter writer;
	private BufferedReader reader;
	private Socket clientSocket;

	private ClientPrivatServer clientPrivateServer;
	private boolean isClosed;
	private boolean loggedIn;
	private boolean isRegistered;
	
	//privateMessge Vars
	private Socket privateSocket;
	private int tcp_port_private;
	
	//private Thread t_MessageChecker;
	private ExecutorService executor;
	private ExecutorService privateExecutor;
	private ExecutorService privateServerExecutor;
	private ClientResponseHandler responseHandler;
	
	public HandlerTCP(Socket clientSocket) {
		
		log.setLevel(Level.OFF);
	
		this.clientSocket = clientSocket;
		this.clientPrivateServer = null;
		this.writer = null;
		this.reader = null;
		this.privateSocket = null; 
		this.responseHandler = null;
		this.isClosed = false;
		this.loggedIn = false;
		this.isRegistered = false;
		executor = Executors.newFixedThreadPool(1);	
		privateExecutor = Executors.newFixedThreadPool(1);	
		privateServerExecutor = Executors.newFixedThreadPool(1);
		init();
		
		//private Message Vars
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
		privateExecutor.shutdown();
		privateServerExecutor.shutdown();
		
		if(this.clientPrivateServer != null)
			this.clientPrivateServer.close();
		if(writer != null)
			writer.close();
		try {
			if(reader != null)
				reader.close();
			if(clientSocket != null && !clientSocket.isClosed())
				log.info("try to close Handler TCP");
				clientSocket.close();
			if(this.responseHandler != null)
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
		//check if there some return string waiting
		//String ret = this.responseHandler.getResult();
		
		
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
		
		//do lookup to check if user has been registered
		String lookupMessage = this.lookup(username);
		if(lookupMessage.startsWith("Wrong"))
			return lookupMessage;
		else{
		//user is registered -> create new tcp connection to contact user/client
			log.info("lookup successfull");
			log.info("private ip adress client user: " + lookupMessage);
			//extract ip + port -> expected e.g. 192.168.0.2:4563
			
			String ip = lookupMessage.substring(0,lookupMessage.length()-6);
			String port = lookupMessage.substring(lookupMessage.length()-5);
			log.info("iA: "+ ip + " port: " + port); 
			//create new socket
			//TODO close private Socket and privateExecutor
			
			//this.privateSocket = new Socket(InetAddress.getByName(""), Integer.parseInt(port));
			try {
				this.privateSocket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
			} catch (NumberFormatException | IOException e) {
				return "Wrong username or user not reachable";
			}
			//this.privateSocket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
			HandlerPrivateTCP privateHandler =  new HandlerPrivateTCP(this.privateSocket);
			privateExecutor.execute(privateHandler);
			log.info("new socket and it runs -> message: " + message);
			if(privateHandler.msg(message)){
				privateHandler.close();
				privateExecutor.shutdown();
				return (username + " replied with !ack.");
			}
			else{
				privateHandler.close();
				privateExecutor.shutdown();
				return "Wrong username or user not reachable";
			}
				
		}
	}
	
	public String lastMsg(){
		//TODO implement
		return null;
	}
	
	public String send(String message) throws IOException{
		
		String messageToServer= "!send " + message;
		log.info("!send command sended");
		writer.println(messageToServer);
		return null;
		//return  reader.readLine();
	}
	
	public String lookup(String username){
		
		log.info("try to lookup: " + username);
		writer.println("!lookup " + username);
		return this.responseHandler.getResult();
	}
	
	public String register(String privateIPAdress){
	
		log.info("try to register " + privateIPAdress);
		
		writer.println("!register " + privateIPAdress); // send register command to server
		
		String s = this.responseHandler.getResult();	//wait for success message 
		log.info("resonse register: " + s);
		
		//wenn noch nicht registriert und erfolgreich die ip eingetragen -> start den serversocket für priv adressen
		if(!this.isRegistered && s.startsWith("Successfully")){
			
			log.info("Registered");
			//this.tcp_port_private = Integer.valueOf(privateIPAdress.substring(privateIPAdress.length()-5));	//extract tcp port from string
			//log.info("registered -> open serverSocket " + this.tcp_port_private);
			this.clientPrivateServer = new ClientPrivatServer(privateIPAdress);						//erzeuge Server					
			privateServerExecutor.execute(clientPrivateServer);												//start Thread
			this.isRegistered = true;
		}
		return s;
	}
	
	
	@Override
	public void run() {
		
		log.info("Client HandlerTCP running");
		
		try {
						
			while(!this.isClosed){
	
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
