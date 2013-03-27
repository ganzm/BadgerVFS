package ch.eth.jcd.badgers.vfs.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.encryption.EncryptionTest;

@RunWith(Suite.class)
@SuiteClasses({ EncryptionTest.class })
public class EncryptionTests {

}
