import java.net.DatagramSocket;


public class Peer {
	/**
	 * Create a Peer object with its identity being
	 * assigned using the input parameter and its port
	 * number as the sum of 5000 and the input parameter.
	 * @param aIdentity An integer that specifies the
	 * identity of the new Peer. This integer will be used
	 * to determine the port number in which this new Peer
	 * will listen to.
	 * @throws Exception 
	 */
	public Peer(int aIdentity) throws Exception {
		this.identity = aIdentity;
		this.portNumber = this.portNumber + aIdentity;
		socket = new DatagramSocket();
		this.pinger = new Pinger();
	}

	/**
	 * Get name or identity of the current
	 * peer.
	 * @return An integer that specifies the identity
	 * of the peer.
	 */
	public int getName() {
		return this.identity;
	}
	
	/**
	 * Set the first successor of the current peer to
	 * the given identity.
	 * @param aIdentity An integer that specifies peer
	 * which will be assigned to the current peer as its
	 * first successor.
	 * @throws Exception 
	 */
	public void setFirstSuccessor(int aIdentity) throws Exception {
		this.firstSuccessor = new Peer(aIdentity);
	}
	
	/**
	 * Set the second successor of the current peer to
	 * the given identity.
	 * @param aIdentity An integer that specifies the peer
	 *  which will be assigned to the current peer as its
	 *  second successor.
	 * @throws Exception 
	 */
	public void setSecondSuccessor(int aIdentity) throws Exception {
		this.secondSuccessor = new Peer(aIdentity);
	}
	
	/**
	 * Get the first successor of the current peer.
	 * @return Return a Peer which is the first successor
	 * of the current peer.
	 */
	public Peer getFirstSuccessor() {
		return this.firstSuccessor;
	}
	
	/**
	 * Get the second successor of the current peer.
	 * @return Return a Peer which is the second successor
	 * of the current peer.
	 */
	public Peer getSecondSuccessor() {
		return this.secondSuccessor;
	}
	
	public Peer findSuccessor(int id) {
		if (id == firstSuccessor.getName())
			return firstSuccessor;
		else if (id == secondSuccessor.getName())
			return secondSuccessor;
		return null;
	}
	
	public Pinger getPinger() {
		return pinger;
	}
	
	/**
	 * Get the port number in which the current peer is
	 * listening to.
	 * @return An integer that specifies the port number.
	 */
	public int getPortNumber() {
		return this.portNumber;
	}
	
	public DatagramSocket getSocket() {
		return this.socket;
	}

	private int identity;
	private int portNumber = 5000;
	private Peer firstSuccessor;
	private Peer secondSuccessor;
	private DatagramSocket socket;
	private Pinger pinger;
	
}
