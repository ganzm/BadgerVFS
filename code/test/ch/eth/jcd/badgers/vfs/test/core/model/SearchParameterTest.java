package ch.eth.jcd.badgers.vfs.test.core.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class SearchParameterTest {

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Test
	public void testSearchParameterBase() {
		String str = "CamelCaseString";

		SearchParameter sp = new SearchParameter();
		sp.setSearchString(str);

		assertFalse(sp.matches("anyrandomstring"));
		assertTrue(sp.matches(str));
		assertTrue(sp.matches(str + "somesuffix"));
	}

	@Test
	public void testSearchParameterCaseSensitive() {
		String str = "CamelCaseString";

		SearchParameter sp = new SearchParameter();
		sp.setCaseSensitive(true);
		sp.setSearchString(str);

		assertTrue(sp.matches(str));
		assertFalse(sp.matches(str.toLowerCase()));

		sp.setCaseSensitive(false);
		assertTrue(sp.matches(str.toLowerCase()));
	}

	@Test
	public void testWildCards() {
		String str = "Camel*String";

		SearchParameter sp = new SearchParameter();
		sp.setCaseSensitive(true);
		sp.setSearchString(str);

		assertTrue(sp.matches("CamelString"));
		assertTrue(sp.matches("CamelBlubString"));
		assertTrue(sp.matches("CamelAString"));
		assertFalse(sp.matches("Camel"));

		sp.setSearchString("*Camel");
		assertTrue(sp.matches("GreenCamel"));
		assertTrue(sp.matches("Camel"));
		assertTrue(sp.matches("CamelTropyh"));
	}

	@Test
	public void testPlaceHolder() {
		String str = "?String";

		SearchParameter sp = new SearchParameter();
		sp.setCaseSensitive(false);
		sp.setSearchString(str);

		assertTrue(sp.matches("AString"));
		assertTrue(sp.matches("BString"));
		assertFalse(sp.matches("String"));

		sp.setSearchString("Str?ng");
		assertTrue(sp.matches("String"));
		assertTrue(sp.matches("strong"));
		assertFalse(sp.matches("strng"));
	}

	@Test
	public void testRegexMetaCharacters() {
		SearchParameter sp = new SearchParameter();
		sp.setSearchString("[\\^$.|+()");
		assertTrue(sp.matches("[\\^$.|+()"));

	}

	@Test
	public void testFileExtensions() {
		SearchParameter sp = new SearchParameter();
		sp.setSearchString("*.m");
		assertTrue(sp.matches("File.m"));

		sp.setSearchString("*m");
		assertTrue(sp.matches("File.m"));

	}
}
