package com.samberro.utils;

import java.util.Arrays;
import java.util.Iterator;

public class Options {
    private String inputFilename, outputFilename;
    private String inputByteString;
    private InputOutputType inputType;
    private InputOutputType outputType;
    private boolean debugMode;
    private boolean compress = true;

    public static Options parseOptions(String[] args) {
        Options options = new Options();
        Iterator<String> it = Arrays.stream(args).iterator();
        while (it.hasNext()) {
            Option option = Option.parse(it.next());
            if(option == null) printHelp();
            switch (option) {
                case FileInputOption:
                    options.inputType = InputOutputType.File;
                    options.inputFilename = it.next();
                    break;
                case FileOutputOption:
                    options.outputType = InputOutputType.File;
                    options.outputFilename = it.next();
                    break;
                case ByteStringInputOption:
                    options.inputType = InputOutputType.ByteString;
                    options.outputType = InputOutputType.ByteString; //also output to console as byte string
                    options.inputByteString = it.next();
                    break;
                case ByteStringOutputOption:
                    options.outputType = InputOutputType.ByteString;
                    break;
                case DebugModeOption:
                    options.debugMode = true;
                    break;
                case DecompressModeOption:
                    options.compress = false;
                    break;
                case HelpModeOption:
                    printHelp();
                    break;
            }
        }
        return options;
    }

    public String getInputFilename() {
        return inputFilename;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public String getInputByteString() {
        return inputByteString;
    }

    public InputOutputType getInputType() {
        return inputType;
    }

    public InputOutputType getOutputType() {
        return outputType;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean compress() {
        return compress;
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar compressor.jar [-u] [-is|-if <byte-string|filename>] " +
                "[-os|-of <byte-string|filename>] [-d]");
        System.out.println("Ex: java -jar compressor.jar -if ./sample.txt -of ./sample.cmp");
        System.out.println("Ex: java -jar compressor.jar -u -if ./sample.cmp -of ./sample_uncompressed.txt");
        System.out.println("Ex: java -jar compressor.jar -is AABBCCDDAABBCCDD -d");
        Option.printHelp();
        System.exit(0);
    }

    public enum InputOutputType {ByteString, File}

    enum Option {
        HelpModeOption("-h", "--help", "This help"),
        FileInputOption("-if", "--input-file", "Use a binary file as input"),
        FileOutputOption("-of", "--output-file", "Save output to a file"),
        ByteStringInputOption("-is", "--input-string", "Use a byte string as input" +
                "Ex: \"AABBCC\" for [0xAA, 0xBB, 0xCC]"),
        ByteStringOutputOption("-os", "--output-string", "Output as a byte string to console"),
        DebugModeOption("-d", "--debug", "print extra info, enable integrity checks and debug mode (slower)"),
        DecompressModeOption("-u", "--uncompress", "Uncompress input");


        private String shrt;
        private String lng;
        private String desc;

        Option(String shrt, String lng, String desc) {
            this.shrt = shrt;
            this.lng = lng;
            this.desc = desc;
        }

        static Option parse(String option) {
            if (option.startsWith("--")) return fromLong(option);
            else if (option.startsWith("-")) return fromShort(option);
            return null;
        }

        static private Option fromLong(String option) {
            for (Option o : values()) {
                if (o.lng.equals(option)) return o;
            }
            return null;
        }

        static private Option fromShort(String option) {
            for (Option o : values()) {
                if (o.shrt.equals(option)) return o;
            }
            return null;
        }

        static void printHelp() {
            for (Option o : values()) {
                System.out.printf("\t%s,%s: %s\n", o.shrt, o.lng, o.desc);
            }
        }
    }
}
