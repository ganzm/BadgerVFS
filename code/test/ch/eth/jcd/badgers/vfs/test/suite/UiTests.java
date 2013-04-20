package ch.eth.jcd.badgers.vfs.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.eth.jcd.badgers.vfs.test.ui.shell.VFSConsoleTest;
import ch.eth.jcd.badgers.vfs.test.ui.shell.VFSUiControllerTest;

@RunWith(Suite.class)
@SuiteClasses({ VFSConsoleTest.class, VFSUiControllerTest.class })
public class UiTests {

}
