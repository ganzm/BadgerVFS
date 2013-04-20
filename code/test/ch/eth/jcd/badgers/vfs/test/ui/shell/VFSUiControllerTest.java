package ch.eth.jcd.badgers.vfs.test.ui.shell;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;
import ch.eth.jcd.badgers.vfs.ui.shell.VFSConsole;
import ch.eth.jcd.badgers.vfs.ui.shell.VFSUIController;

public class VFSUiControllerTest {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	private static final String BFS_FILE = TMP_DIR + File.separatorChar + "VFSUiControllerTest.bfs";
	private ByteArrayOutputStream outputStream;
	private String input;
	private String output;

	@BeforeClass
	public static void beforeClass() throws VFSException, IOException {
		UnittestLogger.init();
		UnitTestUtils.deleteFileIfExist(BFS_FILE);
		initDisk();
	}

	@Before
	public void before() {
		input = "";
		output = ">";
		openBfs();
	}

	@After
	public void after() throws IOException {
		outputStream.close();
	}

	@Test
	public void getChangeDirectoryCommandTest() throws IOException {
		noManagerTest("cd");
		noCorrectNumberOfParams("cd", 1);
		input += "cd .." + NEW_LINE;
		output += BFS_FILE + ">";
		createFile("getChangeDirectoryCommandTest.txt");
		input += "cd getChangeDirectoryCommanTest.txt" + NEW_LINE;
		output += "Child: getChangeDirectoryCommanTest.txt not found in current directory" + NEW_LINE + BFS_FILE + ">";
		input += "cd getChangeDirectoryCommandTest.txt" + NEW_LINE;
		output += "Cannot cd to file!" + NEW_LINE + BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getCloseCommandTest() throws IOException {
		noManagerTest("close");
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getCopyCommandTest() throws IOException {
		noManagerTest("cp");
		noCorrectNumberOfParams("cp", 2);
		input += "cp / /" + NEW_LINE;
		output += BFS_FILE + ">";
		createDir("getCopyCommandTest");
		input += "cp getCopyCommandTest getCopyCommandTest" + NEW_LINE;
		output += BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getCreateCommandTest() throws IOException {
		noCorrectNumberOfParams("create", 2);
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getDisposeCommandTest() throws IOException {
		noManagerTest("dispose");
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getDFCommandTest() throws IOException {
		noManagerTest("df");
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getExitCommandTest() throws IOException {
		input += "exit" + NEW_LINE;
		output += "There is still a disk in use, please use close command first" + NEW_LINE + BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getExportCommandTest() throws IOException {
		noManagerTest("export");
		noCorrectNumberOfParams("export", 2);
		input += "export getExportCommandTest.txt " + BFS_FILE + NEW_LINE;
		output += BFS_FILE + ">";
		input += "export getExportCommandTest.txt " + BFS_FILE + ".txt" + NEW_LINE;
		output += "Child: getExportCommandTest.txt not found in current directory, nothing exported" + NEW_LINE + BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getFindCommand() throws IOException {
		noManagerTest("find");
		noCorrectNumberOfParams("find", 1);
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getImportCommandTest() throws IOException {
		noManagerTest("import");
		noCorrectNumberOfParams("import", 2);
		input += "import " + BFS_FILE + ".txt getImportCommandTest.txt" + NEW_LINE;
		output += "Path on host file system does not exist/tmp/VFSUiControllerTest.bfs.txt" + NEW_LINE + BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getListCommandTest() throws IOException {
		noManagerTest("ls");
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getMakeDirCommandTest() throws IOException {
		noManagerTest("mkdir");
		noCorrectNumberOfParams("mkdir", 1);
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getMakeFileCommandTest() throws IOException {
		noManagerTest("mkfile");
		noCorrectNumberOfParams("mkfile", 1);
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getMoveCommandTest() throws IOException {
		noManagerTest("mv");
		noCorrectNumberOfParams("mv", 2);
		input += "mv / /" + NEW_LINE;
		output += BFS_FILE + ">";
		createDir("getMoveCommandTest");
		input += "mv getMoveCommandTest getMoveCommandTest" + NEW_LINE;
		output += BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getOpenCommandTest() throws IOException {
		noCorrectNumberOfParams("open", 1);
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getPWDCommandTest() throws IOException {
		noManagerTest("pwd");
		runTest();
		assertEquals(output, outputStream.toString());
	}

	@Test
	public void getRemoveCommandTest() throws IOException {
		noManagerTest("rm");
		noCorrectNumberOfParams("rm", 1);
		input += "rm getRemoveCommandTest.txt" + NEW_LINE;
		output += "Child: getRemoveCommandTest.txt not found in current directory, nothing removed" + NEW_LINE + BFS_FILE + ">";
		runTest();
		assertEquals(output, outputStream.toString());
	}

	private void createFile(String fileName) {
		input += "mkfile " + fileName + NEW_LINE;
		output += BFS_FILE + ">";
	}

	private void createDir(String dirName) {
		input += "mkdir " + dirName + NEW_LINE;
		output += BFS_FILE + ">";

	}

	private void noManagerTest(String command) {
		closeBfs();
		input += command + NEW_LINE;
		output += VFSUIController.NO_DISK_OPEN_ERROR + NEW_LINE + ">";
		openBfs();
	}

	private void noCorrectNumberOfParams(String command, int paramsNeeded) {
		input += command + NEW_LINE;
		output += String.format(VFSUIController.NO_CORRECT_NUMBER_OF_PARAMS, command, paramsNeeded, 0) + NEW_LINE;
		output += VFSConsole.HELP_MESSAGE + NEW_LINE;
		output += BFS_FILE + ">";
	}

	private void openBfs() {
		input += "open " + BFS_FILE + NEW_LINE;
		output += BFS_FILE + ">";
	}

	private void closeBfs() {
		input += "close" + NEW_LINE;
		output += ">";
	}

	private void runTest() throws IOException {
		outputStream = new ByteArrayOutputStream();
		closeBfs();
		try (BufferedReader reader = new BufferedReader(new CharArrayReader(input.toCharArray())); PrintWriter writer = new PrintWriter(outputStream);) {
			VFSConsole console = new VFSConsole(reader, writer);
			console.run();
			writer.flush();
		}

	}

	private static void initDisk() throws IOException {
		String inStr = "create " + BFS_FILE + " 100" + NEW_LINE + "close" + NEW_LINE;
		try (BufferedReader reader = new BufferedReader(new CharArrayReader(inStr.toCharArray()));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintWriter writer = new PrintWriter(out);) {
			VFSConsole console = new VFSConsole(reader, writer);
			console.run();
			writer.flush();
		}

	}

}
