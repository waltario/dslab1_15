package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandlerUDP implements Runnable{

	private static final Logger log = Logger.getLogger(ClientHandlerUDP.class.getName());
	
	private InetAddress ia;
	private int udpPort;
	private DatagramPacket packet;
	private DatagramSocket datagramSocket;
	byte[] data;
	private boolean isClosed = false;
	
	
	public ClientHandlerUDP(){
		log.setLevel(Level.OFF);
	}
	
	public ClientHandlerUDP(int udpPort,String ia) {
		log.setLevel(Level.OFF);
		this.udpPort = udpPort;
		this.datagramSocket = null;
		this.packet = null;
		this.ia = null;
		
		try {
			
			this.ia = InetAddress.getByName(ia);
		
		} catch (UnknownHostException e) {
		}
	
	}

	//### commmands ###
	
	public String list(){
		
		//send udp paket to server
		String sendToServer = "!list";
		data = sendToServer.getBytes();
		packet = new DatagramPacket(data, data.length, this.ia, this.udpPort);
		try{
		datagramSocket = new DatagramSocket();
		datagramSocket.send(packet);
		 
		datagramSocket.setSoTimeout(2000);   	//if datagarmsocket.receive is 2s inactive -> Exception -> if sever is not online anymore
		
		//wait for server response
		data = new byte[ 1024 ];
		DatagramPacket packet = new DatagramPacket(data,data.length);
		
			this.datagramSocket.receive( packet );
	    } catch (IOException e) {
	    	return "### ERROR - Timeout UDP Sending, Server might be offline";
	    }
		//create string from udp data
		String ret = new String (packet.getData(),0, packet.getLength());
		
		return ret;
	}
	
	public String list(String ia, int udpPort) throws IOException{
		
		log.info("ia name: " + ia + " udpPort: " + udpPort);
		
		//send udp paket to server
		String sendToServer = "!list";
		data = sendToServer.getBytes();
		packet = new DatagramPacket(data, data.length, InetAddress.getByName(ia), udpPort);
		datagramSocket = new DatagramSocket();
		datagramSocket.send(packet);
		log.info("udp packet sended");
		
		//wait for server response
		data = new byte[ 1024 ];
		DatagramPacket packet = new DatagramPacket(data,data.length);
		log.info("udp packet wainting");
		this.datagramSocket.receive( packet );
		log.info("udp packet received");
		//create string from udp data
		String ret = new String (packet.getData(),0, packet.getLength());
		log.info("ret: string" + ret);
		return ret;
	}
	
	
	
	//### setters ###
	
	public void setUDPPort(int udpPort){
		this.udpPort = udpPort;
	}
	
	public void close(){
		
		this.isClosed = true;
		if(datagramSocket !=null){		
			datagramSocket.close();
		}
		
	}
	
	public void setIA(String ia){
		
		try {
		
			this.ia = InetAddress.getByName(ia);
		
		} catch (UnknownHostException e) {
		}
	}
	
	@Override
	public void run() {
		
		log.info("Client HandlerUDP running");
		
		while(!isClosed){
			
		}
		
	}
	
	
	
	
	
	
}
