package ch.eth.jcd.badgers.vfs.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CoreTests.class, EncryptionTests.class, CompressionTests.class, UiTests.class })
public class AllTests {

}
