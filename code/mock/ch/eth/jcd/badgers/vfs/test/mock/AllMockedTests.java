package ch.eth.jcd.badgers.vfs.test.mock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MockedVFSDiskManagerImplTest.class, MockedVFSEntryTest.class, MockedVFSPathTest.class })
public class AllMockedTests {

}
