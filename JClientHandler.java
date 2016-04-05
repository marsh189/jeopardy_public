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
	public boolean myTurn = true;
	public String receivedMessage;
	String[] names = new String[4];
	boolean gameStart = false;

	JClientHandler(Socket sock, ArrayList<Socket> socketList, ArrayList<String> ans, ArrayList<String> q, String[] nameArr)
	{
		this.connectionSock = sock;
		this.socketList = socketList;	// Keep reference to master list
		this.answers = ans;
		this.questions = q;
		this.names = nameArr;
	}

	public void run()
	{
        		// Get data from a client and Starts the game
		try
		{
			int clientNum = socketList.size();
			System.out.println("Connection made with Client number " + clientNum);
			DataOutputStream welcomeClient = new DataOutputStream(socketList.get(clientNum - 1).getOutputStream());
			welcomeClient.writeBytes(clientNum  + names[clientNum - 1] + "\n");
			System.out.println("Name: " + names[clientNum-1]);
			if(clientNum == 4)
			{
				welcomeClient.writeBytes(4 + "\n");
			}
			if(clientNum == (names.length - 1))
			{
				String start = "All Clients have joined. It is time to play JEAPORDY! \n";
				for (Socket s : socketList)
				{
					DataOutputStream clientOutput = new DataOutputStream(s.getOutputStream());
					clientOutput.writeBytes(start + "\n");
				}
				System.out.println(start);

				SendAnswer();
			}
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

		int randomNum = (int)(Math.random() * answers.size());
		for (Socket s : socketList)
		{
			DataOutputStream clientOutput = new DataOutputStream(s.getOutputStream());
			clientOutput.writeBytes("Answer: " + answers.get(randomNum) + "\n");
		}
		System.out.println("Answer: " + answers.get(randomNum));
		GetResponse(randomNum);
	}

	public void GetResponse(int num) throws Exception
	{
		gameStart = true;
		while(myTurn)
		{
			if(gameStart)
			{
				BufferedReader clientInput = new BufferedReader(
					new InputStreamReader(connectionSock.getInputStream()));
				receivedMessage = clientInput.readLine();
			}
		}	
	}

	public void SendMessage(String message) throws Exception
	{
		DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());
		clientOutput.writeBytes(message + "\n");
	}

} // ClientHandler for JServer.java
