import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class mtp_server {
	public static void main(String args[]) throws Exception {
		if (args.length < 2) {
			System.out.println("Not enough arguements");
			return;
		}
		
		int myPort = Integer.parseInt(args[0]);
		String fileName = args[1];
		
		DatagramSocket socket = new DatagramSocket(myPort);
		
		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(request);
			System.out.println("Received data");
			System.out.println(new String(request.getData()));
		}
	}
	
	private static final int FILE_SIZE = 1024 * 5;
}
