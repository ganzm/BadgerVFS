package ch.eth.jcd.badgers.vfs.exception;

public class VFSInvalidPathException extends VFSRuntimeException {

	private static final long serialVersionUID = 5036406436697127986L;

	public VFSInvalidPathException(Exception exception) {
		super(exception);
	}

	public VFSInvalidPathException(String message) {
		super(message);
	}

}
