import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;


public class Serialisation {
	public static DatagramPacket serialise(Packet p, InetAddress server, int port)  throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(p);
		out.close();
		
		DatagramPacket packet = new DatagramPacket(bos.toByteArray(), bos.size(), server, port);
		return packet;
	}
	
	public static Packet deserialise(DatagramPacket dp) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(dp.getData());
		ObjectInputStream in = new ObjectInputStream(bis);
		in.close();
		return ((Packet) in.readObject());
	}
}
