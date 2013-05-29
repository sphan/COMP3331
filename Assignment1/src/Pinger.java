import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class include all needed methods in
 * sending and receiving UDP messages using UDP
 * @author sandy - z3417917
 *
 */
public class Pinger {
	
	/**
	 * Send out ping messages using UDP and waits for
	 * the response message with a time out set.
	 * @param p The peer that would like to send the ping message.
	 * @param receiverPort The receiver in which the peer would like
	 * the message to be received
	 * @return True if the ping is sent and false otherwise.
	 */
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
				
				System.out.println("A ping response message was received from Peer " + (pong.getPort() - 50000) + ".");
			} catch (IOException e) {
			}
			
		}
		return received;
	}
	
	/**
	 * This method receive ping messages using UDP
	 * and send out replies once the messages are received.
	 * @param p The peer that is continuously receiving ping
	 * messages.
	 * @throws Exception All exceptions including IOExceptions,
	 * UnknownHostExceptions, and NumberFormatExceptions.
	 */
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
				
				System.out.println("A ping request message was received from Peer " + sender + ".");
				
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
	
	private static final int TIMEOUT = 10000;
}
