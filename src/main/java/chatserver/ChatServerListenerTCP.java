package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import util.Config;

public class ChatServerListenerTCP implements Runnable{

	private static final Logger log = Logger.getLogger(ChatServerListenerTCP.class.getName());
	
	ServerSocket serverSocket;
	private Config config;
	private ExecutorService executor;
	
	
	public ChatServerListenerTCP(Config config) {
		this.config = config;
		executor = Executors.newFixedThreadPool(100);
	}
	
	@Override
	public void run() {
		
		try {
			
			serverSocket = new ServerSocket(this.config.getInt("tcp.port"));
			log.info("ChatServerListenerTCP established tcp connection");
			
			while(true){
				
				Socket client = serverSocket.accept();
				executor.execute(new HandlerTCP(client));
				log.info("new HandlerTCP created");
			}
			
		} catch (IOException e) {
			
			log.severe("Not able to establis tcp coonection on port" + this.config.getInt("tcp.port"));
			//e.printStackTrace();
		}
		
		
	}
	
}

