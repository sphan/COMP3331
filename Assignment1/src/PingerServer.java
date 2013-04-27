import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
//import java.util.TimerTask;

public class PingerServer implements Runnable {

	public PingerServer(int port) {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Could not create socket.");
		}
		myPort = port;
	}
	
	@Override
	public void run() {
		byte[] buf = new byte[1024];
		DatagramPacket pong = new DatagramPacket(buf, buf.length);
		String msg = null;
		String senderPort = null;
//		try {
//			socket.setSoTimeout(TIMEOUT);
//		} catch (SocketException e1) {
////			System.out.println("Could not set timeout to socket.");
//		}
		try {
			socket.receive(pong);
			msg = new String(pong.getData());
		} catch (IOException e) {
//			System.out.println("Could not receive pong.");
		}
		
		senderPort = msg.replaceAll("\\D+", "");
		
		if (msg.contains("request") || msg.contains("Request"))
			System.out.println("A ping request message was received from Peer " + (Integer.parseInt(senderPort) - 50000));
		else if (msg.contains("response") || msg.contains("Response"))
			System.out.println("A ping response message was received from Peer " + (Integer.parseInt(senderPort) - 50000));

		
//		try {
//			socket.setSoTimeout(TIMEOUT);
//		} catch (SocketException e1) {
//			// TODO Auto-generated catch block
////			e1.printStackTrace();
//		}
		String reply = "Message received. Response from " + myPort;
		buf = reply.getBytes();
		DatagramPacket ping = new DatagramPacket(buf, buf.length, pong.getAddress(), Integer.parseInt(senderPort));
		try {
			socket.send(ping);
		} catch (IOException e) {
		}
		
	}
	
	private DatagramSocket socket = null;
	private int myPort = 0;
	private static final int TIMEOUT = 15000;
}
