import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Pinger {
	
	public Pinger(DatagramSocket aSocket) {
		this.requestMessage = "Are you still alive, Peer ";
		this.requestResponseMessage = "A ping request message was received from Peer ";
		this.responseMessage = "I'm still alive, Peer ";
		this.responseResponseMessage = "A ping response message was received from Peer ";
		this.socket = aSocket;
	}

	public void sendRequestMessage(Peer receiver) throws Exception {
		int port = receiver.getPortNumber();
		byte[] buffer = new byte[1024];
		requestMessage = requestMessage + receiver.getName() + ".";
		buffer = requestMessage.getBytes();

		InetAddress server = InetAddress.getByName(Integer.toString(receiver.getName()));
		DatagramPacket ping = new DatagramPacket(buffer, buffer.length, server, port);
		
		socket.send(ping);
	}
	
	public void sendResponseMessage(Peer receiver) throws Exception {
		int port = receiver.getPortNumber();
		byte[] buffer = new byte[1024];
		responseMessage = responseMessage + receiver.getName() + ".";
		buffer = responseMessage.getBytes();

		InetAddress server = InetAddress.getByName(Integer.toString(receiver.getName()));
		DatagramPacket ping = new DatagramPacket(buffer, buffer.length, server, port);
		
		socket.send(ping);
	}
	
	public void receiveRequestMessage() throws Exception {
		DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
		socket.receive(response);
		int senderId = 0;
		
		Pattern p = Pattern.compile("\\d");
		Matcher m = p.matcher(response.toString());
		while (m.find())
			senderId = Integer.parseInt(m.group());
		System.out.println(requestResponseMessage + senderId + ".");
	}
	
	public void receiveResponseMessage() throws Exception {
		DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
		socket.receive(response);
		int senderId = 0;
		
		Pattern p = Pattern.compile("\\d");
		Matcher m = p.matcher(response.toString());
		while (m.find())
			senderId = Integer.parseInt(m.group());
		System.out.println(responseResponseMessage + senderId + ".");
	}
	
	private String requestMessage;
	private String requestResponseMessage;
	private String responseMessage;
	private String responseResponseMessage;
	private DatagramSocket socket;
}
