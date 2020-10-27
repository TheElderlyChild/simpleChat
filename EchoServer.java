// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;

import common.ChatIF;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the server.
   */
  ServerConsole serverUI; 
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port, ServerConsole serverUI) 
  {
    super(port);
    this.serverUI=serverUI;
  }

  
  //Instance methods ************************************************
  
  /**
   * 
   *  This method handles commands from the client
   *  
   *  @param command The command from the client
   */
   private void handleCommandfromClient(String command, ConnectionToClient client) {
	   if(command.length()>7 && command.substring(0,6).equals("#login")) {
		   if(client.getInfo("login id")!=null){
			   try {
				   client.sendToClient("Error occured - "
				   		+ "Multiple login ids - terminating client");
				   client.close();   
			   }
			   catch(IOException ex) {}	   
		   }
		   client.setInfo("login id",command.substring(7));
		   String logonMsg=client.getInfo("login id") + " has logged on";
		   serverUI.display(logonMsg);
		   this.sendToAllClients(logonMsg);
	   }
	   else {
		   serverUI.display("The command is invalid");
	   }
   }
   
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
	System.out.println("Message received: " + msg + " from " + client.getInfo("login id"));	 
    
	if (isCommand(msg.toString())) {
		handleCommandfromClient(msg.toString(), client);
		return;
	}
	
	this.sendToAllClients(client.getInfo("login id") + "> " + msg);
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
    sendToAllClients("WARNING - The Server has stopped listening for connections");
  }
  
  /**
   * Hook method called each time a new client connection is
   * accepted. The default implementation does nothing.
   * @param client the connection connected to the client.
   */
  protected void clientConnected(ConnectionToClient client) {
	  System.out.println("A new client is attempting to connect to the server.");
  }

  /**
   * Hook method called each time a client disconnects.
   * The default implementation does nothing. The method
   * may be overridden by subclasses but should remains synchronized.
   *
   * @param client the connection with the client.
   */
  synchronized protected void clientDisconnected(
    ConnectionToClient client) {
	  this.sendToAllClients(client.getInfo("login id")+" has disconnected");
	  serverUI.display(client.getInfo("login id")+" has disconnected");
  }
  
  /**
   * Hook method called each time an exception is thrown in a
   * ConnectionToClient thread.
   * The method may be overridden by subclasses but should remains
   * synchronized.
   *
   * @param client the client that raised the exception.
   * @param Throwable the exception thrown.
   */
  synchronized protected void clientException(
    ConnectionToClient client, Throwable exception) {
	  serverUI.display(client.getInfo("login id")+" has disconnected");
	  this.sendToAllClients(client.getInfo("login id") + " has disconnected");
  }
  
  /**
   * 
   *  This method checks whether a message from the UI is a command
   *  
   *  @param command The message from the UI
   */
   private boolean isCommand(String message) {
	  if (message==null) return false;
	  if (message.equals("")) return false;
	  if (message.charAt(0)=='#') return true;
	  return false; 	
   }   
  
 /**
  * 
  *  This method handles commands from the UI
  *  
  *  @param command The command from the UI
  */
  private void handleCommandfromServerUI(String command) {
	   if(command.equals("#quit")) {
		    try
		    {
		      close();
		    }
		    catch(IOException e) {}
		    System.exit(0);
	   }
	   else if(command.equals("#stop")) {
		   stopListening();
	   }
	   else if(command.equals("#close")) {
		   try {
		   close();
		   }
		   catch(IOException ex) {serverUI.display("Unable to close connections");}
	   }
	   else if(command.equals("#start")) {
		   try {
			   listen();
		   }
		   catch(IOException ex) {serverUI.display("Unable to start listening");}
	   }
	   else if(command.equals("#getport")){
		   serverUI.display(String.valueOf(getPort()));	
	   }
	   
	   else if(command.length()>9 && command.substring(0,8).equals("#setport")) {
		   if(!isListening()){
		   try {
			   int newPort=Integer.parseInt(command.substring(9));
			   setPort(newPort);
			   System.out.println("Port set to: " + getPort());
		   }
		   catch(NumberFormatException ex){
			   serverUI.display("The <port> parameter is invalid");
		   }
		   }
		   else {
			   serverUI.display("Cannot change port while server is listening.");
		   }
	   }
	   else {
		   serverUI.display("The command is invalid");
	   }
   }
  
  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromServerUI(String message)
  {    
		if (isCommand(message)) {
			handleCommandfromServerUI(message);
			return;
		}
		this.sendToAllClients("SERVER MESSAGE> " + message);
	    serverUI.display("SERVER MESSAGE> " + message);
  }
  
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port, new ServerConsole(port));
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
}
//End of EchoServer class
