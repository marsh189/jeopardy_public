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
	private ArrayList<String> alternates = new ArrayList<String>();
	private JClientHandler buzzedInFirst;
	private String nameOfBuzzed;

	ArrayList<JClientHandler> handlerList = new ArrayList<JClientHandler>();
	ArrayList<JClientHandler> canBuzzIn = new ArrayList<JClientHandler>();
	ArrayList<String> names = new ArrayList<String>();
	String clientAnswer = "";
	int randomNum;
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
			//read files for answers and questions (Matt implemented this)
			Scanner ansScanner = new Scanner(new File("Answers.txt"));
			Scanner qScanner = new Scanner(new File("Questions.txt"));
			Scanner altScanner = new Scanner(new File("Alternates.txt"));
			String aCurrent;
			String qCurrent;
			String altCurrent;

			while (ansScanner.hasNextLine())
			{
				aCurrent = ansScanner.nextLine();
				qCurrent = qScanner.nextLine();
				altCurrent = altScanner.nextLine();
				answers.add(aCurrent);
				questions.add(qCurrent);
				alternates.add(altCurrent);

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
								
								// Send to ClientHandler the socket, arraylist, and names of all sockets
								JClientHandler handler = new JClientHandler(connectionSock, this.socketList, this.names);
								Thread theThread = new Thread(handler);
								theThread.start();
								
								//Adds ClientHandlers in handlerList
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

								if(socketList.size() == 3) //Starts Game
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
						
							randomNum = (int)(Math.random() * answers.size());
							for (JClientHandler j : handlerList)
							{
								j.SendMessage("Answer: " + answers.get(randomNum));
								j.SendMessage("\nPress 'Enter' to BUZZ in...");
								j.gameStart = true;
							}
							System.out.println("Answer: " + answers.get(randomNum));
							System.out.println("\nWaiting for clients to BUZZ in.");

							state = 2;

							break;

						case 2: //wait for buzz (worked on by both Haley and Matt)

							for(JClientHandler x : handlerList)
							{
								//System.out.println(x.receivedMessage);
								if(x.receivedMessage != null)		//checks if any of the contestants clicked Enter
								{
									int num = handlerList.indexOf(x);
									state = 3;
									buzzedInFirst = x;
									x.SendMessage("You Buzzed in first!");
									nameOfBuzzed = names.get(num);
									System.out.println(nameOfBuzzed + " buzzed in first."); //prints out who buzzed in

									for(JClientHandler j : canBuzzIn)
									{
										j.SendMessage(nameOfBuzzed+ " buzzed in first.");
										j.myTurn = false;
										j.receivedMessage = null;

									}
									
									buzzedInFirst.receivedMessage = null;
									canBuzzIn.remove(x);

									break;
								}

							}
							break;

						case 3: //wait and receive answer (implemented my Matt)
							buzzedInFirst.SendMessage("Type your response:");
							buzzedInFirst.myTurn = true;
							String ans = buzzedInFirst.GetResponse();
							if(ans != null || ans != "")
							{
								clientAnswer = ans;
								state = 4;
							}
							break;

						case 4: //check if correct or not (implemented by Haley)
							int index =  clientAnswer.indexOf("?");
							if(index < 0)
							{
								buzzedInFirst.SendMessage("Your answer should include a ?");
								for(JClientHandler j : handlerList)
								{
									j.SendMessage(nameOfBuzzed + " has not entered the correct response.");
									j.SendMessage("remaining contestants have another opportunity to answer this question ");
								}
								state = 2;
							}
							else
							{
								String temp = clientAnswer.substring(0,index);
								System.out.println(temp.toLowerCase());
								System.out.println(questions.get(randomNum).toLowerCase());
								String cAnswer = temp.toLowerCase();
								String correctAnswer = (String)questions.get(randomNum).toLowerCase();
								String alt = (String)alternates.get(randomNum).toLowerCase();

								if(cAnswer.equals(correctAnswer) || cAnswer.equals(alt))
								{
									buzzedInFirst.SendMessage("You have entered the correct response.");
									System.out.println("The correct response has been entered");
									for(JClientHandler j : handlerList)
									{
										j.SendMessage(names.get(handlerList.indexOf(j)) + " has entered the correct response. Prepare for the next round.");

									}
									questions.remove(randomNum);
									answers.remove(randomNum);
									alternates.remove(randomNum);
									canBuzzIn.add(buzzedInFirst);
									state = 1;
								}

								else
								{
									System.out.println("You have not entered the correct response.");
									for(JClientHandler j : handlerList)
									{
										j.SendMessage(nameOfBuzzed + " has not entered the correct response.");
										j.SendMessage("remaining contestants have another opportunity to answer this question ");
									}
									state = 2;
								}
							}
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
