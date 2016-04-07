/**
 * JServer.java
 *
 * This program implements a simple multithreaded chat server.  Every client that
 * connects to the server can broadcast data to all other clients.
 * The server stores an ArrayList of sockets to perform the broadcast.
 *
 * The JServer uses a JClientHandler whose code is in a separate file.
 * When a client connects, the JServer starts a JClientHandler in a separate thread 
 * to receive messages from the client.
 *
 *
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class JServer
{
	// Maintain list of all client sockets for broadcast
	private ArrayList<Socket> socketList;
	public int clientCount;
	private ArrayList<String> answers = new ArrayList<String>();
	private ArrayList<String> questions = new ArrayList<String>();
	ArrayList<String> names = new ArrayList<String>();

	ArrayList<JClientHandler> handlerList = new ArrayList<JClientHandler>();
	ArrayList<JClientHandler> canBuzzIn = new ArrayList<JClientHandler>();
	int state = 0;

	public JServer()
	{
		socketList = new ArrayList<Socket>();

	}

	private void getConnection()
	{
		// Wait for a connection from the client
		try
		{
			//read files for answers and questions
			Scanner ansScanner = new Scanner(new File("Answers.txt"));
			Scanner qScanner = new Scanner(new File("Questions.txt"));

			String aCurrent;
			String qCurrent;

			while (ansScanner.hasNextLine())
			{
				aCurrent = ansScanner.nextLine();
				qCurrent = qScanner.nextLine();
				answers.add(aCurrent);
				questions.add(qCurrent);
			}

			System.out.println("Waiting for client connections on port 7654.");
			ServerSocket serverSock = new ServerSocket(7654);
			// This is an infinite loop, the user will have to shut it down
			// using control-c
			while (true)
			{
				try
				{
					switch (state)
					{
						case 0: //connect clients
							while(socketList.size() < 3)
							{
								Socket connectionSock = serverSock.accept();
								// Add this socket to the list
								socketList.add(connectionSock);

								//Store Names to array
								BufferedReader clientInput = new BufferedReader(
									new InputStreamReader(connectionSock.getInputStream()));
								names.add(clientInput.readLine());
								
								// Send to ClientHandler the socket,arraylist,list of q and a, and names of all sockets
								JClientHandler handler = new JClientHandler(connectionSock, this.socketList, this.names);
								Thread theThread = new Thread(handler);
								theThread.start();
								
								if(socketList.size() == 1 && names.size() == 1)
								{
									handlerList.add(handler);
									
								}
								else if(socketList.size() == 2 && names.size() == 2)
								{
									handlerList.add(handler);
									
								}
								else if(socketList.size() == 3 && names.size() == 3)
								{
									handlerList.add(handler);
									canBuzzIn = handlerList;
								}

								if(socketList.size() == 3)
								{
									String start = "All Contestants have joined. Time to play JEOPARDY! \n";
		
									for (JClientHandler j : handlerList)
									{
										j.SendMessage(start);
									}
									
									state = 1;
									break;
								}
							}
							
						case 1: //send Answer to clients
						
							int randomNum = (int)(Math.random() * answers.size());
							for (JClientHandler j : handlerList)
							{
								j.SendMessage("Answer: " + answers.get(randomNum));
								j.gameStart = true;
							}
							System.out.println("Answer: " + answers.get(randomNum));

							state = 2;

							break;

						case 2: //wait for buzz

							for(JClientHandler x : handlerList)
							{
								System.out.println(x.receivedMessage);
								if(x.receivedMessage == "b")
								{
									int num = handlerList.indexOf(x);
									state = 3;
									x.SendMessage("You Buzzed in first!");
									System.out.println(names.get(num) + "Buzzed in first.");

									for(JClientHandler j : canBuzzIn)
									{
										j.SendMessage(names.get(num) + "Buzzed in first.");
										j.myTurn = false;
									}
									canBuzzIn.remove(x);
									break;
								}

							}
							break;

						case 3: //wait for answer
							//handlerList.get(buzzOrder[0] - 1).myTurn = true;
							// while(true)
							// {

							// }
							break;

						default:
							break;
					}

				}
				catch (Exception e)
				{
					System.out.println("Error: " + e.getMessage());
				}
			}
			// Will never get here, but if the above loop is given
			// an exit condition then we'll go ahead and close the socket
			//serverSock.close();
		}
		catch (IOException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		JServer server = new JServer();
		server.getConnection();
	}

} // JServer
