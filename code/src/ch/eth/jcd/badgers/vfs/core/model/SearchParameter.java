package ch.eth.jcd.badgers.vfs.core.model;

public class SearchParameter {

	private String searchString = "";

	private boolean caseSensitive = true;

	private boolean includeSubFolders = true;

	public SearchParameter() {
	}

	/**
	 * TODO implement me: implement case sensitivity switch, metrics like editing distance, wild cards,
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean matches(String fileName) {
		return fileName.toLowerCase().contains(searchString.toLowerCase());
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isIncludeSubFolders() {
		return includeSubFolders;
	}

	public void setIncludeSubFolders(boolean includeSubFolders) {
		this.includeSubFolders = includeSubFolders;
	}

}
