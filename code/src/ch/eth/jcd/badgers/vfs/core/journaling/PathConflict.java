package ch.eth.jcd.badgers.vfs.core.journaling;

/**
 * Simply contains two pathes
 * 
 * indicates which path had to be replaced by another
 * 
 */
public class PathConflict {
	private final String path;
	private final String conflictResolvedPath;

	public PathConflict(String path, String conflictResolvedPath) {
		this.path = path;
		this.conflictResolvedPath = conflictResolvedPath;
	}

	public String getPath() {
		return path;
	}

	public String getConflictResolvedPath() {
		return conflictResolvedPath;
	}

	public String resolve(String absolutePath) {
		return absolutePath.replace(path, conflictResolvedPath);
	}
}
