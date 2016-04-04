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
	private ArrayList<String> answers;
	private ArrayList<String> questions;
	String[] names = new String[4];
	boolean gameStart = false;
	boolean firstBuzzed = false;

	JClientHandler(Socket sock, ArrayList<Socket> socketList, ArrayList<String> ans, ArrayList<String> q)
	{
		this.connectionSock = sock;
		this.socketList = socketList;	// Keep reference to master list
		this.answers = ans;
		this.questions = q;
	}

	public void run()
	{
        		// Get data from a client and send it to everyone else
		try
		{
			int clientNum = socketList.size();
			System.out.println("Connection made with Client number " + clientNum);
			BufferedReader clientInput = new BufferedReader(
				new InputStreamReader(connectionSock.getInputStream()));

			DataOutputStream welcomeClient = new DataOutputStream(socketList.get(clientNum - 1).getOutputStream());
			names[clientNum -1] = clientInput.readLine();
			welcomeClient.writeBytes(clientNum  + names[clientNum - 1] + "\n");
			System.out.println("Name: " + names[clientNum-1]);
			if(clientNum == (names.length - 1))
			{
				String start = "All Clients have joined it is time to play JEAPORDY! \n";
				for (Socket s : socketList)
				{
					DataOutputStream clientOutput = new DataOutputStream(s.getOutputStream());
					clientOutput.writeBytes(start + "\n");
				}
				System.out.println(start);
				gameStart = true;

				SendAnswer();
			}
			// while (true)
			// {
			// 	// Get data sent from a client
			// 	String clientText = clientInput.readLine();
			// 	if (clientText != null)
			// 	{
			// 		System.out.println("Received: " + clientText);
			// 		// Turn around and output this data
			// 		// to all other clients except the one
			// 		// that sent us this information
			// 		for (Socket s : socketList)
			// 		{
			// 			if (s != connectionSock)
			// 			{
			// 				DataOutputStream clientOutput = new DataOutputStream(s.getOutputStream());
			// 				clientOutput.writeBytes(clientText + "\n");
			// 			}
			// 		}
			// 	}
			// 	else
			// 	{
			// 	  // Connection was lost
			// 	  System.out.println("Closing connection for socket " + connectionSock);
			// 	   // Remove from arraylist
			// 	   socketList.remove(connectionSock);
			// 	   connectionSock.close();
			// 	   break;
			// 	}
			// }
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			socketList.remove(connectionSock);
		}
	}

	public void SendAnswer() throws Exception
	{
		if(gameStart)
		{
			int randomNum = (int)(Math.random() * (answers.size() + 1));
			for (Socket s : socketList)
			{
				DataOutputStream clientOutput = new DataOutputStream(s.getOutputStream());
				clientOutput.writeBytes("Answer: " + answers.get(randomNum) + "\n");
			}
			System.out.println("Answer: " + answers.get(randomNum));
			GetResponse(randomNum);
		}
	}

	public void GetResponse(int num) throws Exception
	{
		while(firstBuzzed == false)
		{

		}
	}

} // ClientHandler for JServer.java
