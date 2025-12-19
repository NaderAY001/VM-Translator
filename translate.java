import java.io.IOException;

public class translate {
    public static void main(String[] args) throws IOException {

        System.out.println("Your code has be translated");
        
        // Ensure the user provided both an input VM file and an output ASM file.
        // If not, print usage instructions and exit.
        if (args.length < 2) {
            System.err.println("Usage: java Main <input.vm> <output.asm>");
            return;
        }

        // First argument: path to the VM file to translate
        String inputPath = args[0];

        // Second argument: path where the generated .asm output should be written
        String outputPath = args[1];

        // Create a Parser to read VM commands from the input file
        Parser p = new Parser(inputPath);

        // Create a CodeWriter to generate Hack assembly into the output file
        CodeWriter writer = new CodeWriter(outputPath);

        // Process the file line-by-line until there are no more VM commands
        while (p.hasMoreCommands()) {

            // Move parser to the next valid (non-empty, non-comment) command
            p.advance();

            // If the command is an arithmetic operation, handle it separately
            if (p.getType() == Parser.commandType.C_ARITHMETIC) {
                writer.writeArithmetic(p.arg1());
            }
            // Otherwise, it must be a push or pop command
            else {
                writer.writePushPop(p.getType(), p.arg1(), p.arg2());
            }
        }

        // Close the output file once all commands have been translated
        writer.close();
    }
}