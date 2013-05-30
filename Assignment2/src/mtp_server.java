import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
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
//			DatagramPacket request = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
//			socket.receive(request);
//			System.out.println("Received data");
//			System.out.println(new String(request.getData()));
			
			ByteArrayInputStream bis = new ByteArrayInputStream(new byte[FILE_SIZE]);
			ObjectInputStream in = new ObjectInputStream(bis);
			Package p = (Package) in.readObject();
			System.out.println(p.isSYN());
		}
	}
	
	private static final int FILE_SIZE = 1024 * 5;
}
