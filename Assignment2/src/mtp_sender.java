import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

/**
 * 
 * @author Sandy
 *
 */
public class mtp_sender {
	public static void main(String args[]) throws Exception {
		if (args.length < 8) {
			System.out.println("Not enough arguements");
			return;
		}
		
		// assign inputs to variables, assuming inputs are
		// all in correct format.
		server = InetAddress.getByName(args[0]);
		receiverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		int mws = Integer.parseInt(args[3]);
		mss = Integer.parseInt(args[4]);
		timeout = Integer.parseInt(args[5]);
		pdrop = Float.parseFloat(args[6]);
		seed = Integer.parseInt(args[7]);
		
		int read = 0; // to store the number of bytes read from file.
		int seqNum = 0; // sequence number
		boolean finished = false; // a flag to see if everything is done
		
		// calculate the number of packets can be
		// sent at once.
		int packetNum = mws / mss;
		
		sentNum = 0; // the number of packets sent
//		int expectingAckNum = 0; // the ack number expecting next
		
		
		socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		
		// get the file.
		File file = new File(fileName);
		final FileInputStream fis = new FileInputStream(file);
		
		// This thread is used to receive acks
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Running receive thread");
					receiveAck(fis);
				} catch (Exception e) {
					System.out.println("Ack not received");
				}
			}
		})).start();
		
		(new Thread(new Runnable() {
			@Override
			public void run() {
				checkTimeout();
			}
		})).start();
		
		// actual file/data sending
		try {
			byte[] buff = new byte[mss];
			int remaining = buff.length;
			establishConnection();
			while (!finished) {
				Random rand = new Random();
				float k = rand.nextFloat();
//				System.out.println("k: " + k);
				// send out packets of the data from the text file.
				if (connectionEstablished) {
					if (sentNum < packetNum) {
						Packet p;
						read = fis.read(buff, buff.length - remaining, remaining);
						p = new Packet(seqNum);
						if (read > 0) {
							remaining -= read;
							if (remaining == 0) { 
								remaining = buff.length;
							} else {
								finished = true;
								remaining = 0;
							}
						}
					
						// send data
						p.setData(buff);
						p.setSeqNumber(seqNum);
						seqNum += read;
						p.setStartTime(Calendar.getInstance());
						
						if (k > pdrop) {
							socket.send(Serialisation.serialise(p, server, receiverPort));
							System.out.println("Packet segment with seq # " + p.getSeqNumber() + " sent");
						} else
							System.out.println("Packet is lost at send");
						sentNum++;
						sentList.add(p); // add sequence number to list
					}
				}
				
//				checkTimeout();
				
				// close the connection and exit after everything is sent out.
				if (finished) {
					closeConnection();
					System.exit(0);
				}
			}
		} finally {
			fis.close();
		}
	}
	
	/**
	 * 
	 * @param fis
	 * @throws Exception
	 */
	public static void receiveAck(FileInputStream fis) throws Exception {
//		socket.setSoTimeout(timeout);
		int duplicatedAckNum = 0;
		int previousAck = 0;
		
		while (true) {
			socket.setSoTimeout(SOCKET_TIMEOUT);
//			System.out.println(connectionEstablished);
			if (connectionEstablished) {
//				System.out.println("Receiving ack");
				byte[] buff = new byte[mss];
				Random rand = new Random();
				float prob = rand.nextFloat();
				
				DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
				socket.receive(response);
				
				if (prob > pdrop) {
					Packet responsePacket = Serialisation.deserialise(response);
					System.out.println("Response ack: " + responsePacket.getAckNumber());
					System.out.println("PreviousAck: " + previousAck);
					if (responsePacket.getAckNumber() != previousAck) {
						System.out.println("Correct ack received");
						if (sentList.size() > 0 && hasSentOut(previousAck)) {
							System.out.println("Packet was sent");
							sentList.remove(responsePacket);
							sentNum--;
						}
						previousAck = responsePacket.getAckNumber();
					} else {
						duplicatedAckNum++;
						System.out.println("Duplicated ack received: " + duplicatedAckNum);
						if (duplicatedAckNum > 2) {
							System.out.println("3 Duplicated ack received");
							fis.read(buff, responsePacket.getAckNumber(), buff.length);
							Packet p = new Packet(responsePacket.getAckNumber());
							p.setData(buff);
							System.out.println(new String(buff));
							sentNum++;
							sentList.add(p);
							socket.send(Serialisation.serialise(p, server, receiverPort));
							System.out.println("Resend successful");
							duplicatedAckNum = 0;
						}
					}
					System.out.println("Received ACK # " + responsePacket.getAckNumber());
				} else
					System.out.println("Packet is lost at receive");
			}
		}
	}
	
	/**
	 * Establish a connection with the server.
	 * @throws Exception Exception is thrown when establishment failed.
	 */
	public static void establishConnection() throws Exception {
		Packet establishment = new Packet(CLIENT_ISN);
		establishment.setSYN(true);
		
		while (!connectionEstablished) {
			// send connection establishment request.
			System.out.println("Establishing connection:");
			socket.send(Serialisation.serialise(establishment, server, receiverPort));
			System.out.println("Establishment written");
			
			try {
				// receive response from server.
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				Packet p = Serialisation.deserialise(response);
				
				// if received response is a SYNACK then send an ACK.
				if (p.isACK() && p.isSYN()) {
					System.out.println("SYNACK received");
					connectionEstablished = true;
					
					// reset establishment details
					establishment.setSeqNumber(establishment.getSeqNumber() + 1);
					establishment.setAckNumber(p.getSeqNumber() + 1);
					establishment.setSYN(false);
					establishment.setACK(true);
					socket.send(Serialisation.serialise(establishment, server, receiverPort));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean closeConnection() throws Exception {
		Packet close = new Packet(CLIENT_ISN);
		close.setFIN(true);
		boolean connectionClosed = false;
		
		while (!connectionClosed) {
			// send out FIN request to server.
			System.out.println("Closing connection");
			DatagramPacket packet = Serialisation.serialise(close, server, receiverPort);
			socket.send(packet);
			System.out.println("Closing packet sent");
			
			try {
				// receive response from server
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				Packet p = Serialisation.deserialise(response);
				
				// if response received is an ACK then wait to
				// receive another response
				if (p.isACK()) {
					System.out.println("Received ACK, Waiting for FIN from server");
					socket.receive(response);
					p = Serialisation.deserialise(response);
					
					// if response received is a FIN then wait
					// for 30 seconds and close the connection.
					if (p.isFIN()) {
						// TODO: shortened the time for debugging purposes.
						wait(10);
						System.out.println("Connection Closed");
						connectionClosed = true;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static void checkTimeout() {
		// This thread is used to constantly check if
		// certain packet has timed out since it was sent.
//		System.out.println("inside checkTimeout");
		
//				System.out.println("Checking if packet has timeout");
		LinkedList<Packet> tempList = copySentList();
		while (true) {
			if (tempList.size() > 0 && connectionEstablished) {
				System.out.println("Checking if packet has timeout");
				for (Packet p : tempList) {
//							System.out.println("Time elapsed for packet # " + p.getSeqNumber() + " " + p.calculateTimeElapse());
					if (p.calculateTimeElapse() >= timeout) {
//								System.out.println("Packet # " + p.getSeqNumber() + " timed out.");
						try {
							socket.send(Serialisation.serialise(p, server, receiverPort));
							System.out.println("Packet resent due to time out");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Checks if certain packet has been sent out.
	 * @param packet The packet to check if it has been sent out.
	 * @param read The number of bytes read
	 * @return True if the packet has been sent out. False otherwise.
	 */
	public static boolean hasSentOut(int ack) {
//		System.out.println("Checking if packet is sent");
//		System.out.println(sentList.size());
		for (Packet p : sentList) {
//			int ack = packet.getAckNumber();
//			if (ack != 0)
//				ack = ack - packet.getData().length;
			System.out.println("Ack checked: " + ack);
			if (p.getSeqNumber() == ack)
				return true;
		}
		return false;
	}
	
	/**
	 * This method is used to wait for a certain time
	 * during the connection tear down process.
	 * @param time The number of seconds required to wait
	 * before actually closing the connection.
	 */
	public static void wait(int time) {
		System.out.println("Waiting 30 secs");
		long t0, t1;
		t0 = System.currentTimeMillis();
		t1 = System.currentTimeMillis();
		
		while ((t1 - t0) < (time * 1000)) {
			t1 = System.currentTimeMillis();
		}
	}
	
	private static LinkedList<Packet> copySentList() {
//		System.out.println("Copying list");
		LinkedList<Packet> tempList = new LinkedList<Packet>();
		for (Packet p : sentList) {
			tempList.add(p);
		}
		return tempList;
	}
	
	private static DatagramSocket socket;
	private static InetAddress server;
	private static int receiverPort;
	private static float pdrop;
	private static int seed;
	private static int timeout;
	private static int sentNum;
	private static int mss;
	private static boolean connectionEstablished = false;

	// a list of all sequence numbers being sent out.
	private static LinkedList<Packet> sentList = new LinkedList<Packet>();
	
	// constants
	private static final int FILE_SIZE = 1024 * 5;
	private static final int CLIENT_ISN = 3;
	private static final int SOCKET_TIMEOUT = 15000;
	
}
