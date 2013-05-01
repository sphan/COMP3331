import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;


public class Pinger {
	
	public static void sendPing(Peer p, int receiverPort) {
		receiverPort += 50000;
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		
		
		String message = "A request message from " + p.getPortNumber();
//		System.out.println(message);
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
			
			DatagramPacket pong = new DatagramPacket(buf, buf.length);
//			System.out.println(clientSocket.getPort());
			try {
				clientSocket.receive(pong);
//				String[] params = new String(pong.getData()).split(" ");
//				int ack = Integer.parseInt(params[8]);
//				p.getAckNum().add(ack);
//				System.out.println(new String(pong.getData()));
				
				System.out.println("A ping response message was received from Peer " + (pong.getPort() - 50000));

			} catch (IOException e) {
				System.out.println("1");
			}
		}
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
//				System.out.println("Running");
				serverSocket.receive(pong);
				String[] params = new String(pong.getData()).split(" ");
				sender = Integer.parseInt(params[4].trim()) - 50000;
//				int ackNum = Integer.parseInt(params[params.length - 1].trim());
//				System.out.println("Receive message from " + pong.getPort() + " with sequence number " + ackNum);
				
				System.out.println("A ping request message was received from Peer " + (sender));
				
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
				buf = reply.getBytes();
				DatagramPacket ping = new DatagramPacket(buf, buf.length, pong.getAddress(), pong.getPort());
				
				try {
					serverSocket.send(ping);
				} catch (IOException e) {
					e.printStackTrace();
				}
//				System.out.println(pong.getPort());
			}
		}
	}
	
	private static final int TIMEOUT = 10000;
//	private static int counter = 0;
}
