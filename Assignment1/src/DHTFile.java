
public class DHTFile {

	/**
	 * Create a new file with the input name.
	 * It also checks if the input name is in the correct length
	 * and is under the correct format.
	 * @param aName A string of numbers of length 4.
	 */
	public DHTFile(String aName) {
		if (aName.length() != 4) {
			System.out.println("Invalid input");
			return;
		}
		try {
			this.fileName = Integer.parseInt(aName);
		} catch (NumberFormatException e) {
			System.out.println("Invalid input");
		}
		this.name = aName;
		setHashCode();
	}
	
	public int hash(int name) {
		int hashCode = name % HASH;
		return hashCode;
	}
	
	public String getFileName() {
		return this.name;
	}
	
	public int getFileId() {
		return this.fileName;
	}

	public int getHashCode() {
		return this.hashCode;
	}

	public void setHashCode() {
		this.hashCode = this.hash(this.fileName);
	}

	private String name;
	private int fileName;
	private int hashCode;
	private static final int HASH = 256;
}
