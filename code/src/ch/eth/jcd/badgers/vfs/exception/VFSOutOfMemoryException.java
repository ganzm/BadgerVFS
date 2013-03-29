package ch.eth.jcd.badgers.vfs.exception;

public class VFSOutOfMemoryException extends VFSRuntimeException {

	private static final long serialVersionUID = -7357101337678178427L;

	public VFSOutOfMemoryException(String cause, Exception exception) {
		super(cause, exception);
	}

	public VFSOutOfMemoryException(String cause) {
		super(cause);
	}

}
