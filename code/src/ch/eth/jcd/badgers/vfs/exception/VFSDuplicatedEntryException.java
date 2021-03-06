package ch.eth.jcd.badgers.vfs.exception;

public class VFSDuplicatedEntryException extends VFSException {

	private static final long serialVersionUID = 8249215540814299841L;

	public VFSDuplicatedEntryException(String cause) {
		super(cause);
	}

	public VFSDuplicatedEntryException(String cause, Exception exception) {
		super(cause, exception);
	}

	public VFSDuplicatedEntryException() {
		super("");
	}

	public static void throwIf(boolean b) throws VFSDuplicatedEntryException {
		if (b) {
			throw new VFSDuplicatedEntryException();
		}
	}
}
