/**
 * A class that is used to store all required contents
 * of a file
 * @author sandy
 *
 */
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
	
	/**
	 * The hash function which will evaluate the hash
	 * value of a given identity of a file.
	 * @param name The identity of a file in integer.
	 * @return The hash code of the associating file name.
	 */
	public int hash(int name) {
		int hashCode = name % HASH;
		return hashCode;
	}
	
	/**
	 * Get the file name in string in the format of
	 * 4 characters.
	 * @return The string representation of the file name.
	 */
	public String getFileName() {
		return this.name;
	}
	
	/**
	 * Get the file's ID in the format of an integer.
	 * All 0's in front of the number will be discarded.
	 * @return The integer representation of the file.
	 */
	public int getFileId() {
		return this.fileName;
	}

	/**
	 * Get the hash value of the file.
	 * @return The hash value of the file in integer.
	 */
	public int getHashCode() {
		return this.hashCode;
	}

	/**
	 * Set the hash value of a file.
	 */
	public void setHashCode() {
		this.hashCode = this.hash(this.fileName);
	}

	private String name;
	private int fileName;
	private int hashCode;
	private static final int HASH = 256;
}
