package ch.eth.jcd.badgers.vfs.core.model;

public enum Compression {
	NONE("None"), LZ77("Lempel Ziff 77"), RLE("Run Lenght Encoding"), ;

	private String str;

	private Compression(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}

	public static Compression fromString(String compressionString) {
		for (Compression value : Compression.values()) {
			if (value.toString().equals(compressionString)) {
				return value;
			}
		}

		return null;
	}
}
