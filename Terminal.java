package OperatingSystems.Assignment1;

import java.io.*;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CLI {

    private String currentDirectory;
    String username = System.getProperty("user.name");

    public static void main(String[] args) {
        CLI cli = new CLI();
        cli.start();
    }
    public CLI() {
        currentDirectory = System.getProperty("user.dir");
    }

    private void start() {
        currentDirectory = System.getProperty("user.dir");

        String hostname = System.getenv("COMPUTERNAME"); // For Windows
        if (hostname == null) {
            hostname = System.getenv("HOSTNAME"); // For Unix/Linux
        }

        System.out.println("Welcome to SimpleCLI! " + " \nType 'help' for available commands.");

        BufferedReader commandReader = new BufferedReader(new InputStreamReader(System.in));
        String command;

        while (true) {
            System.out.println(username + '@' + hostname + ' ' + currentDirectory);
            System.out.print("$ ");

            try {
                command = commandReader.readLine();

                if (command == null || command.trim().isEmpty()) {
                    continue;
                }

                switch (command.trim()) {
                    case "exit":
                        System.exit(0);
                        break;
                    case "help":
                        help();
                        break;
                    default:
                        executeCommand(command.trim());
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeCommand(String command) {
        // First check for pipe
        String[] pipeParts = parsePipe(command);
        if (pipeParts != null) {
            // Execute first command and capture its output
            String firstOutput = captureCommandOutput(pipeParts[0]);

            // Save the original input stream
            InputStream originalIn = System.in;

            // Set the new input stream with the output from first command
            System.setIn(new ByteArrayInputStream(firstOutput.getBytes()));

            // Execute second command
            executeCommand(pipeParts[1]);

            System.setIn(originalIn);
            return;
        }

        String[] redirectParts = parseRedirection(command);
        if (redirectParts[1] != null) {
            // Handle redirection
            String originalCommand = redirectParts[0];
            String operator = redirectParts[1];
            String outputFile = redirectParts[2].trim();

            // Redirect System.out to capture the output
            PrintStream originalOut = System.out;
            try {
                File file = new File(outputFile);
                if (!file.isAbsolute()) {
                    file = new File(currentDirectory, outputFile);
                }

                FileOutputStream fos = new FileOutputStream(file, operator.equals(">>"));
                PrintStream ps = new PrintStream(fos);
                System.setOut(ps);

                // Execute the original command
                executeCommand(originalCommand);

                ps.close();
                fos.close();
            } catch (IOException e) {
                System.err.println("Error redirecting output: " + e.getMessage());
            } finally {
                System.setOut(originalOut);
            }
            return;
        }

        String[] tokens = command.split(" ");
        String cmd = tokens[0];
        switch (cmd) {
            case "pwd":
                System.out.println(new File(currentDirectory));
                break;

            case "cd": {
                File newDir;
                if (tokens.length < 2) {
                    newDir = new File("C:/Users/" + username);
                } else {
                    // Reconstruct the path by joining all tokens after "cd"
                    String path = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));

                    if (path.equals("..")) {
                        newDir = new File(currentDirectory).getParentFile();
                    } else if (path.charAt(0) == '~') {
                        newDir = new File("C:/Users/" + username + '/' + path.substring(1));
                    } else {
                        newDir = new File(path);
                        if (!newDir.isAbsolute()) {
                            newDir = new File(currentDirectory, path);
                        }
                    }
                }

                if (newDir != null && newDir.exists() && newDir.isDirectory()) {
                    currentDirectory = newDir.getAbsolutePath();
                    System.setProperty("user.dir", currentDirectory);
                    System.out.println("Changed directory to: " + currentDirectory);
                } else {
                    System.err.println("Directory does not exist: " + (tokens.length > 1 ? String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length)) : newDir.getPath()));
                }
                break;
            }

            case "ls": {
                File listedDirectory;
                boolean a = false, r = false;

                if (tokens.length == 1) {
                    listedDirectory = new File(currentDirectory);
                    File[] files = listedDirectory.listFiles();

                    for (File file : files) {
                        System.out.println(file.getName());
                    }
                    break;
                }

                String path;

                if (tokens[1].charAt(0) == '-') {
                    if (tokens.length == 2) {
                        path = currentDirectory;
                    } else {
                        path = tokens[2];
                    }

                    for (char option : tokens[1].toCharArray()) {
                        switch (option) {
                            case 'a':
                                a = true;
                                break;
                            case 'r':
                                r = true;
                                break;
                        }
                    }
                } else {
                    path = tokens[1];
                }

                if (path.equals("..")) {
                    listedDirectory = new File(currentDirectory).getParentFile();
                } else if (path.charAt(0) == '~') {
                    listedDirectory = new File("C:/Users/" + username + '/' + path.substring(1));
                } else {
                    listedDirectory = new File(path);
                    if (!listedDirectory.isAbsolute()) {
                        listedDirectory = new File(currentDirectory, path);
                    }
                }

                if (listedDirectory.isDirectory()) {
                    File[] files = listedDirectory.listFiles();

                    if (r) {
                        for (int i = files.length - 1; i >= 0; i--) {
                            if (a && files[i].isHidden()) {
                                continue;
                            }
                            System.out.println(files[i].getName());
                        }
                    } else {
                        for (File file : files) {
                            if (!a && file.isHidden()) {
                                continue;
                            }
                            System.out.println(file.getName());
                        }
                    }
                } else {
                    System.err.println(path + " is not a Directory");
                }
                break;
            }

            case "mkdir": {
                if (tokens.length < 2) {
                    System.err.println("No directory name specified");
                    break;
                }

                for (int i = 1; i < tokens.length; i++) {
                    String dirName = tokens[i];
                    File newDir = new File(dirName);

                    if (!newDir.isAbsolute()) {
                        newDir = new File(currentDirectory, dirName);
                    }

                    if (!newDir.exists()) {
                        if (newDir.mkdirs()) {
                            System.out.println("Created directory: " + newDir.getAbsolutePath());
                        } else {
                            System.err.println("Failed to create directory: " + dirName);
                        }
                    } else {
                        System.err.println("Directory already exists: " + dirName);
                    }
                }
                break;
            }

            case "rmdir": {
                if (tokens.length < 2) {
                    System.err.println("No directory name specified.");
                    break;
                }

                String dirName = tokens[1];
                File dir = new File(dirName);

                if (!dir.isAbsolute()) {
                    dir = new File(currentDirectory, dirName);
                }

                if (!dir.exists() || !dir.isDirectory()) {
                    System.err.println("Directory does not exist: " + dirName);
                } else if (dir.listFiles().length != 0) {
                    System.err.println("Directory is not empty: " + dirName);
                } else if (dir.delete()) {
                    System.out.println("Directory removed successfully: " + dir.getAbsolutePath());
                } else {
                    System.err.println("Failed to remove directory: " + dirName);
                }
                break;
            }

            case "touch": {
                if (tokens.length < 2) {
                    System.err.println("No file name specified");
                    break;
                }

                for (int i = 1; i < tokens.length; i++) {
                    String fileName = tokens[i];
                    File newFile = new File(fileName);

                    if (!newFile.isAbsolute()) {
                        newFile = new File(currentDirectory, fileName);
                    }

                    try {
                        if (newFile.exists()) {
                            newFile.setLastModified(System.currentTimeMillis());      //update timestamp if files exists
                            System.out.println("Updated timestamp: " + fileName);
                        } else {
                            if (newFile.createNewFile()) {
                                System.out.println("Created file: " + newFile.getAbsolutePath());   //create file
                            } else {
                                System.err.println("Failed to create file: " + fileName);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error creating file " + fileName + ": " + e.getMessage());
                    }
                }
                break;
            }

            case "mv": {
                if (tokens.length < 3) {
                    System.err.println("mv: missing file operand");
                    break;
                }

                // get the destination (last argument)
                String destPath = String.join(" ", Arrays.copyOfRange(tokens, tokens.length - 1, tokens.length));
                File destination = new File(destPath);
                if (!destination.isAbsolute()) {
                    destination = new File(currentDirectory, destPath);
                }

                // get source files
                List<File> sourceFiles = new ArrayList<>();
                for (int i = 1; i < tokens.length - 1; i++) {
                    String sourcePath = String.join(" ", Arrays.copyOfRange(tokens, i, i + 1));
                    File source = new File(sourcePath);
                    if (!source.isAbsolute()) {
                        source = new File(currentDirectory, sourcePath);
                    }
                    sourceFiles.add(source);
                }

                // check if many files
                if (sourceFiles.size() > 1 && !destination.isDirectory()) {
                    System.err.println("Directory does not exist: "+destPath);
                    break;
                }

                // process each file
                for (File source : sourceFiles) {
                    if (!source.exists()) {
                        System.err.println("Directory does not exist: "+ source.getName());
                        continue;
                    }

                    try {
                        File targetFile;
                        if (destination.isDirectory()) {
                            // If destination is a directory, move into it
                            targetFile = new File(destination, source.getName());
                        } else {
                            // If destination is not a directory (and we have only one source file)
                            targetFile = destination;
                        }

                        if (source.renameTo(targetFile)) {
                            System.out.println("Moved '" + source.getName() + "' to '" + targetFile.getAbsolutePath() + "'");
                        } else {
                            System.err.println("Destination file already exists: "+targetFile.getAbsolutePath() );
                        }
                    } catch (SecurityException e) {
                        System.err.println("mv: permission denied");
                    }
                }
                break;
            }

            case "rm": {
                if (tokens.length < 2) {
                    System.err.println("No file name specified.");
                    break;
                }

                String fileName = tokens[1];
                File file = new File(fileName);

                if (!file.isAbsolute()) {
                    file = new File(currentDirectory, fileName);
                }

                if (!file.exists()) {
                    System.err.println("File does not exist: " + fileName);
                } else if (file.delete()) {
                    System.out.println("File deleted successfully: " + file.getAbsolutePath());
                } else {
                    System.err.println("Failed to delete file: " + fileName);
                }
                break;
            }

            case "cat": {
                boolean hasRedirection = false;
                String outputFile = null;
                for (int i = 1; i < tokens.length; i++) {
                    if (tokens[i].equals(">")) {            //checks for >
                        hasRedirection = true;
                        if (i + 1 < tokens.length) {
                            outputFile = tokens[i + 1];
                        }
                        break;
                    }
                }

                if (hasRedirection && outputFile != null) {    //handles redirection
                    File file = new File(outputFile);
                    if (!file.isAbsolute()) {
                        file = new File(currentDirectory, outputFile);
                    }

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        System.out.println("Enter text (press Enter twice to finish):");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        String line;
                        while ((line = reader.readLine()) != null && !line.isEmpty()) {
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (IOException e) {
                        System.err.println("Error writing to file: " + e.getMessage());
                    }
                    break;
                }

                if (tokens.length == 1) {       //no arg case
                    try {
                        System.out.println("Enter text (press Enter twice to finish):");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        String line;
                        while ((line = reader.readLine()) != null && !line.isEmpty()) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading input: " + e.getMessage());
                    }
                    break;
                }

                for (int i = 1; i < tokens.length; i++) {       //normal case or with multiple files
                    String fileName = tokens[i];
                    File file = new File(fileName);
                    if (!file.isAbsolute()) {
                        file = new File(currentDirectory, fileName);
                    }
                    if (!file.exists()) {
                        System.err.println("File does not exist: " + fileName);
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + e.getMessage());
                    }
                }
                break;
            }

            default:
                System.out.println("Unknown Command: " + command.trim());
                break;
        }
    }

    public void help() {
        System.out.println("Available commands:");
        System.out.println("  pwd       - Print working directory");
        System.out.println("  cd <dir>  - Change directory");
        System.out.println("  ls        - List directory contents");
        System.out.println("  ls -a     - List all files, including hidden ones");
        System.out.println("  ls -r     - List files in reverse order");
        System.out.println("  mkdir <dir> - Create new directory");
        System.out.println("  rmdir <dir> - Remove directory (if empty)");
        System.out.println("  touch <file> - Create a new file");
        System.out.println("  mv <src> <dest> - Move/rename file or directory");
        System.out.println("  rm <file> - Delete file");
        System.out.println("  cat <file> - Display file contents");
        System.out.println("  exit      - Exit the CLI");
        System.out.println("  |         - Pipe command");
        System.out.println("  > <file>  - Redirect output to file");
        System.out.println("  >> <file> - Append output to file");
    }

    private String[] parseRedirection(String command) {
        String[] parts = new String[3]; 
        if (command.contains(" >> ")) {
            parts = command.split(" >> ", 2);
            parts = new String[]{parts[0], ">>", parts[1]};
        } else if (command.contains(" > ")) {
            parts = command.split(" > ", 2);
            parts = new String[]{parts[0], ">", parts[1]};
        }
        return parts;
    }

    private String[] parsePipe(String command) {
        if (command.contains(" | ")) {
            return command.split(" \\| ", 2);
        }
        return null;
    }

    private String captureCommandOutput(String command) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        // Execute command
        executeCommand(command);

        // Restore original out
        System.setOut(originalOut);

        return baos.toString();
    }
}
