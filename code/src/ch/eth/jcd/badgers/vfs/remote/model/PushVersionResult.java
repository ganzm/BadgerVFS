package ch.eth.jcd.badgers.vfs.remote.model;

import java.io.Serializable;

public class PushVersionResult implements Serializable {
	private static final long serialVersionUID = -7563206974448503076L;

	private boolean success;
	private long newServerVersion;
	private String message;

	public PushVersionResult(boolean success, String message) {
		this.success = success;
		this.message = message;
		this.newServerVersion = -1;
	}

	public PushVersionResult(boolean success, long newServerVersion) {
		this.success = success;
		this.message = null;
		this.newServerVersion = newServerVersion;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public long getNewServerVersion() {
		return newServerVersion;
	}

	public void setNewServerVersion(long newServerVersion) {
		this.newServerVersion = newServerVersion;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Success[" + success + "] NewServerVersion[" + newServerVersion + "] Message[" + message + "]";
	}
}
