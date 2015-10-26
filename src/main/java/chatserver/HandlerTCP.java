package chatserver;

import java.net.Socket;

public class HandlerTCP implements Runnable{

	Socket tcpSocket;
	
	
	public HandlerTCP(Socket tcpSocket) {
		this.tcpSocket = tcpSocket;
	}
	
	@Override
	public void run() {
		
		
	}

}
