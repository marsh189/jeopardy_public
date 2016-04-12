/**
 * JClientListener.java
 *
 * This class runs on the client end and just
 * displays any text received from the server.
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
public class JClientListener implements Runnable
{
	private Socket connectionSock = null;
	String clientNum;

	JClientListener(Socket sock)
	{
		this.connectionSock = sock;
	}

	public void run()
	{
       	// Wait for data from the server.  If received, output it.
		try
		{
			BufferedReader serverInput = new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));

			//get the client number from the server
			boolean findClientNum = true;
			while(findClientNum)
			{
				String response = serverInput.readLine();
				String num = response.substring(0,1);
				String name = response.substring(1);

				//if contestant is the 4th to connect, terminate the connection
				if(num.equals("4"))
				{
					System.out.println("Server is full");
					System.exit(0);
				}
				else
				{
					findClientNum = false;
				}
			}
			while (true)
			{
				// Get data sent from the server
				String serverText = serverInput.readLine();
				if (serverInput != null)
				{
					System.out.println(serverText);
				}
				else
				{
					// Connection was lost
					System.out.println("Closing connection for socket " + connectionSock);
					connectionSock.close();
					break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.toString());
		}
	}
} // ClientListener for JClient
