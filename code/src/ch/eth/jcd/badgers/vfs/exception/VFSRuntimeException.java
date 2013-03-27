package ch.eth.jcd.badgers.vfs.exception;

public class VFSRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7686384456055244311L;

	public VFSRuntimeException(String cause) {
		super(cause);
	}

	public VFSRuntimeException(Exception exception) {
		super(exception);
	}

	public VFSRuntimeException(String cause, Exception exception) {
		super(cause, exception);
	}

}
