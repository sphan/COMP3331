import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class cdht {
	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			System.out.println("Not enough arguements");
			return;
		}
		
		/**
		 * Initialisation.
		 */
		peer = new Peer(Integer.parseInt(args[0]));
		peer.setFirstSuccessor(Integer.parseInt(args[1]));
		peer.setSecondSuccessor(Integer.parseInt(args[2]));
		
		System.out.println("I am " + peer.getName());
		System.out.println("My first successor is " + peer.getFirstSuccessor());
		System.out.println("My second successor is " + peer.getSecondSuccessor());
		System.out.println("My port number is " + peer.getPortNumber());
		
		/**
		 * A new thread used to constantly listen for inputs
		 * for each peer.
		 */
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					Scanner input = new Scanner(System.in);
					String s = input.nextLine();
					if (s.contains("request") || s.contains("Request")) {
						String params[] = s.split(" ");
						DHTFile f = new DHTFile(params[1]);
						try {
							TCPSockets.sendRequest(peer, f);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else  if (s.contains("quit") || s.contains("Quit")) {
						try {
							TCPSockets.sendQuitRequest(peer);
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}
			}
		})).start();
		
		/**
		 * Call the pinger to ping the 2 successors.
		 */
		pingFirstSuccessor();
		pingSecondSuccessor();
		
		/**
		 * A new thread open to constantly listen to
		 * its port for ping requests through UDP.
		 */
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Pinger.receivePing(peer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
		
		/**
		 * A new thread used for constantly listening to
		 * its port for file request messages through TCP.
		 */
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TCPSockets.receiveRequest(peer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
	}
	
	public static void pingFirstSuccessor() {
		final Runnable firstSuccessor = new Runnable() {
			@Override
			public void run() {
				Pinger.sendPing(peer, peer.getFirstSuccessor());
			}
		};
		
		scheduler1.scheduleAtFixedRate(firstSuccessor, DELAY, PING_INTERVAL, TimeUnit.SECONDS);
	}
	
	public static void pingSecondSuccessor() {
		final Runnable secondSuccessor = new Runnable() {
			@Override
			public void run() {
				Pinger.sendPing(peer, peer.getSecondSuccessor());
			}
		};
		
		scheduler2.scheduleAtFixedRate(secondSuccessor, DELAY, PING_INTERVAL, TimeUnit.SECONDS);
	}
	
	private static final long DELAY = 0L;
	private static final long PING_INTERVAL = 10L; // 10 seconds
	private static Peer peer;
	private final static ScheduledExecutorService scheduler1 =
			Executors.newScheduledThreadPool(1);
	private final static ScheduledExecutorService scheduler2 =
			Executors.newScheduledThreadPool(1);
}
