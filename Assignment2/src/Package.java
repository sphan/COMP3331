import java.io.Serializable;


public class Package implements Serializable {
	
	public Package(int seqNum) {
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
	
	private int seqNumber;
	private int ackNumber;
	private boolean SYN;
	private boolean FIN;
	private boolean RST;
	private boolean PSH;
	private boolean ACK;
	private boolean URG;
	private byte[] data;
	/**
	 * Default serialization ID
	 */
	private static final long serialVersionUID = 1L;
}
