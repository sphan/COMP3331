import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;


public class Packet implements Serializable {
	
	public Packet(int seqNum) {
		this.seqNumber = seqNum;
		this.SYN = false;
		this.FIN = false;
		this.RST = false;
		this.PSH = false;
		this.ACK = false;
		this.URG = false;
	}
	
	public int getSeqNumber() {
		return seqNumber;
	}
	
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}
	
	public int getAckNumber() {
		return ackNumber;
	}
	
	public void setAckNumber(int ackNumber) {
		this.ackNumber = ackNumber;
	}
	
	public boolean isSYN() {
		return SYN;
	}
	
	public void setSYN(boolean SYN) {
		this.SYN = SYN;
	}
	
	public boolean isFIN() {
		return FIN;
	}
	
	public void setFIN(boolean FIN) {
		this.FIN = FIN;
	}
	
	public boolean isRST() {
		return RST;
	}
	
	public void setRST(boolean RST) {
		this.RST = RST;
	}
	
	public boolean isPSH() {
		return PSH;
	}
	
	public void setPSH(boolean PSH) {
		this.PSH = PSH;
	}
	
	public boolean isACK() {
		return ACK;
	}
	
	public void setACK(boolean ACK) {
		this.ACK = ACK;
	}
	
	public boolean isURG() {
		return URG;
	}
	
	public void setURG(boolean URG) {
		this.URG = URG;
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * This method calculates the amount of time elapsed since
	 * the packet was sent out.
	 * @param startTime The time when the packet was sent out
	 * @return The number of milliseconds elapsed.
	 */
	public long calculateTimeElapse() {
		Calendar endTime = Calendar.getInstance();
		Date st = startTime.getTime();
		Date et = endTime.getTime();
		long ls = st.getTime();
		long le = et.getTime();
		long diff = (le - ls);
		return diff;
	}
	
	private int seqNumber;
	private int ackNumber;
	private boolean SYN;
	private boolean FIN;
	private boolean RST;
	private boolean PSH;
	private boolean ACK;
	private boolean URG;
	private byte[] data;
	
	// The time when the packet was sent out.
	private Calendar startTime;
	/**
	 * Default serialization ID
	 */
	private static final long serialVersionUID = 1L;
}
