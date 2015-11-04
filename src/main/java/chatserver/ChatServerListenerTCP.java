package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;



public class ChatServerListenerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	ServerSocket serverSocket;
	private ExecutorService executor;
	private ChatServerData chatServerData;	//Singleton object to access central user data
	private int tcpPort;
	private boolean isClosed;
	
	public ChatServerListenerTCP(int tcpPort) {
		log.setLevel(Level.OFF);
		this.tcpPort = tcpPort;
		this.serverSocket = null;
		this.isClosed = false;
		this.chatServerData = ChatServerData.getChatSeverDataSingleton();
		executor = Executors.newFixedThreadPool(25);	
	}
	
	public void close(){
		
		this.isClosed = true;
		//close all open HandlerTCP connections 
		executor.shutdown();		
		if(this.chatServerData.getHandlerTCPList() != null){
			log.info("size of List HandlerTCP: " + this.chatServerData.getHandlerTCPList().size());
			/*for(HandlerTCP shutdownHandler : this.chatServerData.getHandlerTCPList()){	//shutdown all TCPHandler Connections to Clients
				shutdownHandler.shutdown();
			}*/
			List <HandlerTCP>  shutdownHandler	= this.chatServerData.getHandlerTCPList();
			for(int i =0; i < shutdownHandler.size() ; i++ ){
				shutdownHandler.get(i).shutdown();
			}
		}
			//dont accept any incoming threads
		
		if(serverSocket !=null && !serverSocket.isClosed()){		
			try {
				serverSocket.close();	
				
			} catch (IOException e) {
				
			}
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
		
		try {
			
			serverSocket = new ServerSocket(this.tcpPort);
			log.info("ChatServerListenerTCP established tcp connection");
			
			while(!this.isClosed){
				
				Socket client = serverSocket.accept();				//waiting for incoming tcp request from client
				HandlerTCP handlerTCP = new HandlerTCP(client);		//create new HandlerTCP
				executor.execute(handlerTCP);						//start new Thread
				//this.chatServerData.addTCPHandler(handlerTCP);
				
				log.info("new HandlerTCP created");
			}
			
		} catch (IOException e) {
			
		} finally {
			
		}
		
	}
	
	
	
}

