/**
 * JClientHandler.java
 *
 * This class handles communication between the client
 * and the server.  It runs in a separate thread but has a
 * link to a common list of sockets to handle broadcast.
 *
 * Once a client connects, a name for each client is saved to an array.
 * There can only be three clients. If a fourth connects, they are disconnected.
 *
 */
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

//All code in this file was written by both Haley and Matt
public class JClientHandler implements Runnable
{
	private Socket connectionSock = null;
	private ArrayList<Socket> socketList;
	private ArrayList<String> names;

	public int myIndex;
	public String myName;
	public String receivedMessage;
	public boolean canBuzzIn = true; //If true, Client is able to buzz in
	public boolean canAnswer = false;

	JClientHandler(Socket sock, ArrayList<Socket> socketList, ArrayList<String> nameArr)
	{
		this.connectionSock = sock;
		this.socketList = socketList;	// Keep reference to master list
		this.names = nameArr;
	}

	public void run()
	{
        // Get data from a client and Starts the game
		try
		{
			
			int clientNum = socketList.size();
			myIndex = clientNum - 1;
			System.out.println("Connection made with Client number " + clientNum);
			System.out.println("Name: " + names.get(clientNum-1));
			myName = names.get(clientNum-1);
			if(clientNum == 4)
			{
				SendMessage("4");
			}

			//waits for something to be entered as a buzz in
			while(canBuzzIn)
			{
				BufferedReader clientInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
				receivedMessage = clientInput.readLine();

				if(receivedMessage != null)
				{
					canBuzzIn = false; //changed
				}

				else
				{
			 		 // Connection was lost
			  		System.out.println("Closing connection for socket " + connectionSock);
			   		
			   		// Remove from arraylist
			  		socketList.remove(connectionSock);
			  		connectionSock.close();
			   		break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			socketList.remove(connectionSock);
		}
	}
	
	//Receives a response from the contestant
	public String GetResponse() throws Exception
	{

		BufferedReader cInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
		while(canAnswer)
		{
			String input = cInput.readLine();

			if(input != null)
			{
				return input;	
			}
		}
		return "";
	}

	//Sends a message to the contestant
	public void SendMessage(String message) throws Exception
	{
		DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
		clientOutput.writeBytes(message + "\n");
	}

} // JClientHandler for JServer.java
