import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
		socket = new DatagramSocket(myPort);
		
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
			
			if (p.isSYN()) {
				System.out.println("SYN packet received");
				establishConnection(p);
			}
			
			if (p.isFIN()) {
				closeConnection();
			}
			
			if (connectionEstablished) {
				if (p.getData().length > 0) {
					fos.write(p.getData());
					fos.flush();
				}
			}
			
			if (connectionEstablished == false)
				System.exit(0);
		}
		
	}
	
	public static void establishConnection(Package requestPacket) throws Exception {
		reply = new Package(SERVER_ISN);
		reply.setACK(true);
		reply.setSYN(true);
		reply.setAckNumber(requestPacket.getSeqNumber() + 1);
		
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		System.out.println("SYNACK sent");
		
		try {
			DatagramPacket responseFromClient = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(responseFromClient);
			
			Package p = Serialisation.deserialise(responseFromClient);
			if (p.isACK()) {
				clientAddress = responseFromClient.getAddress();
				clientPort = responseFromClient.getPort();
				connectionEstablished = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeConnection() throws Exception {
		reply = new Package(SERVER_ISN);
		reply.setACK(true);
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		
		reply.setACK(false);
		reply.setFIN(true);
		socket.send(Serialisation.serialise(reply, clientAddress, clientPort));
		
		try {
			DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
			socket.receive(response);
			
			Package p = Serialisation.deserialise(response);
			if (p.isACK()) {
				clientAddress = null;
				clientPort = '0';
				connectionEstablished = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Package reply;
	private static DatagramSocket socket;
	private static InetAddress clientAddress;
	private static int clientPort;
	private static boolean connectionEstablished = false;
	private static final int FILE_SIZE = 1024 * 5;
	private static final int SERVER_ISN = 6;
}
