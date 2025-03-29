package vmtranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class VMTranslator {
    private static CodeWriter codeWriter;

    public static void main(String args[]) {
        if (args.length > 0) {
            File folder = new File(args[0]);

            if (!folder.exists())
                throw new IllegalArgumentException("No such file/directory");

            String fileName = folder.getName();

            if (folder.isFile()) {
                if (fileName.length() <= 3 || !fileName.substring(fileName.length() - 3).equals(".vm")) {
                    throw new IllegalArgumentException(".vm file is required!");
                }

                codeWriter = new CodeWriter(folder);

                translate(folder);

            } else if (folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files.length == 0) {
                    throw new IllegalArgumentException("Empty directory");
                }

                codeWriter = new CodeWriter(folder);

                codeWriter.writeInit();

                iterateFiles(files);
            }
            codeWriter.close();
        }
    }

    private static void iterateFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                iterateFiles(file.listFiles());
            } else {
                if (file.getName().endsWith(".vm")) {
                    translate(file);
                }
            }
        }
    }

    private static void translate(File file) {
        File outputFile = new File(file.getName().split(".vm")[0] + ".asm");
        Scanner inputScanner = null;
        try {
            inputScanner = new Scanner(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        Parser parser = new Parser(inputScanner);

        codeWriter.setFileName(outputFile.getName());

        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.commandType()) {
                case C_PUSH:
                case C_POP:
                    codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                    break;
                case C_ARITHMETIC:
                    codeWriter.writeArithmetic(parser.arg1());
                    break;
                case C_LABEL:
                    codeWriter.writeLabel(parser.arg1());
                    break;
                case C_IF:
                    codeWriter.writeIf(parser.arg1());
                    break;
                case C_GOTO:
                    codeWriter.writeGoto(parser.arg1());
                    break;
                case C_FUNCTION:
                    codeWriter.writeFunction(parser.arg1(), parser.arg2());
                    break;
                case C_CALL:
                    codeWriter.writeCall(parser.arg1(), parser.arg2());
                    break;
                case C_RETURN:
                    codeWriter.writeReturn();
                    break;
                default:
                    break;
            }
        }
    }

}