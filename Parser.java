import java.io.*;
import java.util.Scanner;

public class Parser {

    // Scanner used to read the VM input file line by line
    private Scanner scanner;

    // Holds the current command split into parts (tokens)
    private String[] parts;

    // Constructor: opens the file and prepares the scanner
    public Parser(String fileName) throws FileNotFoundException {
        this.scanner = new Scanner(new File(fileName));
    }

    // Returns true if there are more lines in the input file
    // Note: some lines may be blank or comments, but advance() handles those
    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    // Reads the next command from the file, skipping empty lines and comments.
    // After this method, 'parts' will contain the tokens of the next VM command.
    public void advance() {

        while (scanner.hasNextLine()) {

            parts = null; // Reset the current instruction

            // Read next raw line and trim whitespace
            String line = scanner.nextLine().trim();

            // Remove inline comments if present
            int comment = line.indexOf("//");
            if (comment != -1) {
                line = line.substring(0, comment);
            }

            // Trim again after removing comment
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            // Split the valid VM command into tokens
            parts = line.split("\\s+");
            return;
        }
    }

    // The three command types
    public enum commandType {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
    }

    // Determines the type of the current command based on its first token
    public commandType getType() {
        switch (parts[0]) {
            case "push": return commandType.C_PUSH;
            case "pop": return commandType.C_POP;
            // All other commands are arithmetic commands
            default: return commandType.C_ARITHMETIC;
        }
    }

    // Returns the first argument of the current command.
    // For arithmetic commands, this is the command itself (e.g., "add", "sub", "eq").
    // For push/pop, this returns the segment name.
    public String arg1() {
        if (getType() == commandType.C_ARITHMETIC) {
            return parts[0]; // e.g., "add"
        }
        return parts[1]; // e.g., "local" in "push local 0"
    }

    // Returns the second argument (only valid for push/pop commands).
    public int arg2() {
        return Integer.parseInt(parts[2]);
    }

}
