package ch.eth.jcd.badgers.vfs.ui.desktop.model;

public class RemoteSynchronisationWizardContext {
	public static enum LoginActionEnum {
		SYNC, LOGIN;
	}

	private LoginActionEnum loginActionEnum;
	private String remoteHostName;
	private String username;
	private String password;

	public RemoteSynchronisationWizardContext(LoginActionEnum loginActionEnum) {
		this.loginActionEnum = loginActionEnum;
	}

	public String getRemoteHostName() {
		return remoteHostName;
	}

	public void setRemoteHostName(String remoteHostName) {
		this.remoteHostName = remoteHostName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LoginActionEnum getLoginActionEnum() {
		return loginActionEnum;
	}

	public void setLoginActionEnum(LoginActionEnum loginActionEnum) {
		this.loginActionEnum = loginActionEnum;
	}
}
