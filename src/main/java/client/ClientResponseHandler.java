package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;


public class ClientResponseHandler implements Runnable {

	private static final Logger log = Logger.getLogger(ClientResponseHandler.class.getName());
	
	//private Queue queue;
	private boolean isClosed;
	private int count;
	private String message;
	private String retMessage;
	private boolean isMessage;
	private BufferedReader reader;
	
	public ClientResponseHandler(BufferedReader reader) {
		
		this.reader = reader;
		//this.queue = new LinkedList<String>();
		this.isClosed = false;
		count=0;
		this.isMessage = false;
	}
	
	
	public String  getResult(){
		while(!this.isMessage){
			
		}
		this.isMessage = false;
		return this.retMessage;
	}
	
	@Override
	public void run() {
		
		
		//if not shutted down
		while(!this.isClosed){
			
			
			try {
				
				this.message = this.reader.readLine();
				if(this.message == null)
					break;
				
				log.info("message returned: " + this.message);
				
				boolean test1 = message.substring(0,3).matches("\\d+");
				boolean test2 = this.message.startsWith("Successfully");
				boolean test3 = this.message.startsWith("Wrong");
				boolean test4 = this.message.startsWith("###");
				//TODO Add more response strings
				
				if( test1 || test2 || test3 || test4){
				
					this.retMessage = this.message;
					this.isMessage = true;
				}
				else{
					System.out.println(this.message);
				}
					
			} catch (IOException e) {
				
				
				//log.info("Exception: User was logged out -> closing thread for incoming responses");
				//e.printStackTrace();
			} finally {
				this.close();
			}
			
			
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
