package ch.eth.jcd.badgers.vfs.core.model;

import java.util.regex.Pattern;

public class SearchParameter {

	private String searchString = "";

	private boolean caseSensitive = true;

	private boolean includeSubFolders = true;
	private Pattern searchPattern;

	public SearchParameter() {
		createSearchExpression();
	}

	private void createSearchExpression() {
		String str = searchString;

		if (!caseSensitive) {
			str = str.toLowerCase();
		}

		// quotes the search string into \Q....\E
		// so we don't need to escape all these regex special chars which are
		// '[', '\\', '^', '$', '.', '|', '?', '*', '+', '(', ')'
		str = Pattern.quote(str);
		str = str.replace("?", "\\E\\w\\Q");
		str = str.replace("*", "\\E\\w*\\Q");
		str = str + "\\w*";

		searchPattern = Pattern.compile(str);
	}

	/**
	 * @param fileName
	 * @return
	 */
	public boolean matches(final String fileName) {
		final String tmp = caseSensitive ? fileName : fileName.toLowerCase();
		return searchPattern.matcher(tmp).matches();
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(final String searchString) {
		this.searchString = searchString;
		createSearchExpression();
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(final boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		createSearchExpression();
	}

	public boolean isIncludeSubFolders() {
		return includeSubFolders;
	}

	public void setIncludeSubFolders(final boolean includeSubFolders) {
		this.includeSubFolders = includeSubFolders;
	}

}
