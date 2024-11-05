package OperatingSystems.Assignment1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CLITest {
    private CLI cli;
    private String originalDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    @BeforeEach
    void setUp() {
        cli = new CLI();
        originalDir = System.getProperty("user.dir");
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void testpwd() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("pwd");
        System.out.println(outContent.toString());
        String expectedOutput = originalDir + System.lineSeparator() +originalDir +System.lineSeparator() +System.lineSeparator();

        assertEquals(expectedOutput, outContent.toString());
    }
    @Test
    void testChangeDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        cli.executeCommand("cd " + tempDir.toString());

        String expectedDir = tempDir.toAbsolutePath().toString();
        assertEquals(expectedDir, System.getProperty("user.dir"));
        Files.delete(tempDir);
    }

    @Test
    void testInvalidChangeDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        cli.executeCommand("cd " + tempDir.toString() + "null");

        String expectedDir = tempDir.toAbsolutePath().toString();
        assertNotEquals(expectedDir, System.getProperty("user.dir"));
        Files.delete(tempDir);
    }
    @Test
    void testChangeDirectoryToParent() throws IOException {
        Path parentDir = new File(System.getProperty("user.dir")).getParentFile().toPath();
        cli.executeCommand("cd " + "..");

        String expectedDir = parentDir.toAbsolutePath().toString();
        assertEquals(expectedDir, System.getProperty("user.dir"));

    }

    @Test
    void testChangeDirectoryToTilda() throws IOException {
        String userHome = System.getProperty("user.home");
        cli.executeCommand("cd ~");
        
        assertEquals(userHome, System.getProperty("user.dir"));
    }

    @Test
    void testChangeDirectoryToHome() throws IOException {
        String userHome = System.getProperty("user.home");
        cli.executeCommand("cd");

        assertEquals(userHome, System.getProperty("user.dir"));
    }

    @Test
    void testListFiles() throws IOException {
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        Path testFile = currentDir.resolve("testFile.txt");
        Files.createFile(testFile);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("ls");
        assertTrue(outContent.toString().contains("testFile.txt"));

        Files.delete(testFile);
    }

    @Test
    void testListFileswithHidden() throws IOException {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path testFile = currentDir.resolve(".testFile.txt");
        
        Files.deleteIfExists(testFile);
        
        try {
            Files.createFile(testFile);
            
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            
            cli.executeCommand("ls -a");
            
            assertTrue(outContent.toString().contains(".testFile.txt"));
            
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    void testListFileswithReverse() throws IOException {
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        Path tempDir = Files.createTempDirectory("cli_test_dir");
        Files.createFile(Paths.get(tempDir.toString(), "file1.txt"));
        Files.createFile(Paths.get(tempDir.toString(), "file2.txt"));
        Files.createFile(Paths.get(tempDir.toString(), "file3.txt"));

        cli.executeCommand("cd " + tempDir.toString());


        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("ls");
        String outputNormal = outContent.toString().trim();
        outContent.reset();

        cli.executeCommand("ls -r");
        String outputReverse = outContent.toString().trim();

        String[] normalFiles = outputNormal.split(System.lineSeparator());
        String[] reverseFiles = outputReverse.split(System.lineSeparator());

        normalFiles = Arrays.stream(normalFiles).filter(f -> !f.isEmpty()).toArray(String[]::new);
        reverseFiles = Arrays.stream(reverseFiles).filter(f -> !f.isEmpty()).toArray(String[]::new);

        for (int i = 0; i < normalFiles.length; i++) {
            assertEquals(normalFiles[normalFiles.length - 1 - i], reverseFiles[i]);
        }

    }


    @Test
    void testMakeDirectory() throws IOException {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path newDir = currentDir.resolve("testSubDir");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("mkdir testSubDir");

        assertTrue(Files.exists(currentDir.resolve("testSubDir")));

        Files.deleteIfExists(newDir);
    }

    @Test
    void testRemoveDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("rmdirTest");
        Files.createDirectory(tempDir.resolve("emptyDir"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("cd " + tempDir.toString());
        cli.executeCommand("rmdir emptyDir");

        assertFalse(Files.exists(tempDir.resolve("emptyDir")));

        Files.delete(tempDir);
    }

    @Test
    void testTouchFile() throws IOException {
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("touch testFile.txt");

        assertTrue(Files.exists(currentDir.resolve("testFile.txt")));

        Files.delete(currentDir.resolve("testFile.txt"));
    }

    @Test
    void testDeleteFile() throws IOException {

        Path currentDir = Paths.get(System.getProperty("user.dir"));

        Files.createFile(currentDir.resolve("testFile.txt"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("rm testFile.txt");

        assertFalse(Files.exists(currentDir.resolve("testFile.txt")));
    }

    @Test
    void testHelp() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.help();

        String actualOutput = outContent.toString().replace("\r\n", "\n");
        String expectedOutput = "Available commands:\n" +
                "  pwd       - Print working directory\n" +
                "  cd <dir>  - Change directory\n" +
                "  ls        - List directory contents\n" +
                "  ls -a     - List all files, including hidden ones\n" +
                "  ls -r     - List files in reverse order\n" +
                "  mkdir <dir> - Create new directory\n" +
                "  rmdir <dir> - Remove directory (if empty)\n" +
                "  touch <file> - Create a new file\n" +
                "  mv <src> <dest> - Move/rename file or directory\n" +
                "  rm <file> - Delete file\n" +
                "  cat <file> - Display file contents\n" +
                "  exit      - Exit the CLI\n" +
                "  |         - Pipe command\n" +
                "  > <file>  - Redirect output to file\n" +
                "  >> <file> - Append output to file\n";

        assertEquals(expectedOutput, actualOutput);
    }


    private String captureCommandOutput(String command) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        cli.executeCommand(command);

        System.setOut(originalOut);

        return baos.toString();
    }
    @Test
    void testMoveFileSuccessfully() throws IOException {
        String currentDirectory = captureCommandOutput("pwd").trim();

        Path sourceFile = Files.createTempFile("testFile", ".txt");
        Path destFile = Path.of(currentDirectory, "movedFile.txt");

        cli.executeCommand("mv " + sourceFile.toAbsolutePath() + " " + destFile.toAbsolutePath());

        assertFalse(Files.exists(sourceFile));
        assertTrue(Files.exists(destFile));

        Files.delete(destFile);
    }

    @Test
    void testMoveFileSourceDoesNotExist() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err; // Save the original System.err
        System.setErr(new PrintStream(outContent));

        try {
            cli.executeCommand("mv nonexistent.txt newFile.txt");
            
            String actualOutput = outContent.toString().replace("\r\n", "\n");
            String expectedOutput = "Directory does not exist: nonexistent.txt\n";
            
            assertEquals(expectedOutput, actualOutput);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testMoveFileDestinationExists() throws IOException {
        String currentDirectory = captureCommandOutput("pwd").trim();

        Path sourceFile = Files.createTempFile("testFile", ".txt");
        Path destFile = Path.of(currentDirectory, "existingFile.txt");
        
        try {
            Files.deleteIfExists(destFile);
            Files.createFile(destFile);

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(outContent));

            cli.executeCommand("mv " + sourceFile.toAbsolutePath() + " " + destFile.toAbsolutePath());

            String expectedError = "Destination file already exists: " + destFile.toAbsolutePath() + System.lineSeparator();
            assertEquals(expectedError, outContent.toString());

        } finally {
            // Clean up both files
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(destFile);
        }
    }

    @Test
    void testRedirectOutputToFile() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        cli.executeCommand("ls > testOutput.txt");
        cli.executeCommand("ls");

        String content = Files.readString(Path.of("testOutput.txt")).trim();

        assertEquals(outContent.toString() , content + System.lineSeparator());
        Files.deleteIfExists(Path.of("testOutput.txt"));
    }

    @Test
    void testAppendOutputToFile() throws IOException {
        Files.deleteIfExists(Path.of("testOutput.txt"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        cli.executeCommand("ls -r >> testOutput.txt");
        cli.executeCommand("ls >> testOutput.txt");
        cli.executeCommand("ls -r");
        cli.executeCommand("ls");

        String content = Files.readString(Path.of("testOutput.txt")).trim();

        assertEquals(outContent.toString() , content + System.lineSeparator());
        Files.deleteIfExists(Path.of("testOutput.txt"));
    }

    @Test
    void testCat() {
        File testFile = null;
        try {
            testFile = new File(System.getProperty("user.dir"), "test.txt");
            String testContent = "Hello World\nTest Line 2";
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
                writer.write(testContent);
            }

            outputStreamCaptor.reset();

            cli.executeCommand("cat test.txt");

            String actualOutput = outputStreamCaptor.toString().replace("\r\n", "\n");
            String expectedOutput = testContent + "\n";


            assertEquals(expectedOutput, actualOutput);

        } catch (IOException e) {
            fail("Test failed due to IO exception: " + e.getMessage());
        } finally {
            // Cleanup: delete the test file if it exists
            if (testFile != null && testFile.exists()) {
                testFile.delete();
            }
        }
    }

}