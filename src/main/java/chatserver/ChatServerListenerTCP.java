package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


import util.Config;

public class ChatServerListenerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	ServerSocket serverSocket;
	private Config config;
	private ExecutorService executor;
	private List<HandlerTCP> clientList = null;
	
	
	public ChatServerListenerTCP(Config config) {
		this.config = config;
		this.clientList = new ArrayList<HandlerTCP>();
		executor = Executors.newFixedThreadPool(100);
		
	}
	
	public void close(){
		
		executor.shutdown();	//dont accept any incoming threads
		
		if(serverSocket !=null){
			try {
				serverSocket.close();
				
			} catch (IOException e) {
				
			}
		}
		
	}
	
	
	@Override
	public void run() {
		
		try {
			
			serverSocket = new ServerSocket(this.config.getInt("tcp.port"));
			log.info("ChatServerListenerTCP established tcp connection");
			
			while(true){
				
				//tcphandler added to ArrayList and started a new Thread
				Socket client = serverSocket.accept();
				HandlerTCP handlerTCP = new HandlerTCP(client);
				executor.execute(handlerTCP);
				this.clientList.add(handlerTCP);
				
				log.info("new HandlerTCP created");
			}
			
		} catch (IOException e) {
			
			log.severe("Not able to establis tcp coonection on port" + this.config.getInt("tcp.port"));
			//e.printStackTrace();
		}
		
		
	}
	
	
	
}

