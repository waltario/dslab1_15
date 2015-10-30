package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import chatserver.ChatServerData;

public class ClientResponseHandler implements Runnable {

	private static final Logger log = Logger.getLogger(ClientResponseHandler.class.getName());
	
	private Queue queue;
	private boolean isClosed;
	private int count;
	private String message;
	private String retMessage;
	private boolean isMessage;
	private BufferedReader reader;
	
	public ClientResponseHandler(BufferedReader reader) {
		
		this.reader = reader;
		this.queue = new LinkedList<String>();
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
				
				boolean test1 = message.substring(0,3).matches("\\d+");
				boolean test2 = this.message.startsWith("Successfully");
				
				if( test1 || test2){
					this.isMessage = true;
					this.retMessage = this.message;
				}
				else{
					System.out.println(this.message);
				}
					
			} catch (IOException e) {
				
				this.isClosed = true;
				log.info("Exception: User was logged out -> closing thread for incoming responses");
				//e.printStackTrace();
			}
			
			
		}
		
		
		/*
		while(!this.isClosed){
			if(count>0){
				
				this.message = (String) queue.poll();
				count--;
				log.info("Message Received TCP Handler :" + this.message);
				
				boolean test1 = message.substring(0,2).matches("\\d+");
				boolean test3 = this.message.startsWith("Successfully");
				
				if( test1 ||  test3 ){
					//return value to user
					this.isClosed = true;
					count--;
				}
				else{
					count--;
					//print out imidiately locally
					System.out.println(this.message);
					
				}
				
			}
		}
		*/
	}
	
	
	/*
	public void add(String s){
		this.queue.offer(s);
		count++;
	}
	
	public String remove(){
		String s = (String) this.queue.poll();
		return s;	
	}
	*/
	//closed if user logs out
	public void close(){
		this.isClosed = true;
	}
	

}
