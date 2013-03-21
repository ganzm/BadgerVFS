package ch.eth.jcd.badgers.vfs.exception;

public class VFSException extends Exception {

	private static final long serialVersionUID = -1735486613317907195L;

	public VFSException(String cause) {
		super(cause);
	}

	public VFSException(Exception exception) {
		super(exception);
	}

	public VFSException(String cause, Exception exception) {
		super(cause, exception);
	}

}
