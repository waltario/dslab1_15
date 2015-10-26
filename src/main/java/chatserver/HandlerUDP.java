package chatserver;

import java.net.DatagramPacket;

public class HandlerUDP implements Runnable{

	private DatagramPacket packet;
	
	public HandlerUDP(DatagramPacket packet) {
		this.packet = packet;
	}
	
	@Override
	public void run() {
		
		String request = new String(packet.getData());

		System.out.println("Received request-packet from client: "
				+ request);
	}
	
	
}
