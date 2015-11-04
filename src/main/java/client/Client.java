package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import cli.Command;
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
	
	//config vars
	private String chatserver_name;
	private int chatserver_tcp_port;
	private int chatserver_udp_port;
	
	private boolean isLoggedIn;
	private String lastMessage;
	
	//TCP Var
	private HandlerTCP handlerTCP;
	private Socket clientSocket;
	private Thread t_clientSocket;
	
	//UDP Var
	private ClientHandlerUDP clientHandlerUDP;
	private Thread t_handlerUDP;
	
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
		
		log.setLevel(Level.OFF);
		
		this.clientSocket = null;		
		this.t_clientSocket = null;		
		this.handlerTCP = null;
		this.lastMessage = null;
		this.clientHandlerUDP = null;
		
		//register shell
		this.shell = new Shell(componentName, userRequestStream, userResponseStream);
		this.shell.register(this);
		new Thread(shell).start(); //start shell thread 
		
		this.client_init();
		
	}
	
	public void client_init(){
		
		//read tcp, udp chatsevername from config
		this.chatserver_name = config.getString("chatserver.host");
		this.chatserver_tcp_port = config.getInt("chatserver.tcp.port");
		this.chatserver_udp_port = config.getInt("chatserver.udp.port");
		this.isLoggedIn = false;	// at start User is not logged in
	
		log.info("tcp_port: " + this.chatserver_tcp_port + " udp_port: " + this.chatserver_udp_port + "name: " + this.chatserver_name);
	
	}
	
	public boolean checkLogStatus(){
		return this.isLoggedIn;
	}

	@Override
	public void run() {
	
		//start UDP Handler at startup
		this.clientHandlerUDP = new ClientHandlerUDP();
		this.t_handlerUDP = new Thread(clientHandlerUDP);	
		this.t_handlerUDP.start();
		
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		
		
		if(this.checkLogStatus())
			return "User " + username + " already logged in.";
		
		//create new socket and forward to thread, start thread and perfom login 
				try {
					this.clientSocket = new Socket(chatserver_name, chatserver_tcp_port);
				} catch (UnknownHostException e) {
					return "### ERROR Chat-Sever offline ";
				} catch (IOException e) {
					return "### ERROR - Chat-Sever offline ";
				}
				this.handlerTCP = new HandlerTCP(this.clientSocket);
				this.t_clientSocket = new Thread(this.handlerTCP);
				this.t_clientSocket.start();
		
		String serverMessage = handlerTCP.login(username, password);
		log.info("return value : servermessage : " +serverMessage);
		if(serverMessage.startsWith("Successfully")){	
			this.isLoggedIn = true;				//set client as logged in to be able to send all commands
			return serverMessage;
		}
		else{
		    log.info("login not successfull");	//login was not successful -> close TCP Conenction
			this.handlerTCP.close();
			return serverMessage;
		}
			
		
		
	}

	@Override
	@Command
	public String logout() throws IOException {
		
		if(!this.checkLogStatus())
			return "User is already logged out.";
				
		log.info("before sending logout");
		String s = handlerTCP.logout();				//send logout request to sever
		this.isLoggedIn = false;					//set client as logged out
		handlerTCP.close();							//close tcp c
		return s;
	}

	@Override
	@Command
	public String send(String message) throws IOException {
		//TODO implement
		
		if(!this.checkLogStatus())
			return "You must login to use !send command.";
		
		this.lastMessage = this.handlerTCP.send(message);
		return this.lastMessage;
	}

	@Override
	@Command
	public String list() throws IOException {
		return this.clientHandlerUDP.list(this.chatserver_name,this.chatserver_udp_port);
	}

	@Override
	@Command
	public String msg(String username, String message) throws IOException {
		//TODO implement
		
		if(!this.checkLogStatus())
			return "You must login to use !msg command.";
		
		return this.handlerTCP.msg(username, message);
	}

	@Override
	@Command
	public String lookup(String username) throws IOException {
		//TODO implement
		if(!this.checkLogStatus())
			return "You must login to use !lookup command.";
		
		return this.handlerTCP.lookup(username);
	}

	@Override
	@Command
	public String register(String privateAddress) throws IOException {
		//TODO implement
		if(!this.checkLogStatus())
			return "You must login to use !register command.";
		
		//return this.handlerTCP.register(privateAddress + ":" + Integer.toString(this.tcp_port_count++));
		return this.handlerTCP.register(privateAddress);
	}
	
	@Override
	@Command
	public String lastMsg() throws IOException {
		
		if(!this.checkLogStatus())
			return "You must login to use !lastMsg command.";
			
		return this.handlerTCP.lastMsg();
	}

	@Override
	@Command
	public String exit() throws IOException {
	
		if(this.handlerTCP != null)
			this.handlerTCP.close();
		if(this.clientHandlerUDP != null)
			this.clientHandlerUDP.close();
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
