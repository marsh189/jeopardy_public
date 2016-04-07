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

public class JClientHandler implements Runnable
{
	private Socket connectionSock = null;
	private ArrayList<Socket> socketList;
	public boolean myTurn = true;
	public boolean gameStart = false;
	public String receivedMessage;
	ArrayList<String> names;

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
			System.out.println("Connection made with Client number " + clientNum);
			System.out.println("Name: " + names.get(clientNum-1));
			//SendMessage(clientNum  + names.get(clientNum - 1));
			if(clientNum == 4)
			{
				SendMessage("4");
			}

			BufferedReader clientInput = new BufferedReader(
						new InputStreamReader(connectionSock.getInputStream()));
			while(true)
			{
				if(myTurn)
				{
					String input = clientInput.readLine();

					if(input != null)
					{
						for(Socket s : socketList)
						{
							if(s != connectionSock)
							{
								DataOutputStream clientOutput = new DataOutputStream(s.getOutputStream());
								clientOutput.writeBytes("Client " + socketList.indexOf(connectionSock) + ":" + input);
							}
						}
						myTurn = false;
						receivedMessage = input;
						break;
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
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			socketList.remove(connectionSock);
		}
	}


	public void GetResponse() throws Exception
	{
	
	}

	public void SendMessage(String message) throws Exception
	{
		DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
		clientOutput.writeBytes(message + "\n");
	}

} // ClientHandler for JServer.java
