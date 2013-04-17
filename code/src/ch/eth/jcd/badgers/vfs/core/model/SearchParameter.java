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
		str = str.replace("?", "\\w");
		str = str.replace("*", "\\w*");
		str = str + "\\w*";

		searchPattern = Pattern.compile(str);
	}

	/**
	 * @param fileName
	 * @return
	 */
	public boolean matches(String fileName) {
		String tmp;
		if (!caseSensitive) {
			tmp = fileName.toLowerCase();
		} else {
			tmp = fileName;
		}

		return searchPattern.matcher(tmp).matches();
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
		createSearchExpression();
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		createSearchExpression();
	}

	public boolean isIncludeSubFolders() {
		return includeSubFolders;
	}

	public void setIncludeSubFolders(boolean includeSubFolders) {
		this.includeSubFolders = includeSubFolders;
	}

}
