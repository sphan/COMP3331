import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class PingerClient implements Runnable {
	public PingerClient(int myPort, int recPort) {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Could not create socket");
		}
		this.receiverPort = recPort;
		this.myPort = myPort;
	}
	
	@Override
	public void run() {
		String message = "A request message from " + myPort;
		byte[] buf = new byte[1024];
		buf = message.getBytes();
		InetAddress address = null;
//		try {
//			socket.setSoTimeout(TIMEOUT);
//		} catch (SocketException e1) {
////			System.out.println("Could not set timeout to client socket.");
//		}
		try {
			address = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
//			System.out.println("Could not find host.");
		}
		DatagramPacket ping = new DatagramPacket(buf, buf.length, address, receiverPort);
		try {
			socket.send(ping);
		} catch (IOException e) {
//			System.out.println("Could not send ping to " + receiverPort);
		}
	}
	
	private int receiverPort = 0;
	private int myPort = 0;
	private DatagramSocket socket = null;
	private static final int TIMEOUT = 5000;
}
