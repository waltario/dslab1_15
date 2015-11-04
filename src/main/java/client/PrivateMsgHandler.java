package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrivateMsgHandler implements Runnable {

	private static final Logger log = Logger.getLogger(PrivateMsgHandler.class.getName());
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	public PrivateMsgHandler(Socket socket) {
		log.setLevel(Level.OFF);
		this.socket = socket;
		init();
	
	}
	
	private void init(){
		//streams
		try {
			this.writer = new PrintWriter(socket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
	}

	/*
	 *	wait for incoming !msg command and return !ack
	 */
	@Override
	public void run() {
		log.info("PrivateMsgHandler running...");
		//expecting "!msg " + privateMessageString 
		String request = null;
		try {
			request = reader.readLine();
			log.info("request received" + request);
			writer.println("!ack");
			
			
		} catch (IOException e) {
			
		}finally {
			if(writer != null)
				writer.close();
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
				
				}
			try {
				if(this.socket != null && !socket.isClosed())
					this.socket.close();
				
			} catch (IOException e) {
				
			}
		}

		
	}

}
