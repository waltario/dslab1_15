package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientResponseHandler implements Runnable {

	private static final Logger log = Logger.getLogger(ClientResponseHandler.class.getName());
	
	//private Queue queue;
	private boolean isClosed;
	private String message;
	private String retMessage;
	private boolean isMessage;
	private BufferedReader reader;
	private String lastPublicMessage;
	
	public ClientResponseHandler(BufferedReader reader) {
		log.setLevel(Level.OFF);
		
		this.lastPublicMessage = null;
		this.reader = reader;
		//this.queue = new LinkedList<String>();
		this.isClosed = false;
		this.isMessage = false;
	}
	
	
	public String  getResult(){	//get TCP response for request -> no public message
		while(!this.isMessage){	//no message received / detected
		}
		this.isMessage = false;	//activate for next message
		return this.retMessage;	//return to expected command
	}
	
	
	public String getLastPublicMessage(){
		return this.lastPublicMessage;
	}
	
	@Override
	public void run() {
		
		
			try {
				//if not shutted down
				while(!this.isClosed){
				
				this.message = this.reader.readLine();
				if(this.message == null){
					
					log.info("null was received");
					break;
				}
				log.info("message returned: " + this.message);
				
				boolean test1 = message.substring(0,3).matches("\\d+");		//ip adress response 192.xxx
				boolean test2 = this.message.startsWith("Successfully");	//success message response
				boolean test3 = this.message.startsWith("Wrong");			//wrong username 
				boolean test4 = this.message.startsWith("###");				//### error response
				//boolean test5 = this.message.startsWith("User");			//register response
				//TODO Add more response strings
				
				if( test1 || test2 || test3 || test4){		//TCP req/resp message
				
					this.retMessage = this.message;
					this.isMessage = true;
				}
				else{										//pulic message
					this.lastPublicMessage = this.message;
					System.out.println(this.message);
				}
					
				}
			} catch (IOException e) {
				
				this.close();
				//log.info("Exception: User was logged out -> closing thread for incoming responses");
				//e.printStackTrace();
			} finally {
				
			}

	
	}
	
	
	public void close() {
		this.isClosed = true;
		if(this.reader != null)
			try {
				this.reader.close();
			} catch (IOException e) {
			
			}
	}
	

}
