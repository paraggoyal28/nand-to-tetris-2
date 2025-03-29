package vmtranslator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter mPrintWriter;
    private String mFileName;
    private int lCommands;
    private int returnNum;

    public CodeWriter(File file) {
        mPrintWriter = null;
        File outputFile = new File(file.getAbsolutePath().split(".vm")[0] + ".asm");
        if (file.isDirectory()) {
            outputFile = new File(file.getAbsolutePath() + "/" + file.getName() + ".asm");
        }
        try {
            mPrintWriter = new PrintWriter(new FileWriter(outputFile));
            mFileName = file.getName();
            lCommands = 0;
            returnNum = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public void writeInit() {
        mPrintWriter.println("@256\nD=A\n@SP\nM=D");
        writeCall("Sys.init", 0);
    }

    // Writes assembly code that affects the label command
    public void writeLabel(String label) {
        mPrintWriter.println("(" + label + ")");
    }

    // Writes assembly code that affects the goto command
    public void writeGoto(String label) {
        mPrintWriter.println("@" + label + "\n0;JMP");
    }

    // Write assembly code that affects the if-goto command
    public void writeIf(String label) {
        mPrintWriter.println("// write if goto");
        mPrintWriter.println("@SP\nAM=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "@" + label +
                "\nD;JNE");
    }

    // Writes assembly code that effects the function command
    public void writeFunction(String functionName, int numVars) {
        mPrintWriter.println("// write function " + functionName);
        writeLabel(functionName);
        for (int i = 0; i < numVars; ++i) {
            writePushPop(CommandType.C_PUSH, "constant", 0);
        }
    }

    // Writes assembly code that effects the call command
    public void writeCall(String functionName, int numVars) {
        mPrintWriter.println("// write call");
        writePop("return", returnNum);
        writePop("local", 0);
        writePop("argument", 0);
        writePop("this", 0);
        writePop("that", 0);
        // ARG = SP - 5 - nArgs
        mPrintWriter.println("@" + (numVars + 5) +
                "\nD=A\n" +
                "@SP\n" +
                "D=M-D\n" +
                "@ARG\n" +
                "M=D");

        // LCL = SP
        mPrintWriter.println("@SP\n" +
                "D=M\n" +
                "@LCL\n" +
                "M=D");
        writeGoto(functionName);
        writeLabel("RETURN" + returnNum++);
    }

    // Writes assembly code that effects the return command
    // SP LCL ARG THIS THAT
    // ROM 0 1 2 3 4
    public void writeReturn() {
        mPrintWriter.println("// write return");

        // FRAME = LCL
        // writePushPop() uses R13, so we use R14 here
        mPrintWriter.println("@LCL\n" +
                "D=M\n" +
                "@R14\n" +
                "M=D");

        // RET = *(FRAME - 5)
        // all return commands in a function return to the same return address, so don't
        // use returnNum here
        mPrintWriter.println("@5\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@R15\n" +
                "M=D");

        // *ARG = pop()
        writePushPop(CommandType.C_POP, "argument", 0);

        // SP = ARG + 1
        mPrintWriter.println("@ARG\n" +
                "D=M\n" +
                "@SP\n" +
                "M=D+1");

        // THAT = *(FRAME - 1)
        mPrintWriter.println("@R14\n" +
                "A=M-1\n" +
                "D=M\n" +
                "@THAT\n" +
                "M=D");

        // THIS = *(FRAME = 2)
        mPrintWriter.println("@R14\n" +
                "D=M\n" +
                "@2\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@THIS\n" +
                "M=D");

        // ARG = *(FRAME - 3)
        mPrintWriter.println("@R14\n" +
                "D=M\n" +
                "@3\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@ARG\n" +
                "M=D");

        // LCL = *(FRAME - 4)
        mPrintWriter.println("@R14\n" +
                "D=M\n" +
                "@4\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@LCL\n" +
                "M=D");

        // goto RET
        mPrintWriter.println("@R15\nA=M\n0;JMP");

    }

    /**
     * Writes to the output file the assembly code that implements the given
     * arithmetic command
     * true in VM: -1 0xFFFF 1111111111111111
     * false in VM: 0 0x0000 0000000000000000
     * 
     * @param command
     * @param segment
     * @param index
     */
    public void writeArithmetic(String command) {
        printArithmeticCommand(command);
        mPrintWriter.println("@SP\nAM=M-1");
        switch (command) {
            case "add":
                mPrintWriter.println("D=M\nA=A-1\nM=D+M");
                break;
            case "sub":
                mPrintWriter.println("D=M\nA=A-1\nM=M-D");
                break;
            case "neg":
                mPrintWriter.println("M=-M\n@SP\nAM=M+1");
                break;
            case "eq":
                mPrintWriter.println("D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "M=0\n" +
                        "@END_EQ" + lCommands +
                        "\nD;JNE\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(END_EQ" + lCommands + ")");
                lCommands++;
                break;
            case "gt":
                mPrintWriter.println("D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "M=0\n" +
                        "@END_GT" + lCommands +
                        "\nD;JLE\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(END_GT" + lCommands + ")");
                lCommands++;
                break;
            case "lt":
                mPrintWriter.println("D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "M=0\n" +
                        "@END_LT" + lCommands +
                        "\nD;JGE\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(END_LT" + lCommands + ")");
                lCommands++;
                break;
            case "and":
                mPrintWriter.println("D=M\nA=A-1\nM=M&D");
                break;
            case "or":
                mPrintWriter.println("D=M\nA=A-1\nM=M|D");
                break;
            case "not":
                mPrintWriter.println("M=!M\n@SP\nAM=M+1");
                break;
            default:
                break;
        }
    }

    /**
     * Writes to the output file the assembly code that implements the given
     * arithmetic command
     * where command is either C_PUSH or C_POP
     * 
     * @param segment
     * @param index
     */
    public void writePushPop(CommandType command, String segment, int index) {
        printPushPopCommand(command, segment, index);

        // push
        if (command == CommandType.C_PUSH) {
            switch (segment) {
                case "argument":
                    mPrintWriter.println("@ARG\n" +
                            "D=M\n" +
                            "@" + index + "\n" +
                            "A=D+A\n" +
                            "D=M");
                    break;
                case "local":
                    mPrintWriter.println("@" + index + "\n" +
                            "D=A\n" +
                            "@LCL\n" +
                            "A=D+M\n" +
                            "D=M");
                    break;
                case "static":
                    mPrintWriter.println("@" + mFileName +
                            "." + index + "\nD=M");
                    break;
                case "constant":
                    mPrintWriter.println("@" + index + "\nD=A");
                    break;
                case "this":
                    mPrintWriter.println("@THIS\n" +
                            "D=M\n" +
                            "@" + index + "\n" +
                            "A=D+A\n" +
                            "D=M");
                    break;
                case "that":
                    mPrintWriter.println("@THAT\n" +
                            "D=M\n" +
                            "@" + index + "\n" +
                            "A=D+A\n" +
                            "D=M");
                    break;
                case "pointer":
                    mPrintWriter.println("@" + (index + 3) + "\nD=M");
                    break;
                case "temp":
                    mPrintWriter.println("@" + (index + 5) + "\nD=M");
                    break;
                default:
                    break;
            }
            mPrintWriter.println("@SP\n" +
                    "AM=M+1\n" +
                    "A=A-1\n" +
                    "M=D");
        }
        // pop
        if (command == CommandType.C_POP) {
            switch (segment) {
                case "argument":
                    mPrintWriter.println("@ARG\n" +
                            "D=M\n" +
                            "@" + index + "\n" +
                            "D=D+A");
                    break;
                case "local":
                    mPrintWriter.println("@" + index + "\n" +
                            "D=A\n" +
                            "@LCL\n" +
                            "D=D+M");
                    break;
                case "static":
                    mPrintWriter.println("@" + mFileName + "." + index + "\nD=A");
                    break;
                case "constant":
                    mPrintWriter.println("@" + index + "\nD=A");
                    break;
                case "this":
                    mPrintWriter.println("@THIS\n" +
                            "D=M\n" +
                            "@" + index + "\n" +
                            "D=D+A");
                    break;
                case "that":
                    mPrintWriter.println("@THAT\n" +
                            "D=M\n" +
                            "@" + index + "\n" +
                            "D=D+A");
                    break;
                case "pointer":
                    mPrintWriter.println("@" + (index + 3) + "\nD=A");
                    break;
                case "temp":
                    mPrintWriter.println("@" + (index + 5) + "\nD=A");
                    break;
                default:
                    break;
            }
            mPrintWriter.println("@R13\n" +
                    "M=D\n" +
                    "@SP\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "@R13\n" +
                    "A=M\n" +
                    "M=D");
        }

    }

    // Closes the output file
    public void close() {
        mPrintWriter.close();
    }

    private void printArithmeticCommand(String command) {
        mPrintWriter.println("// " + command);
    }

    private void printPushPopCommand(CommandType command, String segment, int index) {
        if (command == CommandType.C_PUSH) {
            mPrintWriter.println("// push " + segment + " " + index);
        } else {
            mPrintWriter.println("// pop " + segment + " " + index);
        }
    }

    // pop commands for writeCall()
    private void writePop(String segment, int index) {
        switch (segment) {
            case "argument":
                mPrintWriter.println("@ARG\nD=M");
                break;

            case "local":
                mPrintWriter.println("@LCL\nD=M");
                break;

            case "this":
                mPrintWriter.println("@THIS\nD=M");
                break;

            case "that":
                mPrintWriter.println("@THAT\nD=M");
                break;

            case "return":
                mPrintWriter.println("@RETURN" + index + "\nD=A");
                break;
            default:
                break;
        }
        mPrintWriter.println("@SP\n" +
                "AM=M+1\n" +
                "A=A-1\n" +
                "M=D");
    }

}
