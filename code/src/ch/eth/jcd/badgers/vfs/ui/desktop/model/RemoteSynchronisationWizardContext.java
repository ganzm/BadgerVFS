package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;

public class RemoteSynchronisationWizardContext {
	public static enum LoginActionEnum {
		SYNC, LOGINREMOTE, CONNECT;
	}

	private LoginActionEnum loginActionEnum;

	/**
	 * @Deprecated to be refactored - Memo MG: do not carry around RemoteManager Threads. RemoteManagers created by ServerUrlDialog may not be disposed
	 *             correctly
	 */
	private RemoteManager remoteManager;

	private LinkedDisk selectedDiskToLink;

	private String localFilePath;

	private String remoteHostName;
	private String username;
	private String password;
	private VFSDiskManager diskManager;

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

	public LinkedDisk getSelectedDiskToLink() {
		return selectedDiskToLink;
	}

	public void setSelectedDiskToLink(final LinkedDisk selectedDiskToLink) {
		this.selectedDiskToLink = selectedDiskToLink;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public VFSDiskManager getDiskManager() {
		return diskManager;
	}

	public void setDiskManager(VFSDiskManager diskManager) {
		this.diskManager = diskManager;
	}
}
