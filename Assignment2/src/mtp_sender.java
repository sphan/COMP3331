import java.io.File;
import java.io.FileInputStream;
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
		Random rand = new Random(seed);
		
		client_isn = Math.abs(rand.nextInt());
		
		int read = 0; // to store the number of bytes read from file.
		
		int seqNum = client_isn + 1; // sequence number
		
		// calculate the number of packets can be
		// sent at once.
		packetNum = mws / mss;
		
		sentNum = 0; // the number of packets sent
		
		log = new LogFile("mtp_sender_log.txt");
		
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
					receiveAck(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
		
		timer();
		
		// actual file/data sending
		try {
			byte[] buff = new byte[mss];
			int remaining = buff.length;
			establishConnection();
			while (!sendFinished) {
				// send out packets of the data from the text file.
				if (connectionEstablished) {
					if (sentNum < packetNum) {
						Packet p;
						buff = new byte[mss];
						
						// read data from text file
						read = fis.read(buff, buff.length - remaining, remaining);
						p = new Packet(seqNum);
						if (read > 0) {
							remaining -= read;
							if (remaining == 0)
								remaining = buff.length;
						} else {
							if (remaining < buff.length) {
								sendFinished = true;
								break;
							}
						}
						
						// send data
						p.setData(buff);
						p.setSeqNumber(seqNum);
						p.setAckNumber(p.getSeqNumber() + read);
						seqNum += read;
						p.setStartTime(Calendar.getInstance());
						
						sendPacket(p, false);
						if (sentNum == 1)
							startTime = Calendar.getInstance();
					}
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
	public static void sendPacket(Packet p, boolean resend) throws Exception {
		Random rand = new Random(seed);
		float k = rand.nextFloat();
		
		if (sentNum < packetNum) {
			if (k > pdrop) {
				if (p != null) {
					socket.send(Serialisation.serialise(p, server, receiverPort));
					currentTime = Calendar.getInstance();
					if (resend)
						log.writeLog(currentTime.getTime() + " " + "Data packet resent to " + server + " " + receiverPort +
							" with sequence number " + p.getSeqNumber());
					else
						log.writeLog(currentTime.getTime() + " " + "Data packet sent to " + server + " " + receiverPort +
								" with sequence number " + p.getSeqNumber());
				}
			} else {
				currentTime = Calendar.getInstance();
				log.writeLog(currentTime.getTime() + " " + "Data packet for " + server + " " + receiverPort +
						" with sequence number " + p.getSeqNumber()+ " is dropped.");
			}
			sentNum++;
			
			synchronized (sentList) {
				if (hasPacket(p, sentList) == null)
					sentList.add(p); // add packet sent to list
			}
		}
		
	}
	
	/**
	 * 
	 * @param fis
	 * @throws Exception
	 */
	public static void receiveAck(FileInputStream fis) throws Exception {
		int duplicatedAckNum = 0;
		int previousAck = 0;
		
		while (true) {
			socket.setSoTimeout(SOCKET_TIMEOUT);
			if (connectionEstablished) {
				DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
				socket.receive(response);
				currentTime = Calendar.getInstance();
				
				Packet responsePacket = Serialisation.deserialise(response);
				if (responsePacket.getAckNumber() != previousAck) {
					if (sentList.size() > 0 && hasSentOut(previousAck)) {
						// remove the corresponding packet from sent list
						synchronized (sentList) {
							for (Packet p : sentList) {
								if (p.getSeqNumber() == 0 || p.getSeqNumber() < responsePacket.getAckNumber()) {
									if (hasPacket(p, receivedList) == null) {
										receivedList.add(p);
									}
									previousAck += p.getData().length;
								}
							}
							startTime = Calendar.getInstance();
						}
					}
					previousAck = responsePacket.getAckNumber();
				} else {
					duplicatedAckNum++;
					if (duplicatedAckNum > 2) {
						synchronized (sentList) {
							Packet p = getPacket(responsePacket.getAckNumber());
							sendPacket(p, false);
						}
						duplicatedAckNum = 0;
					}
				}
				sentNum--;
				if (sentList.size() > receivedList.size())
					startTime = Calendar.getInstance();
				log.writeLog(currentTime.getTime() + " " + "ACK packet received from " + server + " " + receiverPort +
						" and acknowledgement number " + responsePacket.getAckNumber());
				
				if (sendFinished && sentList.size() == receivedList.size()) {
					finished = true;
					System.out.println("Sent successful.");
					try {
						closeConnection();
						socket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.exit(0);
				}
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
			socket.send(Serialisation.serialise(establishment, server, receiverPort));
			currentTime = Calendar.getInstance();
			log.writeLog(currentTime.getTime() + " " + "SYN packet sent to " + server + " " + receiverPort +
					" with sequence number " + establishment.getSeqNumber());
//			bw.newLine();
			
			try {
				// receive response from server.
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				Packet p = Serialisation.deserialise(response);
				
				// if received response is a SYNACK then send an ACK.
				if (p.isACK() && p.isSYN()) {
					currentTime = Calendar.getInstance();
					log.writeLog(currentTime.getTime() + " " + "SYNACK packet received from " + server + " " + receiverPort +
							" with sequence number " + p.getSeqNumber() + " and acknowledgement number " + p.getAckNumber());
					connectionEstablished = true;
					
					// reset establishment details
					establishment.setSeqNumber(establishment.getSeqNumber() + 1);
					establishment.setAckNumber(p.getSeqNumber() + 1);
					establishment.setSYN(false);
					establishment.setACK(true);
					socket.send(Serialisation.serialise(establishment, server, receiverPort));
					currentTime = Calendar.getInstance();
					log.writeLog(currentTime.getTime() + " " + "ACK packet sent to " + server + " " + receiverPort +
							" with sequence number " + establishment.getSeqNumber() + 
							" and ackknowledgement number " + establishment.getAckNumber());
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
			DatagramPacket packet = Serialisation.serialise(close, server, receiverPort);
			socket.send(packet);
			
			try {
				// receive response from server
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				Packet p = Serialisation.deserialise(response);
				
				// if response received is an ACK then wait to
				// receive another response
				if (p.isACK()) {
					socket.receive(response);
					p = Serialisation.deserialise(response);
					
					// if response received is a FIN then wait
					// for 30 seconds and close the connection.
					if (p.isFIN()) {
						wait(END_WAIT_TIME);
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
	 * This is a timer function that uses a new thread to constantly check if 
	 * timer has timed out. If time out, this will resend the oldest packet.
	 */
	public static void timer() {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				while (!finished) {
					if (calculateTimeElapse() >= timeout) {
						sentNum--;
						Packet p = null;
//						if (sentList.size() != receivedList.size()) {
							synchronized (sentList) {
								for (Packet pt : sentList) {
									if (hasPacket(pt, receivedList) == null) {
										p = pt;
										currentTime = Calendar.getInstance();
										log.writeLog(currentTime.getTime() + " " + "Timed out data packet for " + server + " " + receiverPort +
												" with sequence number " + p.getSeqNumber());
										try {
											sendPacket(p, true);
										} catch (Exception e) {
											e.printStackTrace();
										}
										break;
									}
								}
							}
//						}
						
						startTime = Calendar.getInstance();
					}
				}
			}
		})).start();
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
	
	/**
	 * Check if certain packet exists in certain list.
	 * @param packet The packet looking for in the list.
	 * @param list The list from where the packet should look up from.
	 * @return The packet if the packet is in the list specified. Null otherwise.
	 */
	public static Packet hasPacket(Packet packet, LinkedList<Packet> list) {
		for (Packet p : list) {
			if (p.getSeqNumber() == packet.getSeqNumber()) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * This method is used to wait for a certain time
	 * during the connection tear down process.
	 * @param time The number of seconds required to wait
	 * before actually closing the connection.
	 */
	public static void wait(int time) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		t1 = System.currentTimeMillis();
		
		while ((t1 - t0) < (time * 1000)) {
			t1 = System.currentTimeMillis();
		}
	}
	
	/**
	 * Get the packet with the corresponding sequence number.
	 * @param seqNum The sequence number of the packet looking for.
	 * @return The packet with the corresponding sequence number.
	 */
	private static Packet getPacket(int seqNum) {
		for (Packet p : sentList) {
			if (p.getSeqNumber() == seqNum)
				return p;
		}
		return null;
	}
	
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
	private static int packetNum = 0;
	private static Calendar startTime = Calendar.getInstance();
	private static Calendar currentTime = Calendar.getInstance();
	private static boolean connectionEstablished = false;
	private static boolean sendFinished = false;
	private static boolean finished = false;
	private static LogFile log;

	// a list of all sequence numbers being sent out.
	private static LinkedList<Packet> sentList = new LinkedList<Packet>();
	private static LinkedList<Packet> receivedList = new LinkedList<Packet>();
	
	// constants
	private static final int FILE_SIZE = 1024 * 5;
	private static final int SOCKET_TIMEOUT = 150000;
	private static final int END_WAIT_TIME = 30; // the number of seconds to wait for when disconnecting the connection.
	
}
