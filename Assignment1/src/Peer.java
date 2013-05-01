import java.util.LinkedList;


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
	
	/**
	 * Find the successor by its identity and return
	 * which successor it is to the current peer if found.
	 * @param id The id of the peer that is looking for.
	 * @return The successor's id or 0 if not found.
	 */
	public int findSuccessor(int id) {
		if (id == firstSuccessor)
			return firstSuccessor;
		else if (id == secondSuccessor)
			return secondSuccessor;
		return 0;
	}
	
	/**
	 * Get the port number in which the current peer is
	 * listening to.
	 * @return An integer that specifies the port number.
	 */
	public int getPortNumber() {
		return this.portNumber;
	}
	
	/**
	 * This method determines if a file is stored in the
	 * current peer.
	 * @param hashCode The hash code of the file requested.
	 * @return True if the hash code of the file is in the
	 * responsibility range of the peer. False otherwise.
	 */
	public boolean containFile(int hashCode) {
		if (this.getName() > this.getFirstPreDecessor() && 
				this.getName() < this.getFirstSuccessor()) {
			if (hashCode <= this.getName() && hashCode > this.getFirstPreDecessor())
				return true;
		} else if (this.getName() > this.getFirstPreDecessor() && 
				this.getName() > this.getFirstSuccessor()) {
			if (hashCode <= this.getName() && hashCode > this.getFirstPreDecessor())
				return true;
		} else if (this.getName() < this.getFirstPreDecessor() && 
				this.getName() < this.getFirstSuccessor()) {
			if (hashCode > this.getName())
				return false;
			return true;
		}
		return false;
	}

	/**
	 * Get the first predecessor of the current peer.
	 * Should be the peer that is immediate before the current peer.
	 * @return The peer that is right before current peer.
	 */
	public int getFirstPreDecessor() {
		return firstPreDecessor;
	}

	/**
	 * Set the first predecessor of the current peer.
	 * @param firstPreDecessor The identity of the first predecessor.
	 */
	public void setFirstPreDecessor(int firstPreDecessor) {
		this.firstPreDecessor = firstPreDecessor;
	}

	/**
	 * Get the second predecessor of the current peer.
	 * @return The peer that is before the first predecessor
	 * of current peer.
	 */
	public int getSecondPreDecessor() {
		return secondPreDecessor;
	}

	/**
	 * Set the second predecessor of the current peer.
	 * @param secondPreDecessor The identity of the second predecessor.
	 */
	public void setSecondPreDecessor(int secondPreDecessor) {
		this.secondPreDecessor = secondPreDecessor;
	}

	/**
	 * Returns the list of sequence numbers it has sent out.
	 * @return A linked list of sequence numbers.
	 */
	public LinkedList<Integer> getSequenceNum() {
		return sequenceNum;
	}
	
	/**
	 * Returns the list of acknowledge numbers it has received.
	 * @return A linked list of acknowledged numbers.
	 */
	public LinkedList<Integer> getAckNum() {
		return ackNum;
	}

	private int identity;
	private int portNumber = 50000;
	private int firstSuccessor;
	private int secondSuccessor;
	private int firstPreDecessor = 0;
	private int secondPreDecessor = 0;
	private static LinkedList<Integer> sequenceNum = new LinkedList<Integer>();
	private static LinkedList<Integer> ackNum = new LinkedList<Integer>();
	
}
