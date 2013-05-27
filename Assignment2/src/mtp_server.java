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
			
		}
	}
}
