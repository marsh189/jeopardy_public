/**
 * JClient.java
 *
 * This program implements a simple multithreaded chat client.  It connects to the
 * server (assumed to be localhost on port 7654) and starts two threads:
 * one for listening for data sent from the server, and another that waits
 * for the user to type something in that will be sent to the server.
 * Anything sent to the server is broadcast to all clients.
 *
 * The JClient uses a JClientListener whose code is in a separate file.
 * The JClientListener runs in a separate thread, recieves messages form the server,
 * and displays them on the screen.
 * 
 *
 */
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;

public class JClient
{
	public static void main(String[] args)
	{
		System.out.println("*** WELCOME TO JEOPARDY ***");
		System.out.print("\nEnter your name: ");
		Scanner n = new Scanner(System.in);
		String name = n.nextLine(); 

		try
		{
			String hostname = "localhost";
			int port = 7654;

			System.out.println("Connecting to server on port " + port);
			Socket connectionSock = new Socket(hostname, port);

			DataOutputStream serverOutput = new DataOutputStream(connectionSock.getOutputStream());

			System.out.println("Connection made.");
			serverOutput.writeBytes(name + "\n");
			// Start a thread to listen and display data sent by the server
			JClientListener listener = new JClientListener(connectionSock);
			Thread theThread = new Thread(listener);
			theThread.start();

			// Read input from the keyboard and send it to everyone else.
			// The only way to quit is to hit control-c, but a quit command
			// could easily be added.
			Scanner keyboard = new Scanner(System.in);
			while (true)
			{
				String data = keyboard.nextLine();
				serverOutput.writeBytes(data + "\n");
			}
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
} // MTClient

