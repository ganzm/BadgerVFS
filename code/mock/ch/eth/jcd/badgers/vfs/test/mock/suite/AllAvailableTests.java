package ch.eth.jcd.badgers.vfs.test.mock.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.suite.CompressionTests;
import ch.eth.jcd.badgers.vfs.test.suite.CoreTests;
import ch.eth.jcd.badgers.vfs.test.suite.EncryptionTests;

@RunWith(Suite.class)
@SuiteClasses({ CoreTests.class, EncryptionTests.class, CompressionTests.class, AllMockedTests.class })
public class AllAvailableTests {

}
