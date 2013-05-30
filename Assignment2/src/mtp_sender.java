import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
		
		DatagramSocket socket = new DatagramSocket();
		FileInputStream fis = new FileInputStream(new File(fileName));
		
		try {
			byte[] buff = new byte[mss];
			int remaining = buff.length;
			while (true) {
//				int read = fis.read(buff, buff.length - remaining, remaining);
//				if (read > 0) {
//					remaining -= read;
//					if (remaining == 0) {
//						System.out.println("sending data");
//						DatagramPacket packet = new DatagramPacket(buff, buff.length, server, receiverPort);
//						socket.send(packet);
////						System.out.println(new String(packet.getData()));
//						remaining = buff.length;
//					}
//				}
				
//				try {
//					socket.setSoTimeout(timeout);
//					
//					DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
//					
//					socket.receive(response);
//					System.out.println("Received response");
//				} catch (IOException e) {
//					System.out.println("Timeout");
//				}
			}
		} finally {
			fis.close();
		}
	}
	
	public boolean establishConnection(DatagramSocket socket) throws Exception {
		Package establishment = new Package(0);
		establishment.setSYN(true);
		
		while (!connectionEstalished) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(establishment);
		}
		return false;
	}
	
	private static boolean connectionEstalished = false;
}
