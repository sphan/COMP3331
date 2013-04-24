import java.util.Timer;
import java.util.TimerTask;


public class cdht {
	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			System.out.println("Not enough arguements");
			return;
		}
		
		final Peer peer = new Peer(Integer.parseInt(args[0]));
		peer.setFirstSuccessor(Integer.parseInt(args[1]));
		peer.setSecondSuccessor(Integer.parseInt(args[2]));
		
		System.out.println("I am " + peer.getName());
		System.out.println("My first successor is " + peer.getFirstSuccessor().getName());
		System.out.println("My second successor is " + peer.getSecondSuccessor().getName());
		System.out.println("My port number is " + peer.getPortNumber());
		
		while (true) {
			Timer timer= new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						peer.getPinger().sendMessage(peer.getSocket(), 
								peer.getFirstSuccessor(), "request");
						System.out.println("My port number is " + peer.getPortNumber());
					} catch (Exception e) {
						System.out.println("Could not send ping message.");
						e.printStackTrace();
					}
				}				
			}, 0, PING_INTERVAL);
			peer.getPinger().receiveMessage(peer.getSocket(), peer);
		}
	}
	
	private static final int PING_INTERVAL = 1000; // 1000 milliseconds
}
