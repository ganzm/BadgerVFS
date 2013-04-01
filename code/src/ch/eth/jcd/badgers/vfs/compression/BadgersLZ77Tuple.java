package ch.eth.jcd.badgers.vfs.compression;

class BadgersLZ77Tuple {
	int matchLoc;
	int matchLength;
	String charFollowed;
	static final int lookForwardWindow = 7;
	static final int windowLength = 31;

	/**
	 * 
	 * @param matchLoc
	 *            points to the start of a match relative to current loc. Has value 0 if no match
	 * @param matchLength
	 *            specifies the length of the match 0 if there is no match
	 * @param charFollowed
	 *            first character that does not match.
	 */
	BadgersLZ77Tuple(int matchLoc, int matchLength, String charFollowed) {
		this.matchLoc = matchLoc;
		this.matchLength = matchLength;
		this.charFollowed = charFollowed;
	}

	BadgersLZ77Tuple(int match, int charFollowed) {
		this.matchLoc = match >> 3;
		this.matchLength = match & 7;
		this.charFollowed = Character.toString((char) charFollowed);
	}

	// Overridden toString method
	@Override
	public String toString() {
		byte[] ar = toByte();

		return matchLoc + "," + matchLength + "," + charFollowed + "( " + Integer.toBinaryString((char) ar[0]) + ", " + Integer.toBinaryString((char) ar[1])
				+ ")";
	}

	public byte[] toByte() {
		int concat = (matchLoc << 3) | matchLength;
		byte[] result = new byte[] { (byte) concat, (byte) charFollowed.charAt(0) };
		return result;
	}
}
