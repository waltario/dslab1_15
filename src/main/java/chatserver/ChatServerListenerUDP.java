package chatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import util.Config;

public class ChatServerListenerUDP implements Runnable{
	
	private static final Logger log = Logger.getLogger(ChatServerListenerUDP.class.getName());

	private Config config;
	private DatagramSocket datagramSocket;
	private ExecutorService executor;
	
	public ChatServerListenerUDP(Config config) {
		this.config = config;
		executor = Executors.newFixedThreadPool(100);
	}

	public void close(){
		
		executor.shutdown();
		datagramSocket.close();
	}
	
	@Override
	public void run() {
		
		try {
			
			datagramSocket = new DatagramSocket(config.getInt("udp.port"));
			
			byte[] buffer;
			DatagramPacket packet;
			
		
				while (true) {
					
					buffer = new byte[1024];
					packet = new DatagramPacket(buffer, buffer.length);
					datagramSocket.receive(packet);
					executor.execute(new HandlerUDP(packet));
				}
			
		} catch (SocketException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

}
