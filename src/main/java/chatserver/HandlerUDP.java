package chatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

import client.ClientHandlerUDP;

public class HandlerUDP implements Runnable{

	private static final Logger log = Logger.getLogger(HandlerUDP.class.getName());
	
	private DatagramPacket packet;
	private DatagramSocket datagramSocket;
	private ChatServerData chatServerData;
	
	public HandlerUDP(DatagramPacket packet) {
		this.packet = packet;
		this.chatServerData = ChatServerData.getChatSeverDataSingleton();
	}
	
	@Override
	public void run() {
		log.info("new Sever UDP Handler running");
		
		//create request string from client
		String request = new String(packet.getData(),0, packet.getLength());
		InetAddress ia = packet.getAddress();	//get udp infos
		int udpPort = packet.getPort();
		
		//send udp packet back to client
		log.info("UDP request String from Client: " + request);
		
		String sendToServer = this.checkCommand(request);
		byte[] data = sendToServer.getBytes();
		packet = new DatagramPacket(data, data.length, ia, udpPort);
		
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.send(packet);
			
		} catch (IOException e) {
			e.printStackTrace();
		}	finally {
			if (datagramSocket != null && !datagramSocket.isClosed())
				datagramSocket.close();
		}
		
	
	}
	
	public String checkCommand(String message){
			
			String retMessage = "";
			
			if(message.startsWith("!list") && message.length() == 5){
				retMessage = chatServerData.getAllUsers();
			}
			else{
				retMessage = "ERROR";
			}
			return retMessage;
	}
	
}
