package chatserver;

import java.util.HashMap;
import java.util.Map;

public class ChatServerData {


	private static ChatServerData chatSeverDataSingleton;
	
	private Map<String,String> passwordMap;	//saves Username / Password
	private Map<String,String> usersMap;	//save online / offline status for users
	
	private ChatServerData() {
		
		this.passwordMap = new HashMap<String,String>();
		this.usersMap = new HashMap<String,String>();	
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
