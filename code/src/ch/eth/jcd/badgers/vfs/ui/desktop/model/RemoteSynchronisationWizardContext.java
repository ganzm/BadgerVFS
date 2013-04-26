package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;

public class RemoteSynchronisationWizardContext {
	public static enum LoginActionEnum {
		SYNC, LOGIN;
	}

	private LoginActionEnum loginActionEnum;
	private RemoteManager remoteManager;

	private String remoteHostName;
	private String username;
	private String password;

	public RemoteSynchronisationWizardContext(final LoginActionEnum loginActionEnum) {
		this.loginActionEnum = loginActionEnum;
	}

	public String getRemoteHostName() {
		return remoteHostName;
	}

	public RemoteManager getRemoteManager() {
		return remoteManager;
	}

	public void setRemoteManager(final RemoteManager remoteManager) {
		this.remoteManager = remoteManager;
	}

	public void setRemoteHostName(final String remoteHostName) {
		this.remoteHostName = remoteHostName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public LoginActionEnum getLoginActionEnum() {
		return loginActionEnum;
	}

	public void setLoginActionEnum(final LoginActionEnum loginActionEnum) {
		this.loginActionEnum = loginActionEnum;
	}
}
