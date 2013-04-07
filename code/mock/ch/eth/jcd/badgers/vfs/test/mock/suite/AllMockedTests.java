package ch.eth.jcd.badgers.vfs.test.mock.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.mock.MockedVFSDiskManagerImplTest;
import ch.eth.jcd.badgers.vfs.test.mock.MockedVFSEntryTest;
import ch.eth.jcd.badgers.vfs.test.mock.MockedVFSPathTest;

@RunWith(Suite.class)
@SuiteClasses({ MockedVFSDiskManagerImplTest.class, MockedVFSEntryTest.class, MockedVFSPathTest.class })
public class AllMockedTests {

}
