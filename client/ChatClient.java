// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  
  /**
   * The ID of current client. Used to differentiate the clients.
   * Stored as String
   */
  String loginID;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String host, int port, String loginID, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.loginID = loginID;
    this.clientUI = clientUI;
    try {
    	openConnection();
    }
    catch(IOException ex) {
    	clientUI.display("Cannot open connection! Awaiting command.");
    }
  }

  
  //Instance methods ************************************************

  /**
   * Hook method called after the connection has been closed. The default
   * implementation does nothing. The method may be overriden by subclasses to
   * perform special processing such as cleaning up and terminating, or
   * attempting to reconnect.
   */
   protected void connectionClosed() {
	     clientUI.display("Connection Closed");
   }

  /**
   * Hook method called each time an exception is thrown by the client's
   * thread that is waiting for messages from the server. The method may be
   * overridden by subclasses.
   * 
   * @param exception
   *            the exception raised.
   */
   protected void connectionException(Exception exception) {
	   	 clientUI.display("SERVER SHUTTING DOWN! DICONNECTING!");
	     clientUI.display("Abnormal termination of connection.");
   }   
	
	/**
	 * Hook method called after a connection has been established. The default
	 * implementation does nothing. It may be overridden by subclasses to do
	 * anything they wish.
	 */
	protected void connectionEstablished() {
		try {
		sendToServer("#login "+loginID);
		}
	    catch(IOException e)
	    {
	      clientUI.display
	        ("Could not send message to server.  Terminating client.");
	      quit();
	    }
	}
	
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
   public void handleMessageFromServer(Object msg) 
   {
     clientUI.display(msg.toString());
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
   private void handleCommandfromClientUI(String command) {
	   if(command.equals("#quit")) {
		   quit();
	   }
	   else if(command.equals("#logoff")) {
		   try{
			   closeConnection();
		   }
		   catch(IOException ex) {
			   clientUI.display("Logoff failed.");
		   }
	   }
	   else if(command.equals("#gethost")) {
		   clientUI.display(getHost());
	   }
	   else if(command.equals("#getport")){
		   clientUI.display(String.valueOf(getPort()));		   
	   }
	   else if(command.length()>9 && command.substring(0,8).equals("#sethost")) {
		   if(!isConnected()) {
		   setHost(command.substring(9));
		   System.out.println("Host set to: " + getHost());
		   }
		   else {
			   clientUI.display("Cannot change host while client is connected");
		   }
	   }
	   else if(command.length()>9 && command.substring(0,8).equals("#setport")) {
		   if(!isConnected()) {
		   try {
			   setPort(Integer.parseInt(command.substring(9)));
			   System.out.println("Port set to: " + getPort());
		   }
		   catch(NumberFormatException ex){
			   clientUI.display("The <port> parameter is invalid");
		   }
		   }
		   else {
			   clientUI.display("Cannot change port while client is connected");
		   }
	   }
	   else if(command.equals("#login")){
		   try {
			   openConnection();
		   }
		   catch(IOException ex) {
			   clientUI.display("Login failed");
		   }
	   }		  
	   else {
		   clientUI.display("The command is invalid");
	   }
    }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {
	if (isCommand(message)) {
		handleCommandfromClientUI(message);
		return;
	}
	  
    try
    {
      sendToServer(message);
    }
    catch(IOException e)
    {
      clientUI.display
        ("Could not send message to server.  Terminating client.");
      quit();
    }
  }
  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      if(isConnected())
      {
      closeConnection();
      }
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
//End of ChatClient class
