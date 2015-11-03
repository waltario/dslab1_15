package chatserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ChatServerData {

	private static final Logger log = Logger.getLogger(ChatServerData.class.getName());
	
	private static ChatServerData chatSeverDataSingleton;
													//TODO save to cuncurrent datatypes
	private Map<String,String> passwordMap;			//saves Username / Password of users
	private Map<String,String> usersMap;			//save online / offline status for users
	private List<HandlerTCP> clientList = null;		//save online TCP Connections, to send public messages and for shutdown
	private Map<String,String> registeredUsers;		//save users which have registerd
	
	private ChatServerData() {
		
		this.passwordMap = new ConcurrentHashMap<String,String>();
		this.usersMap = new ConcurrentHashMap<String,String>();	
		this.clientList = Collections.synchronizedList(new ArrayList<HandlerTCP>());	
		this.registeredUsers = new ConcurrentHashMap<String,String>();
	}
	
	/**  only used once at startup - read config file and save in users and passwords in map structure
	 * @param username
	 * @param password
	 */
	public void initUsers(String username, String password){
		this.passwordMap.put(username, password);	//save username and password
		this.usersMap.put(username, "offline");		//at init all users are offline, save username and online / offline status
	}
	
	/**
	 * @param username
	 * @param password
	 * @return true if username exists && password valid, false username doesnt exitst or password invalid
	 */
	public boolean checkLogin(String username, String password){
		
		String userPassword = null;
		
		if(this.passwordMap.containsKey(username)){
			userPassword  = this.passwordMap.get(username);
			
			if (password.matches(userPassword)){
				this.setUserOnline(username);
				return true;
			}
				
		}
		
		return false;	
	}
	
	
	public boolean logout(String username){
		
		usersMap.put(username, "offline");
		return true;
		//get saved tcpHandler connection for user and remove from list and shut down
		//HandlerTCP Connection Name has to be unique
		/*int positionTCPHandler = -1;
		for(HandlerTCP hTCP : this.clientList){
			if(hTCP.getName().matches(username)){
				positionTCPHandler = this.clientList.indexOf(hTCP);
				log.info("Postion TCPHandler logout: " + positionTCPHandler);
			}
		}
		//remove index element from list if found
		if(positionTCPHandler != -1){
			HandlerTCP hTCPRemoved = this.clientList.remove(positionTCPHandler);
			//hTCPRemoved.shutdown();		//close TCP Connection
			//log.info("successfully removed tcp connection and close it");
			return true;
		}
		else 
			return false;
		*/
	}
	
	public boolean removeTCPHandler(String name){
		
		int positionTCPHandler = -1;
		for(HandlerTCP hTCP : this.clientList){
			if(hTCP.getName().matches(name)){
				positionTCPHandler = this.clientList.indexOf(hTCP);
				log.info("Postion TCPHandler logout: " + positionTCPHandler);
			}
		}
		//remove index element from list if found
		if(positionTCPHandler != -1){
			
			HandlerTCP hTCPRemoved = this.clientList.remove(positionTCPHandler);
			log.info("HAndlerTCP removed: " + hTCPRemoved.getName() );
			log.info("Size after removing: " + this.clientList.size());
			//hTCPRemoved.shutdown();		//close TCP Connection
			//log.info("successfully removed tcp connection and close it");
			return true;
		}
		else 
			return false;
	}
	
	public void setUserOnline(String username){
		this.usersMap.put(username, "online");
	}
	
	public void setUserOffline(String username){
		this.usersMap.put(username, "offline");
	}
	
	
	//### private Message commands ###
	
	public String lookup(String username){
		
		log.info("look up: " + username);
		String ipAdress;
		
		if(this.registeredUsers.containsKey(username)){
			ipAdress  = this.registeredUsers.get(username);
			log.info("ip: " + ipAdress);
			return ipAdress;
		}
		
		return "Wrong username or user not reachable.";	
		
	}
	
	public boolean register(String ip,String username){
		
		if(this.registeredUsers.containsKey(username))
			return false; 
		else{
			this.registeredUsers.put(username, ip);
			return true;
		}
	}
	
	
	public List<HandlerTCP> getHandlerTCPList(){
		if(this.clientList.isEmpty())
			return null;
		else
			return this.clientList;
	}
	public void addTCPHandler(HandlerTCP item){
		clientList.add(item);
	}
	

	
	public void addUser(String username, String password){
		this.passwordMap.put(username, password);	//save username and password
	}
	

	
	public String getAllUsers(){
	
		String allUserListStatus = "";
		for(Map.Entry<String, String> entry : this.usersMap.entrySet()){
			allUserListStatus += entry.getKey();
			allUserListStatus += " ";
			allUserListStatus += entry.getValue();
			allUserListStatus += "\n";
		}
		
		return allUserListStatus;
	}
	
	public String getAllRegUsers(){
		
		String allUserListStatus = "";
		for(Map.Entry<String, String> entry : this.registeredUsers.entrySet()){
			allUserListStatus += entry.getKey();
			allUserListStatus += " ";
			allUserListStatus += entry.getValue();
			allUserListStatus += "\n";
		}
		
		return allUserListStatus;
	}
	
	
	public List<String> getAllOnlineUsers(){
		
		List onlineUsers = new ArrayList<String>();
		for(Map.Entry<String, String> entry : this.usersMap.entrySet()){
			if(entry.getValue().equals("online"))
				onlineUsers.add(entry.getKey());
		}
		
		return onlineUsers;
	}
	
	public List<HandlerTCP> getAllOnlineTCPHandler(String sender){
		
		List<HandlerTCP> handler = new ArrayList<HandlerTCP>();
		for(HandlerTCP item : this.getHandlerTCPList()){
			for(String htcp : this.getAllOnlineUsers()){
			
					if(item.getName().equals(htcp) && !item.getClass().equals(sender)){
						handler.add(item);
					}
			}
		}
		log.info(" " + handler.size() + " online HandlerTCP at the moment");
		return handler;
	}
	
	
	public Map<String,String> sortUsers(){
		//TODO return a string that is sorted in alphabetic order for username
		return null;
	}

	
	
	/**
	 * @return an singleton of the ChatServerData 
	 */
	public static ChatServerData getChatSeverDataSingleton(){
		
		if( chatSeverDataSingleton == null)
			chatSeverDataSingleton = new ChatServerData();
		
		return chatSeverDataSingleton;
	}
	
}
