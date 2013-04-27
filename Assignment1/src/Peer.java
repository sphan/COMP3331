
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
		server = new PingerServer(portNumber);
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
		this.firstSuccessor = aIdentity;
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
		this.secondSuccessor = aIdentity;
	}
	
	/**
	 * Get the first successor of the current peer.
	 * @return Return a Peer which is the first successor
	 * of the current peer.
	 */
	public int getFirstSuccessor() {
		return this.firstSuccessor;
	}
	
	/**
	 * Get the second successor of the current peer.
	 * @return Return a Peer which is the second successor
	 * of the current peer.
	 */
	public int getSecondSuccessor() {
		return this.secondSuccessor;
	}
	
	public int findSuccessor(int id) {
		if (id == firstSuccessor)
			return firstSuccessor;
		else if (id == secondSuccessor)
			return secondSuccessor;
		return 0;
	}
	
	public PingerClient getClient() {
		return this.client;
	}

	public void setClient(int port, int recPort) {
		this.client = new PingerClient(port, recPort);
	}

	public PingerServer getServer() {
		return this.server;
	}
	
	/**
	 * Get the port number in which the current peer is
	 * listening to.
	 * @return An integer that specifies the port number.
	 */
	public int getPortNumber() {
		return this.portNumber;
	}

	private int identity;
	private int portNumber = 50000;
	private int firstSuccessor;
	private int secondSuccessor;
	private PingerServer server;
	private PingerClient client;
	
}
