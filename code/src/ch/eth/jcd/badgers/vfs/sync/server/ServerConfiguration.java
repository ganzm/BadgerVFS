package ch.eth.jcd.badgers.vfs.sync.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ServerConfiguration {

	private static final Logger LOGGER = Logger.getLogger(ServerConfiguration.class);
	private static final String DEFAULT_SERVER_FOLDER = System.getProperty("java.io.tmpdir") + File.separatorChar + "BadgersServerConfig";
	private List<UserAccount> userAccounts;
	private final File bfsFileFolder;
	private final File bfsServerConfigFile;

	@SuppressWarnings("unchecked")
	public ServerConfiguration(final String bfsFileFolderPath) throws VFSException {
		String bfsPath = bfsFileFolderPath;
		if (bfsFileFolderPath.isEmpty()) {
			bfsPath = DEFAULT_SERVER_FOLDER;
		}
		bfsFileFolder = new File(bfsPath);
		bfsServerConfigFile = new File(bfsPath + File.separatorChar + "ServerConfiguration.prs");
		if (!bfsServerConfigFile.exists()) {
			try {
				bfsServerConfigFile.getParentFile().mkdirs();
				bfsServerConfigFile.createNewFile();
				try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(bfsServerConfigFile));) {
					out.writeObject(new LinkedList<UserAccount>());
				} catch (final IOException e) {
					throw new VFSException("cannot persist ServerConfig: " + e.getMessage(), e);
				}
			} catch (final IOException e) {
				LOGGER.warn("Cannot create ServerConfiguration File", e);
			}
		}
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(bfsServerConfigFile));) {
			userAccounts = (List<UserAccount>) in.readObject();
		} catch (final IOException | ClassNotFoundException e) {
			throw new VFSException("cannot persist ServerConfig: " + e.getMessage(), e);
		}
	}

	public void persist() throws VFSException {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(bfsServerConfigFile));) {
			out.writeObject(userAccounts);
		} catch (final IOException e) {
			throw new VFSException("cannot persist ServerConfig: " + e.getMessage(), e);
		}
	}

	public void setUserAccount(final UserAccount userAccount) throws VFSException {
		if (accountExists(userAccount.getUsername())) {
			userAccounts.add(userAccount);
			persist();
			return;
		}
		throw new VFSException("Username already exists");
	}

	public UserAccount getUserAccount(final String username, final String password) throws VFSException {
		for (final UserAccount user : userAccounts) {
			if (username == user.getUsername()) {
				if (password.equals(user.getPassword())) {
					return user;
				}
				break;
			}
		}
		throw new VFSException("invalid username or password");
	}

	private boolean accountExists(final String username) {
		for (final UserAccount user : userAccounts) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	}

	public File getBfsFileFolder() {
		return bfsFileFolder;
	}
}
