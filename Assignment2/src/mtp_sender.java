import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

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
		int mss = Integer.parseInt(args[4]);
		int timeout = Integer.parseInt(args[5]);
		pdrop = Float.parseFloat(args[6]);
		seed = Integer.parseInt(args[7]);
		
		int read = 0; // to store the number of bytes read from file.
		int seqNum = 0; // sequence number
		boolean finished = false; // a flag to see if everything is done
		
		// calculate the number of packets can be
		// sent at once.
		int packetNum = mws / mss;
		
		int sentNum = 0; // the number of packets sent
		
		socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		
		// get the file.
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		
		try {
			byte[] buff = new byte[mss];
			int remaining = buff.length;
			establishConnection(socket, server, receiverPort);
			while (!finished) {
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
							}
						}
					
						// send data
						p.setData(buff);
						p.setSeqNumber(seqNum);
						socket.send(Serialisation.serialise(p, server, receiverPort));
						sentList.add(p.getSeqNumber()); // add sequence number to list
						seqNum += read;
						sentNum++;
						
						System.out.println("Packet segment with seq # " + p.getSeqNumber() + " sent");
					}
					
				}
				
				// receive response from server.
				try {
					socket.setSoTimeout(timeout);
					
					DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
					socket.receive(response);
					
					Packet responsePacket = Serialisation.deserialise(response);
					if (sentList.size() > 0 && sentList.contains(responsePacket.getAckNumber() - read)) {
						sentList.remove(responsePacket.getAckNumber());
						sentNum--;
					}
					System.out.println("Received ACK # " + responsePacket.getAckNumber());
				} catch (IOException e) {
					System.out.println("Timeout");
				}
				
				// close the connection and exit after everything is sent out.
				if (finished) {
					closeConnection(socket, server, receiverPort);
					System.exit(0);
				}
			}
		} finally {
			fis.close();
		}
	}
	
	public static void establishConnection(DatagramSocket socket, InetAddress server, int port) throws Exception {
		Packet establishment = new Packet(CLIENT_ISN);
		establishment.setSYN(true);
		
		while (!connectionEstablished) {
			// send connection establishment request.
			System.out.println("Establishing connection:");
			socket.send(Serialisation.serialise(establishment, server, port));
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
					
					establishment.setSeqNumber(establishment.getSeqNumber() + 1);
					establishment.setAckNumber(p.getSeqNumber() + 1);
					establishment.setSYN(false);
					establishment.setACK(true);
					socket.send(Serialisation.serialise(establishment, server, port));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean closeConnection(DatagramSocket socket, InetAddress server, int port) throws Exception {
		Packet close = new Packet(CLIENT_ISN);
		close.setFIN(true);
		boolean connectionClosed = false;
		
		while (!connectionClosed) {
			// send out FIN request to server.
			System.out.println("Closing connection");
			DatagramPacket packet = Serialisation.serialise(close, server, port);
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
	
	public static void wait(int time) {
		System.out.println("Waiting 30 secs");
		long t0, t1;
		t0 = System.currentTimeMillis();
		t1 = System.currentTimeMillis();
		
		while ((t1 - t0) < (time * 1000)) {
			t1 = System.currentTimeMillis();
		}
	}
	
	private static DatagramSocket socket;
	private static InetAddress server;
	private static int receiverPort;
	private static float pdrop;
	private static int seed;
	private static boolean connectionEstablished = false;
	private static final int FILE_SIZE = 1024 * 5;
	private static final int CLIENT_ISN = 3;
	
	// a list of all sequence numbers being sent out.
	private static LinkedList<Integer> sentList = new LinkedList<Integer>();
}
