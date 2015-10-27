package chatserver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ChatServerData {

	private static final Logger log = Logger.getLogger(ChatServerData.class.getName());
	
	private static ChatServerData chatSeverDataSingleton;
	
	private Map<String,String> passwordMap;	//saves Username / Password
	private Map<String,String> usersMap;	//save online / offline status for users
	
	private ChatServerData() {
		
		this.passwordMap = new HashMap<String,String>();
		this.usersMap = new HashMap<String,String>();	
		
	}
	
	public void initUsers(String username, String password){
		this.passwordMap.put(username, password);	//save username and password
		this.usersMap.put(username, "offline");		//at init all users are offline, save username and online / offline status
	}
	
	public void addUser(String username, String password){
		this.passwordMap.put(username, password);	//save username and password
	}
	
	public void setUserOnline(String username){
		this.usersMap.put(username, "online");
	}
	
	public void setUserOffline(String username){
		this.usersMap.put(username, "offline");
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
	
	public Map<String,String> sortUsers(){
		//TODO return a string that is sorted in alphabetic order for username
		return null;
	}
	
	/**
	 * @param username
	 * @param password
	 * @return true if username exists && password valid, false username doesnt exitst or password invalid
	 */
	public boolean checkLogin(String username, String password){
		
		String user_password = null;
		
		if(this.passwordMap.containsKey(username)){
			user_password  = this.passwordMap.get(username);
			
			if (password.matches(user_password)){
				this.setUserOnline(username);
				return true;
			}
				
		}
		
		return false;	
		
	}
	
	public boolean logout(String username){
		usersMap.put(username, "offline");
		return true;
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