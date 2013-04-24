package ch.eth.jcd.badgers.vfs.sync.server;

/**
 * Represents a single connection to a client
 * 
 * 
 * Multiple ClientLink instances may share a UserAccount instance when a user is logged in on multiple machines
 * 
 */
public class ClientLink {

	private final UserAccount userAccount;

	public ClientLink(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}
}
