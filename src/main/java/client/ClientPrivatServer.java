package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientPrivatServer implements Runnable{

	private static final Logger log = Logger.getLogger(ClientPrivatServer.class.getName());
	
	private boolean isClosed;
	private ServerSocket serverPrivateSocket;
	private ExecutorService executor;
	private int tcp_port;
	private String ip;
	
	public ClientPrivatServer(String privateIPAdress) {
		log.setLevel(Level.OFF);
		
		this.isClosed = false;
		this.serverPrivateSocket = null;
		
		this.ip = privateIPAdress.substring(0,privateIPAdress.length()-6);
		this.tcp_port = Integer.valueOf(privateIPAdress.substring(privateIPAdress.length()-5));;
		
	}
	
	public void init(){
		
		try {
			this.executor = Executors.newFixedThreadPool(20); 
			this.serverPrivateSocket = new ServerSocket(tcp_port, 50, InetAddress.getByName(ip));
			//this.serverPrivateSocket = new ServerSocket(InetAddress.getByName("192.168.0.40"),50,this.tcp_port);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void close(){
		
		this.isClosed = true;
		executor.shutdown();			//dont accept any incoming threads
		
		try {
		
			if(this.serverPrivateSocket != null)
				this.serverPrivateSocket.close();
			
		
		} catch (IOException e) {
			
			//e.printStackTrace();
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
	
	@Override
	public void run() {
		
		init();
		
		log.info("ClientPrivateSever running...");
		
		while(!this.isClosed){
			
			//wait for incoming private Connection from Client
			Socket clientSocket=null;
			try {
				log.info("waiting for incoming privateTCP Request");
				clientSocket = this.serverPrivateSocket.accept();
				PrivateMsgHandler msgHandler = new PrivateMsgHandler(clientSocket);
				executor.execute(msgHandler);
				
			} catch (IOException e) {
				
				//e.printStackTrace();
			}
			
			log.info("new PrivateMsgHandler started");
		
		}
		
	}

}
