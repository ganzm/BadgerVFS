package ch.eth.jcd.badgers.vfs.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.core.interfaces.VFSDiskManagerTest;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.VFSEntryTest;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.VFSPathTest;

@RunWith(Suite.class)
@SuiteClasses({ VFSDiskManagerTest.class, VFSEntryTest.class, VFSPathTest.class })
public class AllTests {

}
