
public class cdht {
	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			System.out.println("Not enough arguements");
			return;
		}
		
		Peer peer = new Peer(Integer.parseInt(args[0]));
		peer.setFirstSuccessor(Integer.parseInt(args[1]));
		peer.setSecondSuccessor(Integer.parseInt(args[2]));
		
		System.out.println(peer.getName());
		System.out.println(peer.getFirstSuccessor().getName());
		System.out.println(peer.getSecondSuccessor().getName());
	}
}
