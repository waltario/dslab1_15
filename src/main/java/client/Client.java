package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import chatserver.Chatserver;
import cli.Shell;
import util.Config;

public class Client implements IClientCli, Runnable {

	private static final Logger log = Logger.getLogger(Client.class.getName());
	
	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	
	//shell
	private Shell shell;
	
	private String chatserver_name;
	private int chatserver_tcp_port;
	private int chatserver_udp_port;
	private boolean isLoggedIn;
	
	private Socket clientSocket;
	private Thread t_clientSocket;
	private HandlerTCP handlerTCP;
	
	
	
	//additional variables

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
	public Client(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		this.clientSocket = null;
		this.t_clientSocket = null;
		this.handlerTCP = null;
		
		//register shell
		this.shell = new Shell(componentName, userRequestStream, userResponseStream);
		this.shell.register(this);
		//start shell thread 
		new Thread(shell).start();;
		
		this.client_init();
		
	}
	
	public void client_init(){
		
		//read tcp, udp chatsevername from config
		this.chatserver_name = config.getString("chatserver.host");
		this.chatserver_tcp_port = config.getInt("chatserver.tcp.port");
		this.chatserver_udp_port = config.getInt("chatserver.udp.port=");
		
		this.isLoggedIn = false;
	
		log.info("tcp_port: " + this.chatserver_tcp_port + " udp_port: " + this.chatserver_udp_port + "name: " + this.chatserver_name);
	
	}
	
	public boolean checkLogStatus(){
		return this.isLoggedIn;
	}

	@Override
	public void run() {
		// TODO
		
		try {
			
			//create new socket and forward to thread
			this.clientSocket = new Socket(chatserver_name, chatserver_tcp_port);
			this.handlerTCP = new HandlerTCP(this.clientSocket);
			this.t_clientSocket = new Thread(this.handlerTCP);
			

		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}

	@Override
	public String login(String username, String password) throws IOException {
		
		if(this.checkLogStatus())
			return "User: " + username + " already logged in";
		
		String serverMessage = handlerTCP.login(username, password);
		this.isLoggedIn = true;
		return serverMessage;
		
	}

	@Override
	public String logout() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String send(String message) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String list() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String msg(String username, String message) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String lookup(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String register(String privateAddress) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String lastMsg() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		
		this.handlerTCP.close();
		this.clientSocket.close();
		shell.close();		
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		
		client.run();	//start client
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
