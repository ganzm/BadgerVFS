package ch.eth.jcd.badgers.vfs.test.mock.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.suite.AllTests;

@RunWith(Suite.class)
@SuiteClasses({ AllTests.class, AllMockedTests.class })
public class AllAvailableTests {

}
