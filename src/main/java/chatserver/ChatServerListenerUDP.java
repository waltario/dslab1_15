package chatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import util.Config;

public class ChatServerListenerUDP implements Runnable{
	
	private static final Logger log = Logger.getLogger(ChatServerListenerUDP.class.getName());

	private DatagramSocket datagramSocket;
	private ExecutorService executor;
	private int tcpPort;
	private boolean isClosed;
	
	public ChatServerListenerUDP(int tcpPort) {
		this.tcpPort = tcpPort;
		datagramSocket = null;
		this.isClosed = false;
		executor = Executors.newFixedThreadPool(25);
	}

	public void close(){
		
		this.isClosed = true;
		executor.shutdown();
		
		if(datagramSocket !=null){		
			datagramSocket.close();
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
		
		log.info("ServerUDPListener running...");
		
		try {
			
			datagramSocket = new DatagramSocket(this.tcpPort);
			
			byte[] buffer;
			DatagramPacket packet;
			
				//while not shut down waiting for incoming udp packets -> hand over to FixedThreadPool
				while (!this.isClosed) {
					
					buffer = new byte[1024];
					packet = new DatagramPacket(buffer, buffer.length);
					log.info("waiting for incoming udp packet");
					datagramSocket.receive(packet);
					log.info("udp packet received");
					executor.execute(new HandlerUDP(packet));
				}
			
		} catch (SocketException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			if (datagramSocket != null && !datagramSocket.isClosed())
				datagramSocket.close();
		}
		
	}

}
