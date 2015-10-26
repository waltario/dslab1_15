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

import util.Config;


public class Chatserver implements IChatserverCli, Runnable {
	
	private static final Logger log = Logger.getLogger(Chatserver.class.getName());

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
		
	//additional variables
	private Map<String,String> usersMap;
	private BufferedReader reader;
	private PrintWriter writer;
	
	private int tcpPort;
	private int udpPort;
	
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
		
		this.usersMap = new HashMap<String,String>();
		this.reader = new BufferedReader(new InputStreamReader(userRequestStream));
		this.writer = new PrintWriter(userResponseStream);
	
		

	}

	@Override
	public void run() {
		
		//start thread to listen for incoming tcp requests
		Thread chatServerListenerTCP = new Thread(new ChatServerListenerTCP(this.config));
		chatServerListenerTCP.start();
		log.info("Server TCP Listener started");
		
		//start thread to listen for incoming udp request
		Thread chatServerListenerUDP = new Thread(new ChatServerListenerUDP(this.config));
		chatServerListenerUDP.start();
		log.info("Server UDP Listener started");
		
	}

	@Override
	public String users() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	/** readAllUsersFromProperties()
	 *  reads username / password from user.properties and add to usersMap data-structure
	 * 
	 */
	public void readAllUsersFromProperties(){
		
		Set<String> userNames = new HashSet<String>();
		Config userConfig = new Config("user");
		userNames = userConfig.listKeys();
		
		Iterator iterator = userNames.iterator();
		while(iterator.hasNext()){
			
			String username = (String) iterator.next();
			String password = userConfig.getString(username);
			this.usersMap.put(username, password);		
			
			//log.info("user name user.properties: " + username + " password: " + password);
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
