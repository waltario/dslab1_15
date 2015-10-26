package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HandlerTCP implements Runnable{

	private Socket tcpSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean isDown;
	
	
	public HandlerTCP(Socket tcpSocket) {
		this.tcpSocket = tcpSocket;
		this.isDown = false;
	}
	
	public void shutdown(){
		
		this.isDown = true;
		
	}
	
	@Override
	public void run() {
		
		try {
			this.writer = new PrintWriter(this.tcpSocket.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));
			
			while(!isDown){
				
				
			}
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
	}

}
