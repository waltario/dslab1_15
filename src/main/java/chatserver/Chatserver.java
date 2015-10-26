package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import cli.Command;
import cli.Shell;
import util.Config;


public class Chatserver implements IChatserverCli, Runnable {
	
	private static final Logger log = Logger.getLogger(Chatserver.class.getName());

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	
	private Shell shell;
	
	//additional variables
	private Map<String,String> passwordMap;	//saves Username / Password
	private Map<String,String> usersMap;	//save online / offline status for users

	private int tcpPort;
	private int udpPort;
	
	private ChatServerListenerTCP chatServerListenerTCP;
	private ChatServerListenerUDP chatServerListenerUDP;
 	
	Thread t_chatServerListenerTCP;
	Thread t_chatServerListenerUDP;
	
	/**
	 * @param componentName
	 *            the name of the component - represented in the prompt
	 * @param config
	 *            the configuration to use
	 * @param userRequestStream
	 *            the input stream to read user input from
	 * @param userResponseStream
	 *            the output stream to write the console output to
	 */
	public Chatserver(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		
		//register shell
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		
		//start shell thread 
		new Thread(shell).start();;
		
		this.passwordMap = new HashMap<String,String>();
		this.usersMap = new HashMap<String,String>();
		
	}

	@Override
	@Command
	public void run() {
		
		
		//create listener for TCP and UDP
		this.chatServerListenerTCP = new ChatServerListenerTCP(this.config);
		//this.chatServerListenerUDP = new ChatServerListenerUDP(this.config);
		
		//create new Theads for TCP and UDP
		this.t_chatServerListenerTCP = new Thread(this.chatServerListenerTCP);
		//this.t_chatServerListenerUDP = new Thread(this.chatServerListenerUDP);
		
		//start Threads
		this.t_chatServerListenerTCP.start();
		//this.t_chatServerListenerUDP.start();
		log.info("TCP and UDP Listener started");
		
		
	}

	@Override
	@Command
	public String users() throws IOException {
		// TODO check with Unix compatible
		// TODO not in alphabetic order at the moment
		
		String users_status = "";
		for(Map.Entry<String, String> entry : this.usersMap.entrySet()){
			users_status += entry.getKey();
			users_status += " ";
			users_status += entry.getValue();
			users_status += "\n";
		}
	
		return users_status;
	}

	@Override
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		
		chatServerListenerTCP.close();
		
		shell.close();
		
		return null;
	}

	
	
	/** readAllUsersFromProperties()
	 *  reads username / password from user.properties and add to passwordMap data-structure
	 * 
	 */
	public void readAllUsersFromProperties(){
		
		Set<String> userNames = new HashSet<String>();
		Config userConfig = new Config("user");
		userNames = userConfig.listKeys();
		
		Iterator iterator = userNames.iterator();
		while(iterator.hasNext()){
			
			String username_with_ending = (String) iterator.next();
			String username = username_with_ending.substring(0, username_with_ending.length()-9); // cut off ".password" from the end of string
			String password = userConfig.getString(username_with_ending);						  //with ending is with ".password"
			this.passwordMap.put(username, password);	//save username and password
			this.usersMap.put(username, "offline");		//at init all users are offline, save username and online / offline status
			
			
			log.info("user name user.properties: " + username + " password: " + password);
		}
		
		this.tcpPort = config.getInt("tcp.port");
		this.udpPort = config.getInt("udp.port");
	
		//log.info("tcp_port: " + this.tcpPort + " udp_port: " + this.udpPort );
	}
	
	/**
	 * @param args
	 *            the first argument is the name of the {@link Chatserver}
	 *            component
	 */
	public static void main(String[] args) {
		Chatserver chatserver = new Chatserver(args[0],
				new Config("chatserver"), System.in, System.out);
		
		//read username / password from user.properties
		chatserver.readAllUsersFromProperties();
		Thread chatServerTread = new Thread(chatserver);
		
		
		chatServerTread.run(); 
		
	
	}

}
