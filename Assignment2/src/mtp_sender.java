import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
		
		server = InetAddress.getByName(args[0]);
		receiverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		int mws = Integer.parseInt(args[3]);
		int mss = Integer.parseInt(args[4]);
		int timeout = Integer.parseInt(args[5]);
		int seqNum = 0;
		boolean finished = false;
		
		// calculate the number of packets can be
		// sent at once.
		int packetNum = mws / mss;
		
		socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		
		if (!file.exists())
			file.createNewFile();
		
		try {
			byte[] buff = new byte[mss];
			int remaining = buff.length;
			while (!finished) {
				if (establishConnection(socket, server, receiverPort)) {
					Package p;
					int read = fis.read(buff, buff.length - remaining, remaining);
					p = new Package(seqNum);
					if (read >= 0) {
						remaining -= read;
						if (remaining == 0) { 
							remaining = buff.length;
							seqNum += read;
						} else {
							seqNum += read;
							finished = true;
						}
					}
					
					p.setData(buff);
					p.setSeqNumber(seqNum);
					DatagramPacket pt = new DatagramPacket(buff, buff.length, server, receiverPort);
					System.out.println(new String(new String(p.getData().toString())));
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(bos);
					out.writeObject(p);
					DatagramPacket packet = new DatagramPacket(bos.toByteArray(), bos.size(), server, receiverPort);
					socket.send(packet);
					System.out.println("Packet segment sent");
				}
				
				try {
					socket.setSoTimeout(timeout);
					
					DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
					
					socket.receive(response);
					System.out.println("Received response");
				} catch (IOException e) {
					System.out.println("Timeout");
				}
				
				if (finished) {
					closeConnection(socket, server, receiverPort);
				}
			}
		} finally {
			fis.close();
		}
	}
	
	public static boolean establishConnection(DatagramSocket socket, InetAddress server, int port) throws Exception {
		Package establishment = new Package(CLIENT_ISN);
		establishment.setSYN(true);
		boolean connectionEstalished = false;
//		DatagramPacket packet = new DatagramPacket(buff, buff.length, server, port);
		
		while (!connectionEstalished) {
			System.out.println("Establishing connection:");
			socket.send(Serialisation.serialise(establishment, server, port));
			System.out.println("Establishment written");
			
			try {
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				System.out.print(response.getData());
				System.out.println("Establishment packet: " + establishment);
				Package p = Serialisation.deserialise(response);
				
				if (p.isACK() && p.isSYN()) {
					System.out.println("SYNACK received");
					connectionEstalished = true;
					
					establishment.setSeqNumber(establishment.getSeqNumber() + 1);
					establishment.setAckNumber(p.getSeqNumber() + 1);
					socket.send(Serialisation.serialise(establishment, server, port));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return connectionEstalished;
	}
	
	public static boolean closeConnection(DatagramSocket socket, InetAddress server, int port) throws Exception {
		Package close = new Package(CLIENT_ISN);
		close.setFIN(true);
		boolean connectionClosed = false;
		
		while (!connectionClosed) {
			System.out.println("Closing connection");
			DatagramPacket packet = Serialisation.serialise(close, server, port);
			socket.send(packet);
			System.out.println("Closing packet sent");
			
			try {
				DatagramPacket response = new DatagramPacket(new byte[FILE_SIZE], FILE_SIZE);
				socket.receive(response);
				System.out.print(response.getData());
				System.out.println("Close packet: " + close);
				Package p = Serialisation.deserialise(response);
				
				if (p.isACK()) {
					System.out.println("Received ACK, Waiting for FIN from server");
					socket.receive(response);
					p = Serialisation.deserialise(response);
					
					if (p.isFIN()) {
						wait(30);
						System.out.println("Connection Closed");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static void wait(int time) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		t1 = System.currentTimeMillis();
		
		while ((t1 - t0) < (time * 1000)) {
			t1 = System.currentTimeMillis();
		}
	}
	
	private static DatagramSocket socket;
	private static InetAddress server;
	private static int receiverPort;
	private static final int FILE_SIZE = 1024 * 5;
	private static final int CLIENT_ISN = 3;
}
