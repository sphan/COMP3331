import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
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
//		Random rand = new Random(seed);
		
		// TODO: need to change to random number
		client_isn = 0;
		
		int read = 0; // to store the number of bytes read from file.
		
		// TODO: need to update it to client_isn + 1
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
		
		bw = new BufferedWriter(new FileWriter("mtp_sender_log.txt"));
		bw.write("Test Test");
		
		// This thread is used to receive acks
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Running receive thread");
					receiveAck(fis);
				} catch (Exception e) {
					System.out.println("Ack not received");
					e.printStackTrace();
				}
			}
		})).start();
		
		Thread timer = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (calculateTimeElapse() >= timeout) {
//						System.out.println("Packet timed out");
						Packet p = null;
						if (sentList.size() != receivedList.size()) {
							for (Packet pt : sentList) {
//								System.out.println("Checking if list has packet #" + pt.getSeqNumber() + " at timeout");
								if (hasPacket(pt, receivedList) == null) {
									p = pt;
//									startTime = Calendar.getInstance();
									currentTime = Calendar.getInstance();
									try {
										bw.write(currentTime.getTime() + " " + "Timed out data packet for " + server + " " + receiverPort +
												" with sequence number " + p.getSeqNumber());
										bw.newLine();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									
									try {
										sendPacket(p);
									} catch (Exception e) {
										e.printStackTrace();
									}
									System.out.println("Packet # " + p.getSeqNumber() + " resent");
									break;
								}
							}
						}
						
						startTime = Calendar.getInstance();
					}
				}
			}
		});
		timer.start();
		
		// actual file/data sending
		try {
			byte[] buff = new byte[mss];
			int remaining = buff.length;
			establishConnection();
			while (!finished) {
				// send out packets of the data from the text file.
				if (connectionEstablished) {
					if (sentNum < packetNum) {
						Packet p;
						
						// read data from text file
						read = fis.read(buff, buff.length - remaining, remaining);
						p = new Packet(seqNum);
						if (read > 0) {
							remaining -= read;
							if (remaining == 0)
								remaining = buff.length;
							else
								remaining = 0;
						}
					
						// send data
						p.setData(buff);
						p.setSeqNumber(seqNum);
						p.setAckNumber(p.getSeqNumber() + read);
						System.out.println(p.getAckNumber());
						seqNum += read;
						p.setStartTime(Calendar.getInstance());
						
						sendPacket(p);
						if (sentNum == 1)
							startTime = Calendar.getInstance();
						
						if (sentList.size() == receivedList.size()) {
							System.out.println("List length equal");
							finished = true;
						}
					}
				}
				
				// close the connection and exit after everything is sent out.
				if (finished) {
					closeConnection();
					socket.close();
					bw.close();
					fis.close();
					System.exit(0);
				}
			}
		} finally {
			fis.close();
		}
	}
	
	/**
	 * 
	 * @param p
	 * @throws Exception
	 */
	public static void sendPacket(Packet p) throws Exception {
		Random rand = new Random();
		float k = rand.nextFloat();
		
		if (k > pdrop) {
			if (p != null) {
				socket.send(Serialisation.serialise(p, server, receiverPort));
				System.out.println("Packet segment with seq # " + p.getSeqNumber() + " sent");
				currentTime = Calendar.getInstance();
				bw.write(currentTime.getTime() + " " + "Data packet sent to " + server + " " + receiverPort +
						" with sequence number " + p.getSeqNumber());
				bw.newLine();
			}
		} else {
			currentTime = Calendar.getInstance();
			bw.write(currentTime.getTime() + " " + "Data packet for " + server + " " + receiverPort +
					" with sequence number " + p.getSeqNumber()+ " is dropped.");
			bw.newLine();
		}
//			System.out.println("Packet is lost at send");
		sentNum++;
		
		synchronized (sentList) {
//			System.out.println("Checking if list has packet #" + p.getSeqNumber() + " at send");
			if (hasPacket(p, sentList) == null)
				sentList.add(p); // add packet sent to list
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
			if (connectionEstablished) {
				DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
				socket.receive(response);
				currentTime = Calendar.getInstance();
				System.out.println("Receiving packets");
				
//				if (prob > pdrop) {
					Packet responsePacket = Serialisation.deserialise(response);
//					System.out.println("Previous ACK: " + previousAck);
					if (responsePacket.getAckNumber() != previousAck) {
						if (sentList.size() > 0 && hasSentOut(previousAck)) {
							// remove the corresponding packet from sent list
//							System.out.println("ACK received correctly");
							synchronized (sentList) {
								for (Packet p : sentList) {
									if (p.getSeqNumber() == 0 || p.getSeqNumber() < responsePacket.getAckNumber()) {
//										System.out.println("Adding packet " + p.getSeqNumber() + " to receive list");
										if (hasPacket(p, receivedList) == null) {
											receivedList.add(p);
											System.out.println("packet " + p.getSeqNumber() + " added to received list");
										}
//										System.out.println("Receive list " + receivedList.size());
										previousAck += p.getData().length;
									}
								}
								startTime = Calendar.getInstance();
							}
						}
						previousAck = responsePacket.getAckNumber();
					} else {
						duplicatedAckNum++;
//						System.out.println("Duplicated ack # " + previousAck + " :" + duplicatedAckNum);
						if (duplicatedAckNum > 2) {
							System.out.println("3 Duplicated ack received");
							synchronized (sentList) {
								Packet p = getPacket(responsePacket.getAckNumber());
								sendPacket(p);
								// remove previous copy
//								System.out.println("Checking if list has packet #" + p.getSeqNumber() + " line 222");
//								if (hasPacket(p, receivedList) == null)
//									receivedList.add(p);
							}
							duplicatedAckNum = 0;
						}
					}
					sentNum--;
					if (sentList.size() > receivedList.size())
						startTime = Calendar.getInstance();
					bw.write(currentTime.getTime() + " " + "ACK packet received from " + server + " " + receiverPort +
							" and ackknowledgement number " + responsePacket.getAckNumber());
					bw.newLine();
					
//					System.out.println("Received ACK # " + responsePacket.getAckNumber());
//				} else
//					System.out.println("Packet is lost at receive");
			}
		}
	}
	
	/**
	 * Establish a connection with the server.
	 * @throws Exception Exception is thrown when establishment failed.
	 */
	public static void establishConnection() throws Exception {
		Packet establishment = new Packet(client_isn);
		establishment.setSYN(true);
		
		while (!connectionEstablished) {
			// send connection establishment request.
			System.out.println("Establishing connection:");
			socket.send(Serialisation.serialise(establishment, server, receiverPort));
			currentTime = Calendar.getInstance();
			bw.write(currentTime.getTime() + " " + "SYN packet sent to " + server + " " + receiverPort +
					" with sequence number " + establishment.getSeqNumber());
			bw.newLine();
			System.out.println("Establishment written");
			
			try {
				// receive response from server.
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				Packet p = Serialisation.deserialise(response);
				
				// if received response is a SYNACK then send an ACK.
				if (p.isACK() && p.isSYN()) {
					System.out.println("SYNACK received");
					currentTime = Calendar.getInstance();
					bw.write(currentTime.getTime() + " " + "SYNACK packet received from " + server + " " + receiverPort +
							" with sequence number " + p.getSeqNumber() + " and ackknowledgement number " + p.getAckNumber());
					bw.newLine();
					connectionEstablished = true;
					
					// reset establishment details
					establishment.setSeqNumber(establishment.getSeqNumber() + 1);
					establishment.setAckNumber(p.getSeqNumber() + 1);
					establishment.setSYN(false);
					establishment.setACK(true);
					socket.send(Serialisation.serialise(establishment, server, receiverPort));
					currentTime = Calendar.getInstance();
					bw.write(currentTime.getTime() + " " + "ACK packet sent to " + server + " " + receiverPort +
							" with sequence number " + establishment.getSeqNumber() + 
							" and ackknowledgement number " + establishment.getAckNumber());
					bw.newLine();
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
		Packet close = new Packet(client_isn);
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
	
	/**
	 * Checks if certain packet has been sent out.
	 * @param packet The packet to check if it has been sent out.
	 * @param read The number of bytes read
	 * @return True if the packet has been sent out. False otherwise.
	 */
	public static boolean hasSentOut(int ack) {
		for (Packet p : sentList) {
			if (p.getSeqNumber() == ack)
				return true;
		}
		return false;
	}
	
	public static Packet hasPacket(Packet packet, LinkedList<Packet> list) {
		
		for (Packet p : list) {
			if (p.getSeqNumber() == packet.getSeqNumber()) {
//				System.out.println("Has packet");
				return p;
			}
		}
//		System.out.println("Don't have packet");
		return null;
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
	
	private static Packet getPacket(int seqNum) {
		for (Packet p : sentList) {
			if (p.getSeqNumber() == seqNum)
				return p;
		}
		return null;
	}
	
//	private synchronized static void removeFromSentList(Packet packet) {
//		Iterator<Packet> i = sentList.iterator();
//		System.out.println("Removing packet # " + packet.getSeqNumber());
//		while (i.hasNext()) {
//			Packet p = i.next();
//			if (p.getSeqNumber() == packet.getSeqNumber())
//				i.remove();
//		}
//	}
	
	/**
	 * This method calculates the amount of time elapsed since
	 * the packet was sent out.
	 * @param startTime The time when the packet was sent out
	 * @return The number of milliseconds elapsed.
	 */
	private static long calculateTimeElapse() {
		Calendar endTime = Calendar.getInstance();
		Date st = startTime.getTime();
		Date et = endTime.getTime();
		long ls = st.getTime();
		long le = et.getTime();
		long diff = (le - ls);
		return diff;
	}
	
	private static DatagramSocket socket;
	private static InetAddress server;
	private static int receiverPort;
	private static float pdrop;
	private static int seed;
	private static int timeout;
	private static int sentNum;
	private static int mss;
	private static int client_isn;
	private static Calendar startTime = Calendar.getInstance();
	private static Calendar currentTime = Calendar.getInstance();
	private static boolean connectionEstablished = false;
	private static BufferedWriter bw;

	// a list of all sequence numbers being sent out.
	private static LinkedList<Packet> sentList = new LinkedList<Packet>();
	private static LinkedList<Packet> receivedList = new LinkedList<Packet>();
	
	// constants
	private static final int FILE_SIZE = 1024 * 5;
	private static final int SOCKET_TIMEOUT = 150000;
	
}
