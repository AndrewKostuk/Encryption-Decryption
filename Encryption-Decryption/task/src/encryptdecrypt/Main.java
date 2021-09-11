package encryptdecrypt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


abstract class Encryptor {

    protected String message;

    protected int key;

    public Encryptor(String message, int key) {
        this.message = message;
        this.key = key;
    }

    public String work(String mode) {
        if ("enc".equals(mode)) {
            return encode();
        } else {
            return decode();
        }
    }

    abstract protected String encode();

    abstract protected String decode();
}

class ShiftEncryptor extends Encryptor {

    public ShiftEncryptor(String message, int key) {
        super(message, key);
    }

    @Override
    protected String encode() {
        char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] >= 'a') {
                if (letters[i] + key <= 'z') {
                    letters[i] += key;
                } else if (letters[i] <= 'z') {
                    letters[i] -= 'z' - 'a' + 1 - key;
                }
            }
            if (letters[i] >= 'A') {
                if (letters[i] + key <= 'Z') {
                    letters[i] += key;
                } else if (letters[i] <= 'Z') {
                    letters[i] -= 'Z' - 'A' + 1 - key;
                }
            }
        }
        return new String(letters);
    }

    @Override
    protected String decode() {
        char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] <= 'z') {
                if (letters[i] - key >= 'a') {
                    letters[i] -= key;
                } else if (letters[i] >= 'a') {
                    letters[i] += 'z' - 'a' + 1 - key;
                }
            }
            if (letters[i] >= 'A') {
                if (letters[i] <= 'Z' - key) {
                    letters[i] -= key;
                } else if (letters[i] <= 'Z') {
                    letters[i] += 'Z' - 'A' + 1 - key;
                }
            }
        }
        return new String(letters);
    }
}

class UnicodeEncryptor extends Encryptor {

    public UnicodeEncryptor(String message, int key) {
        super(message, key);
    }

    @Override
    protected String encode() {
        char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            letters[i] += key;
        }
        return new String(letters);
    }

    @Override
    protected String decode() {
        char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            letters[i] -= key;
        }
        return new String(letters);
    }
}

class EncryptorStaticFactory {

    public static Encryptor getEncryptor(String type, String message, int key) {
        if ("unicode".equals(type)) {
            return new UnicodeEncryptor(message, key);
        } else {
            return new ShiftEncryptor(message, key);
        }
    }
}

class FileUtility {
    public static String readFromFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            return "Error " + e.getMessage();
        }
    }

    public static void writeToFile(String path, String message) {
        File file = new File(path);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.print(message);
        } catch (FileNotFoundException e) {
            System.out.println("Error " + e.getMessage());
        }
    }
}


public class Main {
    public static void main(String[] args) {
        int modeParameterIndex = Arrays.asList(args).indexOf("-mode");
        String mode = modeParameterIndex != -1 && modeParameterIndex != args.length - 1
                ? args[modeParameterIndex + 1]
                : "enc";
        int keyParameterIndex = Arrays.asList(args).indexOf("-key");
        int key = keyParameterIndex != -1 && keyParameterIndex != args.length - 1
                ? Integer.parseInt(args[keyParameterIndex + 1])
                : 0;
        int dataParameterIndex = Arrays.asList(args).indexOf("-data");
        String data = dataParameterIndex != -1 && dataParameterIndex != args.length - 1
                ? args[dataParameterIndex + 1]
                : "";
        int inParameterIndex = Arrays.asList(args).indexOf("-in");
        String in = inParameterIndex != -1 && inParameterIndex != args.length - 1
                ? args[inParameterIndex + 1]
                : "";
        int outParameterIndex = Arrays.asList(args).indexOf("-out");
        String out = outParameterIndex != -1 && outParameterIndex != args.length - 1
                ? args[outParameterIndex + 1]
                : "";
        int algParameterIndex = Arrays.asList(args).indexOf("-alg");
        String alg = algParameterIndex != -1 && algParameterIndex != args.length - 1
                ? args[algParameterIndex + 1]
                : "shift";

        String message = !"".equals(in) && "".equals(data)
                ? FileUtility.readFromFile(in)
                : data;
        Encryptor encryptor = EncryptorStaticFactory.getEncryptor(alg, message, key);
        String outputMessage = encryptor.work(mode);
        if (!"".equals(out)) {
            FileUtility.writeToFile(out, outputMessage);
        } else {
            System.out.println(outputMessage);
        }
    }
}