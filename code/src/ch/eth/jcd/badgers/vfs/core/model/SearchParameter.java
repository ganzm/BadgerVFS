package ch.eth.jcd.badgers.vfs.core.model;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

public class SearchParameter {

	private String searchString = "";

	private boolean caseSensitive = true;

	private boolean includeSubFolders = true;

	private boolean regexSearch = false;

	private Pattern searchPattern;

	public SearchParameter() {
		createSearchExpression();
	}

	private void createSearchExpression() {
		try {

			if (isRegexSearch()) {
				searchPattern = Pattern.compile(searchString);
				return;
			}

			String str = searchString;

			if (!caseSensitive) {
				str = str.toLowerCase();
			}

			// quotes the search string into \Q....\E
			// so we don't need to escape all these regex special chars which are
			// '[', '\\', '^', '$', '.', '|', '?', '*', '+', '(', ')'
			str = Pattern.quote(str);
			str = str.replace("?", "\\E[^\\s]\\Q");
			str = str.replace("*", "\\E[^\\s]*\\Q");
			str = str + "[^\\s]*";

			// str = "\\w*m\\w*";
			searchPattern = Pattern.compile(str);
		} catch (PatternSyntaxException ex) {
			throw new VFSRuntimeException("Invalid RegexPattern " + searchString, ex);
		}
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

	public void setRegexSearch(final boolean regexSearch) {
		this.regexSearch = regexSearch;
		createSearchExpression();
	}

	public boolean isRegexSearch() {
		return regexSearch;
	}

	public boolean isIncludeSubFolders() {
		return includeSubFolders;
	}

	public void setIncludeSubFolders(final boolean includeSubFolders) {
		this.includeSubFolders = includeSubFolders;
	}

}
