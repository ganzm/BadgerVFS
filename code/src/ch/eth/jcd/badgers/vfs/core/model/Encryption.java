package ch.eth.jcd.badgers.vfs.core.model;

public enum Encryption {
	NONE("None"), CAESAR("Caesar");

	private String str;

	private Encryption(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}

	public static Encryption fromString(String encryptionString) {
		for (Encryption value : Encryption.values()) {
			if (value.toString().equals(encryptionString)) {
				return value;
			}
		}

		return null;
	}
}
