import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;


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
		int serverSeqNum = 10;
		boolean finished = false;
		Random rand = new Random();
		server_isn = Math.abs(rand.nextInt());
		expectingSeqNum = 0;
		int previousAck = 0;
		
		File file = new File(fileName);
		FileOutputStream fos = new FileOutputStream(file);
		printStream = new PrintStream(fos);
		
		log = new LogFile("mtp_server_log.txt");
		
		if (!file.exists())
			file.createNewFile();
		
		Packet ackReply = null;
		
		// run forever
		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(request);
			
			Packet p = Serialisation.deserialise(request);
			
			// if packet received is a SYN, establish a connection.
			if (p.isSYN()) {
				currentTime = Calendar.getInstance();
				log.writeLog(currentTime.getTime() + " " + "Received SYN packet from " + clientAddress + " " + clientPort +
						" with sequence number " + p.getSeqNumber());
				establishConnection(p, socket, request.getAddress(), request.getPort());
			}
			
			// if packet received is a FIN, establish a connection.
			if (p.isFIN()) {
				writeToFile();
				currentTime = Calendar.getInstance();
				log.writeLog(currentTime.getTime() + " " + "Received FYN packet from " + clientAddress + " " + clientPort +
						" with sequence number " + p.getSeqNumber());
				closeConnection(socket);
			}
			
			if (connectionEstablished && finished == false) {
				if (p.getData() != null && p.getData().length > 0) {
					currentTime = Calendar.getInstance();
					log.writeLog(currentTime.getTime() + " " + "Received data packet from " + clientAddress + " " + clientPort +
							" with sequence number " + p.getSeqNumber());
					ackReply = new Packet(serverSeqNum);
					ackReply.setACK(true);
					// if the packet received is in the correct order, then write to file.
					if (p.getSeqNumber() == expectingSeqNum) {
						previousAck = expectingSeqNum;
						
						// check if there were any out of order packets
						// that were in sequence with the packet just received.
						// set expectingSeqNum accordingly.
						expectingSeqNum = getBufferedData(expectingSeqNum, p.getAckNumber(), fos);
						if (expectingSeqNum == previousAck)
							expectingSeqNum = p.getAckNumber();
						
						// send ACK to client
						ackReply.setAckNumber(expectingSeqNum);
						
						socket.send(Serialisation.serialise(ackReply, clientAddress, clientPort));
						currentTime = Calendar.getInstance();
						log.writeLog(currentTime.getTime() + " " + "Sent ACK packet to " + clientAddress + " " + clientPort +
								" with acknowledge number " + ackReply.getAckNumber());
						if (hasPacket(p.getSeqNumber(), receivedCorrectly) == null)
							receivedCorrectly.add(p);
						
						
					} else {
						if (hasPacket(p.getSeqNumber(), receivedCorrectly) == null) {
							if (hasPacket(p.getSeqNumber(), outOfOrder) == null)
								outOfOrder.add(p);
							ackReply.setAckNumber(expectingSeqNum);
							socket.send(Serialisation.serialise(ackReply, clientAddress, clientPort));
							currentTime = Calendar.getInstance();
							log.writeLog(currentTime.getTime() + " " + "Sent ACK packet to " + clientAddress + " " + clientPort +
									" with acknowledge number " + ackReply.getAckNumber());
						} else {
							ackReply.setAckNumber(expectingSeqNum);
							socket.send(Serialisation.serialise(ackReply, clientAddress, clientPort));
							currentTime = Calendar.getInstance();
							log.writeLog(currentTime.getTime() + " " + "Sent ACK packet to " + clientAddress + " " + clientPort +
									" with acknowledge number " + ackReply.getAckNumber());
						}
					}
				}
			}
		}
		
	}
	
	public static void establishConnection(Packet requestPacket, DatagramSocket socket, InetAddress client, int port) throws Exception {
		reply = new Packet(server_isn);
		reply.setACK(true);
		reply.setSYN(true);
		reply.setAckNumber(requestPacket.getSeqNumber() + 1);
		
		// send SYNACK
		socket.send(Serialisation.serialise(reply, client, port));
		currentTime = Calendar.getInstance();
		log.writeLog(currentTime.getTime() + " " + "Sent SYNACK packet to " + clientAddress + " " + clientPort +
				" with acknowledge number " + reply.getSeqNumber());
		
		try {
			// try to receive an ACK for SYNACK
			DatagramPacket responseFromClient = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(responseFromClient);
			
			Packet p = Serialisation.deserialise(responseFromClient);
			if (p.isACK()) {
				// ACK received, establish a connection
				currentTime = Calendar.getInstance();
				log.writeLog(currentTime.getTime() + " " + "Received ACK for SYNACK packet from " + clientAddress + " " + clientPort +
						" with sequence number " + p.getSeqNumber());
				clientAddress = responseFromClient.getAddress();
				clientPort = responseFromClient.getPort();
				connectionEstablished = true;
				expectingSeqNum = p.getSeqNumber();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeConnection(DatagramSocket socket) throws Exception {
		reply = new Packet(server_isn);
		reply.setACK(true);
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		currentTime = Calendar.getInstance();
		log.writeLog(currentTime.getTime() + " " + "Sent ACK for FIN packet to " + clientAddress + " " + clientPort +
				" with sequence number " + reply.getSeqNumber());
		
		reply.setACK(false);
		reply.setFIN(true);
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		currentTime = Calendar.getInstance();
		log.writeLog(currentTime.getTime() + " " + "Sent FIN packet to " + clientAddress + " " + clientPort +
				" with sequence number " + reply.getSeqNumber());
		
		try {
			DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(response);
			
			Packet p = Serialisation.deserialise(response);
			if (p.isACK()) {
				currentTime = Calendar.getInstance();
				log.writeLog(currentTime.getTime() + " " + "Received ACK for FIN packet from " + clientAddress + " " + clientPort +
						" with sequence number " + p.getSeqNumber());
				writeToFile();
				System.out.println("Received successful.");
				clientAddress = null;
				clientPort = '0';
				connectionEstablished = false;
				outOfOrder.clear();
				System.exit(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int getBufferedData(int expectingSeqNum, int ack, FileOutputStream fos) throws IOException {
		for (Packet p : outOfOrder) {
			if (hasPacket(p.getAckNumber(), receivedCorrectly) == null) {
				if (p.getSeqNumber() == ack) {
					expectingSeqNum = p.getAckNumber();
					receivedCorrectly.add(p);
					ack = p.getAckNumber();
				}
			}
		}
		return expectingSeqNum;
	}
	
	private static Packet hasPacket(int seqNum, LinkedList<Packet> list) {
		for (Packet p : list) {
			if (p.getSeqNumber() == seqNum)
				return p;
		}
		return null;
	}
	
	private static void writeToFile() {
		Collections.sort(receivedCorrectly, new Comparator<Packet>() {
			@Override
			public int compare(Packet o1, Packet o2) {
				if (o1.getSeqNumber() < o2.getSeqNumber())
					return -1;
				else if (o1.getSeqNumber() > o2.getSeqNumber())
					return 1;
				return 0;
			}
		});
		
		for (Packet p : receivedCorrectly) {
			printStream.print(new String(p.getData()));
		}
	}
	
	private static Packet reply;
	private static InetAddress clientAddress;
	private static int clientPort;
	private static boolean connectionEstablished = false;
	private static int server_isn;
	private static final int FILE_SIZE = 1024 * 5;
	private static PrintStream printStream;
	private static Calendar currentTime = Calendar.getInstance();
	private static LogFile log;
	private static int expectingSeqNum;
	private static LinkedList<Packet> outOfOrder = new LinkedList<Packet>();
	private static LinkedList<Packet> receivedCorrectly = new LinkedList<Packet>();
}
