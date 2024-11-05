# Java CLI Simulator

A Java implementation of a Command Line Interface (CLI) that simulates common Unix/Linux commands. This project provides a simple shell environment with support for basic file operations, pipes, and output redirection, with unit testing for each command using JUnit.

## Features

### Basic Commands
- `pwd` - Print working directory
- `cd <dir>` - Change directory
  - `cd` - Change to home directory
  - `cd ~` - Change to home directory
  - `cd ..` - Move up one directory
  - `cd <path>` - Change to specified directory

### File Operations
- `ls` - List directory contents
  - `ls -a` - List all files (including hidden)
  - `ls -r` - List files in reverse order
- `mkdir <dir>` - Create new directory
- `rmdir <dir>` - Remove empty directory
- `touch <file>` - Create a new file or update timestamp
- `mv <src> <dest>` - Move/rename file or directory
- `rm <file>` - Delete file
- `cat <file>` - Display file contents
  - `cat` - Read from standard input
  - `cat <file1> <file2>` - Concatenate and display multiple files

### Special Features
- Pipe operator (`|`) - Chain commands together
- Output redirection
  - `> <file>` - Redirect output to file (overwrite)
  - `>> <file>` - Append output to file

## Testing
The project includes comprehensive JUnit tests for all implemented commands and features, ensuring reliability and correct behavior.


### Prerequisites
- Java JDK 11 or higher
- JUnit 5 for running tests