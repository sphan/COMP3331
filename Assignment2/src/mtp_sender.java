import java.net.InetAddress;


public class mtp_sender {
	public static void main(String args[]) throws Exception {
		if (args.length < 8) {
			System.out.println("Not enough arguements");
			return;
		}
		
		InetAddress server = InetAddress.getByName(args[0]);
		int receiverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		int mws = Integer.parseInt(args[3]);
		int mss = Integer.parseInt(args[4]);
		int timeout = Integer.parseInt(args[5]);
		
		// calculate the number of packets can be
		// sent at once.
		int packetNum = mws / mss;
	}
}
