package ch.eth.jcd.badgers.vfs.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.core.HashTest;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.BadgersVFSDiskManagerTest;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.BadgersVFSEntryTest;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.BadgersVFSPathTest;

@RunWith(Suite.class)
@SuiteClasses({ BadgersVFSDiskManagerTest.class, BadgersVFSEntryTest.class, BadgersVFSPathTest.class, HashTest.class })
public class CoreTests {

}
