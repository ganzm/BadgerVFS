package ch.eth.jcd.badgers.vfs.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.compression.BadgersRLECompressionTest;

@RunWith(Suite.class)
@SuiteClasses({ BadgersRLECompressionTest.class })
public class CompressionTests {

}
