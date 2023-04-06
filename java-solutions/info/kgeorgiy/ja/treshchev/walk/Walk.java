package info.kgeorgiy.ja.treshchev.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {

    private static final int HASH_LENGTH = 64;
    public static final String ERROR_HASH = "0".repeat(HASH_LENGTH);
    public static final int BUFFER_LENGTH = 1 << 12;

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Expected input and output file name!");
            return;
        }

        String input = args[0];
        final Path outputPath;
        try {
            outputPath = Path.of(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Wrong path of output file!");
            return;
        }
        try {
            Path outputParentDirectories = outputPath.getParent();
            if (outputParentDirectories != null) {
                Files.createDirectories(outputParentDirectories);
            }
        } catch (IOException e) {
            System.err.println("Couldn't create output file" + outputPath);
        }

        try (BufferedReader inputFileReader = Files.newBufferedReader(Path.of(input));
             BufferedWriter outputFileWriter = Files.newBufferedWriter(outputPath)) {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            String pathFile;
            while ((pathFile = inputFileReader.readLine()) != null) {
                String hash = getHashOfFile(pathFile, messageDigest);

                outputFileWriter.write(hash + " " + pathFile);
                outputFileWriter.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not read input or output file!");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Couldn't support SHA-256 algorithm!");
        } catch (InvalidPathException e) {
            System.err.println("Invalid output file name! Found " + outputPath);
        }
    }

    private static String getHashOfFile(String file, MessageDigest messageDigest) {
        try (InputStream fileByteReader = Files.newInputStream(Path.of(file))) {
            byte[] buffer = new byte[BUFFER_LENGTH];
            for (int i = 0; i < BUFFER_LENGTH; i++) {
                buffer[i] = 0;
            }
            int countOfBytes;
            while ((countOfBytes = fileByteReader.read(buffer)) >= 0) {
                messageDigest.update(buffer, 0, countOfBytes);
            }
            byte[] hash = messageDigest.digest();
            messageDigest.reset();
            StringBuilder hashToString = new StringBuilder();
            for (byte b : hash) {
                String hexString = String.format("%02x", b);
                hashToString.append(hexString);
            }
            return hashToString.toString();
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't found the file \"" + file + "\"!");
        } catch (InvalidPathException e) {
            System.err.println("Wrong file path! Found \"" + file + "\"!");
        } catch (IOException e) {
            System.err.println("Couldn't read the file \"" + file + "\"!");
        }
        return ERROR_HASH;
    }
}
