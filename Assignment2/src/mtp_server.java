import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class mtp_server {
	public static void main(String args[]) throws Exception {
		if (args.length < 2) {
			System.out.println("Not enough arguements");
			return;
		}
		
		int myPort = Integer.parseInt(args[0]);
		String fileName = args[1];
		int clientPort;
		InetAddress clientAddress;
		
		DatagramSocket socket = new DatagramSocket(myPort);
		File file = new File(fileName);
		FileOutputStream fos = new FileOutputStream(file);
		
		if (!file.exists())
			file.createNewFile();
		
		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(request);
			System.out.println("Received data");
			System.out.println(new String(request.getData()));
//			System.out.println(request.getLength());
			
			ByteArrayInputStream bis = new ByteArrayInputStream(request.getData());
			ObjectInputStream in = new ObjectInputStream(bis);
			Package p = (Package) in.readObject();
//				System.out.println(p.isSYN());
			System.out.println(p.getSeqNumber());
			
			Package reply;
			if (p.isSYN()) {
				System.out.println("SYN packet received");
				reply = new Package(SERVER_ISN);
				reply.setACK(true);
				reply.setSYN(true);
				reply.setAckNumber(p.getSeqNumber() + 1);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(reply);
				out.close();
				
				clientAddress = request.getAddress();
				clientPort = request.getPort();
				
				DatagramPacket response = new DatagramPacket(bos.toByteArray(), bos.size(), clientAddress, clientPort);
				socket.send(response);
				System.out.println("Response sent");
			} else {
				if (p.getData().length > 0) {
					fos.write(p.getData());
					fos.flush();
				}
			}
		}
		
	}
	
	private static final int FILE_SIZE = 1024 * 5;
	private static final int SERVER_ISN = 6;
}
