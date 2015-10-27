package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


import util.Config;

public class ChatServerListenerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	ServerSocket serverSocket;
	private Config config;
	private ExecutorService executor;
	private List<HandlerTCP> clientList = null;
	private boolean isClosed;
	
	
	public ChatServerListenerTCP(Config config) {
		this.config = config;
		this.serverSocket = null;
		this.isClosed = false;
		this.clientList = new ArrayList<HandlerTCP>();	//save all TCPHandler Connections to Clients -> needed for shtudown
		executor = Executors.newFixedThreadPool(100);	
		
	}
	
	public void close(){
		
		this.isClosed = true;
		executor.shutdown();			//dont accept any incoming threads
		
		if(serverSocket !=null){		
			try {
				serverSocket.close();	
				
			} catch (IOException e) {
				
			}
		}
		
		for(HandlerTCP shutdownHandler : this.clientList)	//shutdown all TCPHandler Connections to Clients
			shutdownHandler.shutdown();
		
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
		
		try {
			
			serverSocket = new ServerSocket(this.config.getInt("tcp.port"));
			log.info("ChatServerListenerTCP established tcp connection");
			
			while(!this.isClosed){
				
				Socket client = serverSocket.accept();				//waiting for incoming tcp request from client
				HandlerTCP handlerTCP = new HandlerTCP(client);		//create new HandlerTCP
				executor.execute(handlerTCP);						//start new Thread
				this.clientList.add(handlerTCP);					//add to managed client list
				
				log.info("new HandlerTCP created");
			}
			
		} catch (IOException e) {
			
			//log.severe("Not able to establis tcp coonection on port" + this.config.getInt("tcp.port"));
			//e.printStackTrace();
		}
		
		
	}
	
	
	
}

