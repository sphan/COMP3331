import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Pinger {
	
	public Pinger() {
		msg = "";
	}
	
	public void sendMessage(DatagramSocket mySocket, Peer receiver, String messageType) throws Exception {
		int port = receiver.getPortNumber();
		byte[] buffer = new byte[1024];
		
		if (messageType.equalsIgnoreCase("Request")) {
			msg ="This is a request message, Peer ";
		} else if (messageType.equalsIgnoreCase("Response")) {
			msg = "This is a response message, Peer ";
		}
		
		msg +=  receiver.getName() + ".";
		
		buffer = msg.getBytes();
		
		InetAddress server = InetAddress.getByName("127.0.0.1");
		DatagramPacket ping = new DatagramPacket(buffer, buffer.length, server, port);
		
		mySocket.send(ping);
	}
	
	public void receiveMessage(DatagramSocket mySocket, Peer me) throws Exception {
		DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
		int clientId = 0;

		mySocket.receive(response);
		String message = response.toString();
		
		Pattern p = Pattern.compile("\\d");
		Matcher m = p.matcher(message);
		while (m.find())
			clientId = m.groupCount();
		Peer client = me.findSuccessor(clientId);
		
		if (message.contains("request")) {
			System.out.println("A ping request message was received from Peer " + 
					clientId + ".");
			me.getPinger().sendMessage(mySocket, client, "response");
		} else if (message.contains("response")) {
			msg = "A ping response message was received from Peer " + clientId + ".";
		}
	}
	
	private String msg;
}
