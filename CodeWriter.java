import java.io.*;

public class CodeWriter {

    // Writer used to output translated assembly code
    private BufferedWriter write1;

    // Name of the VM file being translated (without extension)
    private String fileName;

    // Counter for generating unique labels in comparison operations
    private int labelCounter;

    // Closes the output file
    public void close() throws IOException {
        write1.close();
    }

    // Constructor: initializes writer and output .asm file
    public CodeWriter(String filePath) throws IOException {
        File file = new File(filePath);
        this.fileName = file.getName().replace(".vm", "");
        this.write1 = new BufferedWriter(new FileWriter(this.fileName));
    }

    // Handles all push operations for all memory segments
    public void writePush(String segment, int var) throws IOException {
        String output;
        switch (segment) {

            // push constant: directly load constant into D
            case "constant": {
                output = "@" + var + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
                break;
            }

            // push for segments that store base addresses in registers
            case "local": case "argument": case "this": case "that": {
                String base = segmentTranslator(segment);
                output = String.format("@%s\nD=M\n@%d\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", base, var);
                break;
            }

            // push temp segment (fixed base address 5)
            case "temp": {
                output = String.format("@5\nD=A\n@%d\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", var);
                break;
            }

            // push pointer segment (THIS/THAT)
            case "pointer": {
                String ptr = (var == 0) ? "THIS" : "THAT";
                output = String.format("@%s\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", ptr);
                break;
            }

            // push static variable (filename.index)
            case "static": {
                output = String.format("@%s.%d\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", fileName, var);
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid argument: " + segment);
        }
        write1.write(output);
    }

    // Handles all pop operations for all memory segments
    public void writePop(String segment, int var) throws IOException {
        String output;
        switch (segment) {

            // pop to segments stored in base registers
            case "local": case "argument": case "this": case "that": {
                String base = segmentTranslator(segment);
                output = String.format("@%s\nD=M\n@%d\nD=D+A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n", base, var);
                break;
            }

            case "temp": {
                output = String.format("@5\nD=A\n@%d\nD=D+A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n", var);
                break;
            }

            // pop pointer segment (THIS/THAT)
            case "pointer": {
                String ptr = (var == 0) ? "THIS" : "THAT";
                output = String.format("@SP\nAM=M-1\nD=M\n@%s\nM=D\n", ptr);
                break;
            }

            case "static": {
                output = String.format("@SP\nAM=M-1\nD=M\n@%s.%d\nM=D\n", fileName, var);
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid argument: " + segment);
        }
        write1.write(output);
    }

    // Writes arithmetic and logical commands
    public void writeArithmetic(String command) throws IOException {
        String output;
        switch(command){

            case "add": {
                output = "@SP\nAM=M-1\nD=M\nA=A-1\nM=D+M\n";
                break;
            }

            case "sub": {
                output = "@SP\nAM=M-1\nD=M\nA=A-1\nM=M-D\n";
                break;
            }

            case "neg": {
                output = "@SP\nA=M-1\nM=-M\n";
                break;
            }

            // Equal comparison
            case "eq": {
                int id = labelCounter++;
                output = String.format(
                        "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nD=M-D\n@EQ_LABEL_TRUE%d\nD;JEQ\n" +
                                "D=0\n@EQ_LABEL_FALSE%d\n0;JMP\n" +
                                "(EQ_LABEL_TRUE%d)\nD=-1\n(EQ_LABEL_FALSE%d)\n" +
                                "@SP\nA=M\nM=D\n@SP\nM=M+1\n",
                        id, id, id, id
                );
                break;
            }

            // Greater than comparison
            case "gt": {
                int id = labelCounter++;
                output = String.format(
                        "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nD=M-D\n@GT_LABEL_TRUE%d\nD;JGT\n" +
                                "D=0\n@GT_LABEL_FALSE%d\n0;JMP\n" +
                                "(GT_LABEL_TRUE%d)\nD=-1\n(GT_LABEL_FALSE%d)\n@SP\nA=M\nM=D\n@SP\nM=M+1\n",
                        id, id, id, id
                );
                break;
            }

            // Less than comparison
            case "lt": {
                int id = labelCounter++;
                output = String.format(
                        "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nD=M-D\n@LT_LABEL_TRUE%d\nD;JLT\n" +
                                "D=0\n@LT_LABEL_FALSE%d\n0;JMP\n" +
                                "(LT_LABEL_TRUE%d)\nD=-1\n(LT_LABEL_FALSE%d)\n@SP\nA=M\nM=D\n@SP\nM=M+1\n",
                        id, id, id, id
                );
                break;
            }

            case "and": {
                output = "@SP\nAM=M-1\nD=M\nA=A-1\nM=D&M\n";
                break;
            }

            case "or": {
                output = "@SP\nAM=M-1\nD=M\nA=A-1\nM=D|M\n";
                break;
            }

            case "not": {
                output = "@SP\nA=M-1\nM=!M\n";
                break;
            }

            case "end":{
                output = "(END)\n@END\n0;JMP";
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid command " + command);
        }
        write1.write(output);
    }

    // Maps VM memory segment names to Hack assembly symbols for less duplicated code
    private String segmentTranslator(String segment) {
        return switch(segment) {
            case "local" -> "LCL";
            case "argument" -> "ARG";
            case "this" -> "THIS";
            case "that" -> "THAT";
            default -> throw new IllegalArgumentException("Invalid segment: " + segment);
        };
    }

    // Delegates push/pop commands based on their type
    public void writePushPop(Parser.commandType type, String segment, int index) throws IOException {
        if (type == Parser.commandType.C_PUSH) {
            writePush(segment, index);
        } else if (type == Parser.commandType.C_POP) {
            writePop(segment, index);
        } else {
            throw new IllegalArgumentException("Invalid command type for writePushPop");
        }
    }
}
