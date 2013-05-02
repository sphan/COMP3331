import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPSockets {
	public static void sendRequest(Peer p, DHTFile f) throws Exception {
		InetAddress serverIPAddress = InetAddress.getByName(serverName);
		
		int receiverPort = p.getFirstSuccessor() + 50000;
		Socket clientSocket = new Socket(serverIPAddress, receiverPort);
		
		String sentence = "File request named " + f.getFileName() + " with hash " + 
				f.getHashCode() + " from " + p.getPortNumber();
		
		// write to server
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
		System.out.println("File request message for " + f.getFileName() + " has been sent to my successor.");
	}
	
	public static void sendQuitRequest(Peer p) throws Exception {
		InetAddress serverIPAddress = InetAddress.getByName(serverName);
		
		int receiverPort1 = p.getFirstPreDecessor() + 50000;
		int receiverPort2 = p.getSecondPreDecessor() + 50000;
		int receiverPort3 = p.getFirstSuccessor() + 50000;
		int receiverPort4 = p.getSecondSuccessor() + 50000;
		
		Socket clientSocket1 = new Socket(serverIPAddress, receiverPort1);
		Socket clientSocket2 = new Socket(serverIPAddress, receiverPort2);
		Socket clientSocket3 = new Socket(serverIPAddress, receiverPort3);
		Socket clientSocket4 = new Socket(serverIPAddress, receiverPort4);
		
		String sentence = "My successors are peer " + p.getFirstSuccessor() + " and peer " + p.getSecondSuccessor()
				+ " and my predecessors are " + p.getFirstPreDecessor() + " and " + p.getSecondPreDecessor()
				+ " from peer " + p.getPortNumber();
		
		DataOutputStream outToServer = new DataOutputStream(clientSocket1.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
		outToServer = new DataOutputStream(clientSocket2.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
		outToServer = new DataOutputStream(clientSocket3.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
		outToServer = new DataOutputStream(clientSocket4.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
	}
	
	public static void sendSuccessorTerminatedRequest(Peer p, int deadSuccessor) throws Exception {
		InetAddress serverIPAddress = InetAddress.getByName(serverName);
		
		if (deadSuccessor == p.getFirstSuccessor()) {
			p.setFirstSuccessor(p.getSecondSuccessor());
		}
		
		int receiverPort = p.getFirstSuccessor() + 50000;
		Socket clientSocket = new Socket(serverIPAddress, receiverPort);
		
		String sentence = "Who is my second successor from " + p.getPortNumber();
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
	}
	
	public static void receiveRequest(Peer p) throws Exception {
		int serverPort = p.getPortNumber();
		
		// create server socket
		ServerSocket welcomeSocket = new ServerSocket(serverPort);
		
		while (true) {
			// accept connection from connection queue
			Socket connectionSocket = welcomeSocket.accept();
			
			// create read stream to get input
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			String clientSentence;

			clientSentence = inFromClient.readLine();
			String[] params = clientSentence.split(" ");
			String fileName = null;
			int fileHash = 0, peerFrom = 0;
			int successor1 = 0, successor2 = 0;
			int predecessor1 = 0, predecessor2 = 0;
			
			// proccess input and prepare reply stream
			if (clientSentence.contains("request") || clientSentence.contains("has file")) {
				try {
					fileName = params[3];
					fileHash = Integer.parseInt(params[6]);
					peerFrom = Integer.parseInt(params[8]);
				} catch (NumberFormatException e) {
					System.out.println("Could not convert numbers");
				}
			} else if (clientSentence.contains("successors")) {
				try {
					successor1 = Integer.parseInt(params[4].trim());
					successor2 = Integer.parseInt(params[7].trim());
					predecessor1 = Integer.parseInt(params[12].trim());
					predecessor2 = Integer.parseInt(params[14].trim());
					peerFrom = Integer.parseInt(params[17].trim()) - 50000;
				} catch (NumberFormatException e) {
					System.out.println("Could not convert numbers");
					e.printStackTrace();
				}
			} else if (clientSentence.contains("who")) {
				try {
					peerFrom = Integer.parseInt(params[6]) - 50000;
				} catch (NumberFormatException e) {
					System.out.println("Could not convert numbers");
				}
			} else if (clientSentence.contains("second successor")) {
				try {
					successor2 = Integer.parseInt(params[5]) - 50000;
				} catch (NumberFormatException e) {
					System.out.println("Could not convert numbers");
				}
			}
			
			
			InetAddress serverIPAddress = InetAddress.getByName(serverName);
			
			// display out response
			if (clientSentence.contains("request")) {
				// If current peer contains the file, send a reply
				// back to the requesting peer
				// else pass on to its successor.
				if (p.containFile(fileHash)) {
					String reply = "has file named " + fileName + " with hash " + fileHash + " from " + (serverPort - 50000);
					Socket clientSocket = new Socket(serverIPAddress, peerFrom);
					DataOutputStream outToRequester = new DataOutputStream(clientSocket.getOutputStream());
					outToRequester.writeBytes(reply + '\n');
					System.out.println("File " + fileName + " is here.");
					System.out.println("A response message, destined for peer " + (peerFrom - 50000) + ", " +
							"has been sent.");
				} else {
					String request = "File request named " + fileName + " with hash " + fileHash + " from " + peerFrom;
					Socket clientSocket = new Socket(serverIPAddress, p.getFirstSuccessor() + 50000);
					DataOutputStream outToRequester = new DataOutputStream(clientSocket.getOutputStream());
					outToRequester.writeBytes(request + '\n');
					System.out.println("File " + fileName + " is not stored here.");
					System.out.println("File request message has been forwarded to my successor.");
				}
			} else if (clientSentence.contains("has file ")) {
				System.out.println("Received a response message from peer " + peerFrom + ", which has the file " + fileName + ".");
			} else if (clientSentence.contains("successors")) {
				if (p.getFirstSuccessor() == peerFrom) {
					p.setFirstSuccessor(successor1);
					p.setSecondSuccessor(successor2);
					System.out.println("Peer " + peerFrom + " will depart from the network.");
					printMySuccessors(p);
				} else if (p.getSecondSuccessor() == peerFrom) {
					p.setSecondSuccessor(successor1);
					System.out.println("Peer " + peerFrom + " will depart from the network.");
					printMySuccessors(p);
				} else if (p.getFirstPreDecessor() == peerFrom) {
					p.setFirstPreDecessor(predecessor1);
					p.setSecondPreDecessor(predecessor2);
				} else if (p.getSecondPreDecessor() == peerFrom) {
					p.setSecondPreDecessor(predecessor1);
				}
			} else if (clientSentence.contains("who")) {
				p.setFirstPreDecessor(peerFrom);
				String reply = "Your second successor is peer " + p.getFirstSuccessor();
				Socket clientSocket = new Socket(serverIPAddress, peerFrom);
				DataOutputStream outToRequester = new DataOutputStream(clientSocket.getOutputStream());
				outToRequester.writeBytes(reply + '\n');
			} else if (clientSentence.contains("second successor")) {
				p.setSecondSuccessor(successor2);
				printMySuccessors(p);
			}
		}
	}
	
	private static void printMySuccessors(Peer p) {
		System.out.println("My first successor is now peer " + p.getFirstSuccessor() + ".");
		System.out.println("My second successor is now peer " + p.getSecondSuccessor() + ".");
	}
	
	private static String serverName = "127.0.0.1";
}
