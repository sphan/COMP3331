import java.util.Timer;
import java.util.TimerTask;

public class cdht {
	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			System.out.println("Not enough arguements");
			return;
		}
		
		int count = 0;
		
		final Peer peer = new Peer(Integer.parseInt(args[0]));
		peer.setFirstSuccessor(Integer.parseInt(args[1]));
		peer.setSecondSuccessor(Integer.parseInt(args[2]));
		
		System.out.println("I am " + peer.getName());
		System.out.println("My first successor is " + peer.getFirstSuccessor());
		System.out.println("My second successor is " + peer.getSecondSuccessor());
		System.out.println("My port number is " + peer.getPortNumber());
		
		while (count < 20) {
//			Timer timer = new Timer(true);
//			timer.scheduleAtFixedRate(new TimerTask() {
//				@Override
//				public void run() {
//					System.out.println("Testing");
					peer.setClient(peer.getPortNumber(), peer.getFirstSuccessor() + 50000);
					peer.getClient().run();
					peer.setClient(peer.getPortNumber(), peer.getSecondSuccessor() + 50000);
					peer.getClient().run();
//				}
//			}, DELAY, PING_INTERVAL);
			peer.getServer().run();
			count++;
		}
	}
	
	private static final long DELAY = 5*1000L;
	private static final long PING_INTERVAL = 5*1000L; // 10 seconds
	private static Peer peer;
}
