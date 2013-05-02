import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Pinger {
	
	public static boolean sendPing(Peer p, int receiverPort) {
		receiverPort += 50000;
		boolean received = false;
		
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		
		
		String message = "A request message from " + p.getPortNumber();
		byte[] buf = new byte[2048];
		buf = message.getBytes();
		InetAddress address = null;
		if (clientSocket != null) {
			try {
				clientSocket.setSoTimeout(TIMEOUT);
			} catch (SocketException e1) {
				System.out.println("Could not set timeout to client socket.");
			}
			try {
				address = InetAddress.getByName("127.0.0.1");
			} catch (UnknownHostException e) {
				System.out.println("Could not find host.");
			}
			
			DatagramPacket ping = new DatagramPacket(buf, buf.length, address, receiverPort);
			try {
				clientSocket.send(ping);
//				p.getSequenceNum().add(counter);
//				counter++;
			} catch (IOException e) {
			}
			
			buf = new byte[2048];
			DatagramPacket pong = new DatagramPacket(buf, buf.length);
			try {
				clientSocket.receive(pong);
				received = true;
				
				System.out.println("A ping response message was received from Peer " + (pong.getPort() - 50000));
			} catch (IOException e) {
//				p.getHasAck().add(counter);
			}
			
//			if (received) {
//				p.getHasAck().add(counter);
//				System.out.println("Ack received added");
//			} else {
//				p.getNoAck().add(counter);
//				System.out.println("Ack not received added");
//			}
//			checkPeerAlive(p);
		}
		return received;
	}
	
	public static void receivePing(Peer p) throws Exception {
		
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(p.getPortNumber());
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		byte[] buf = new byte[2048];
		DatagramPacket pong = new DatagramPacket(buf, buf.length);
		int sender = 0;
		
		if (serverSocket != null) {
			while (true) {
				serverSocket.receive(pong);
				String[] params = new String(pong.getData()).split(" ");
				sender = Integer.parseInt(params[4].trim()) - 50000;
				
				System.out.println("A ping request message was received from Peer " + sender);
				
				// set predecessor of current peer
				if (p.getFirstPreDecessor() == 0 && p.getSecondPreDecessor() == 0) {
					p.setFirstPreDecessor(sender);
				} else {
					if (p.getFirstPreDecessor() != sender || p.getSecondPreDecessor() != sender) {
						if (sender > p.getFirstPreDecessor()) {
							p.setSecondPreDecessor(p.getFirstPreDecessor());
							p.setFirstPreDecessor(sender);
						} else if (sender < p.getFirstPreDecessor()) {
							p.setSecondPreDecessor(sender);
						}
					}
				}
		
				String reply = "Message received. Response from " + p.getPortNumber();
				buf = new byte[2048];
				buf = reply.getBytes();
				DatagramPacket ping = new DatagramPacket(buf, buf.length, pong.getAddress(), pong.getPort());
				
				try {
					serverSocket.send(ping);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	public static boolean checkPeerAlive(Peer p) {
//		LinkedList<Integer> seqList = p.getSequenceNum();
//		LinkedList<Integer> noAckList = p.getNoAck();
//		LinkedList<Integer> hasAckList = p.getHasAck();
////		boolean isConsecutive = false;
//		int consecutiveCount = 0;
//		boolean peerIsAlive = true;
//		
//		if (seqList.size() > 5) {
//			while (seqList.size() > 0 && 
//					hasAckList.size() > 0 && 
//					noAckList.size() > 0) {
//				if (seqList.peek() != hasAckList.peek()) {
////					System.out.println("Seq #" + seqList.peek() + " is not in received ack list");
//					if (seqList.peek() == noAckList.peek()) {
////						System.out.println("Seq #" + seqList.peek() + " is in not-received ack list");
//						consecutiveCount++;
//						noAckList.pop();
//					}
//				} else {
////					System.out.println("Seq #" + seqList.peek() + " is in received ack list");
//					consecutiveCount = 0;
//					hasAckList.pop();
//				}
//				seqList.pop();
//				System.out.println("Consecutive ping numbers: " + consecutiveCount);
//				if (consecutiveCount > 5) {
//					System.out.println("5 pings have passed. Peer is not alive");
//					System.out.println(5);
//					peerIsAlive = false;
//				}
//			}
//		}
//		return peerIsAlive;
//	}
	
	private static final int TIMEOUT = 10000;
//	private static int counter = 0;
}
