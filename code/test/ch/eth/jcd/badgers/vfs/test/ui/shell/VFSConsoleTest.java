package ch.eth.jcd.badgers.vfs.test.ui.shell;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.junit.Test;

import ch.eth.jcd.badgers.vfs.ui.shell.VFSConsole;

public class VFSConsoleTest {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private final String tmpdir = System.getProperty("java.io.tmpdir");
	private final String bfsFile = tmpdir + File.separatorChar + "consoleTest.bfs";

	@Test
	public void testConsoleOperation() throws FileNotFoundException, IOException {
		String tmpFileContent = "this is the content ASD ÖÄÜöäü";
		String tmpFileName = "VFSConsoleTestTmpFile.txt";
		String outFileName = "VFSConsoleTestOutFile.txt";
		File tmpFile = new File(tmpdir + File.separatorChar + tmpFileName);
		File outFile = new File(tmpdir + File.separatorChar + outFileName);
		try (OutputStream out = new FileOutputStream(tmpFile);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(tmpFileContent);
		}
		String testInput = "create " + bfsFile + " 100" + NEW_LINE + "mkdir test" + NEW_LINE + "" + NEW_LINE + "df" + NEW_LINE + "ls" + NEW_LINE + "find tes"
				+ NEW_LINE + "mkfile test.txt" + NEW_LINE + "import " + tmpFile.getAbsolutePath() + " " + tmpFileName + NEW_LINE + "export " + tmpFileName
				+ " " + outFile.getAbsolutePath() + NEW_LINE + "pwd" + NEW_LINE + "rm test.txt" + NEW_LINE + "cp " + tmpFileName + " test2.txt" + NEW_LINE
				+ "mv " + tmpFileName + " test/testli.txt" + NEW_LINE + "cd test" + NEW_LINE + "close" + NEW_LINE + "open " + bfsFile + NEW_LINE + "dispose"
				+ NEW_LINE + "blub" + NEW_LINE + "exit" + NEW_LINE;

		String testOutput = ">" + bfsFile + ">" + bfsFile + ">" + bfsFile + ">VirtualFileSystem	Size 	Used 	Avail 	Use% 	Mounted on\n"
				+ "/			73.2M	2K	73.2M	0%	" + bfsFile + "\n" + bfsFile + ">" + "test\n" + bfsFile + ">/test\n" + bfsFile + ">" + bfsFile + ">" + bfsFile + ">"
				+ bfsFile + ">/\n" + bfsFile + ">" + bfsFile + ">" + bfsFile + ">" + bfsFile + ">" + bfsFile + ">" + ">" + bfsFile
				+ ">>blub is not a valid input\n" + VFSConsole.HELP_MESSAGE + "\n>";
		// prepare streams
		BufferedReader reader = new BufferedReader(new CharArrayReader(testInput.toCharArray()));
		ByteArrayOutputStream outStream;
		outStream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(outStream);

		VFSConsole console = new VFSConsole(reader, writer);
		console.run();

		writer.flush();
		assertEquals(testOutput, outStream.toString());
		if (outFile.exists()) {
			outFile.delete();
		}
	}

}
