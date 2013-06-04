import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;


public class mtp_server {
	public static void main(String args[]) throws Exception {
		// check inputs
		if (args.length < 2) {
			System.out.println("Not enough arguements");
			return;
		}
		
		int myPort = Integer.parseInt(args[0]);
		String fileName = args[1];
		DatagramSocket socket = new DatagramSocket(myPort);
		int expectingSeqNum = 0;
		int serverSeqNum = 10;
		
		File file = new File(fileName);
		FileOutputStream fos = new FileOutputStream(file);
		
		if (!file.exists())
			file.createNewFile();
		
		// run forever
		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(request);
			System.out.println("Received data");
			
			Packet p = Serialisation.deserialise(request);
			
			// if packet received is a SYN, establish a connection.
			if (p.isSYN()) {
				System.out.println("SYN packet received");
				establishConnection(p, socket, request.getAddress(), request.getPort());
			}
			
			// if packet received is a FIN, establish a connection.
			if (p.isFIN()) {
				System.out.println("FIN received");
				closeConnection(socket);
			}
			
			if (connectionEstablished) {
				if (p.getData() != null && p.getData().length > 0) {
					System.out.println("Packet with seq # " + p.getSeqNumber() + " received");
					// if the packet received is in the correct order, then write to file.
					if (p.getSeqNumber() == expectingSeqNum) {
						fos.write(p.getData());
						fos.flush();
						
						// send ACK to client
						expectingSeqNum += p.getData().length;
						Packet ackReply = new Packet(serverSeqNum);
						ackReply.setAckNumber(expectingSeqNum);
						ackReply.setACK(true);
						
						socket.send(Serialisation.serialise(ackReply, clientAddress, clientPort));
						System.out.println("Ack number " + ackReply.getAckNumber() + " sent");
					} else {
						System.out.println("Out of order.");
						if (hasPacket(p)) {
							
						} else {
							outOfOrder.add(p);
						}
					}
				}
			}
			
			// if the connection is no longer in established state
			// exit the program
			if (connectionEstablished == false) {
				System.out.println("Exiting program");
				System.exit(0);
			}
		}
		
	}
	
	public static void establishConnection(Packet requestPacket, DatagramSocket socket, InetAddress client, int port) throws Exception {
		reply = new Packet(SERVER_ISN);
		reply.setACK(true);
		reply.setSYN(true);
		reply.setAckNumber(requestPacket.getSeqNumber() + 1);
		
		// send SYNACK
		socket.send(Serialisation.serialise(reply, client, port));
		System.out.println("SYNACK sent");
		
		try {
			// try to receive an ACK for SYNACK
			DatagramPacket responseFromClient = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(responseFromClient);
			
			Packet p = Serialisation.deserialise(responseFromClient);
			if (p.isACK()) {
				// ACK received, establish a connection
				System.out.println("ACK for SYNACK received");
				clientAddress = responseFromClient.getAddress();
				clientPort = responseFromClient.getPort();
				connectionEstablished = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeConnection(DatagramSocket socket) throws Exception {
		System.out.println("Closing connection");
		reply = new Packet(SERVER_ISN);
		reply.setACK(true);
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		System.out.println("Sent ACK for FIN");
		
		reply.setACK(false);
		reply.setFIN(true);
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		System.out.println("Sent FIN from server");
		
		try {
			DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(response);
			
			Packet p = Serialisation.deserialise(response);
			if (p.isACK()) {
				System.out.println("Received ACK for FIN");
				clientAddress = null;
				clientPort = '0';
				connectionEstablished = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean hasPacket(Packet packet) {
		for (Packet p : outOfOrder) {
			if (p.getSeqNumber() == packet.getSeqNumber())
				return true;
		}
		return false;
	}
	
	private static Packet reply;
	private static InetAddress clientAddress;
	private static int clientPort;
	private static boolean connectionEstablished = false;
	private static final int FILE_SIZE = 1024 * 5;
	private static final int SERVER_ISN = 6;
	private static final int TIME_OUT = 3000;
	private static LinkedList<Packet> outOfOrder = new LinkedList<Packet>();
}
