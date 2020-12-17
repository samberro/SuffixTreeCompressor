package com.samberro.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class InputDataGenerator {

    /**
     * Generate random array of bytes
     *
     * @param size array size
     * @return random array of bytes
     */
    public static byte[] generateRandomInput(int size) {
        Random rand = new Random(System.currentTimeMillis());
        byte[] bytes = new byte[size];
        rand.nextBytes(bytes);
        return bytes;
    }

    /**
     * Returns a byte-string. Helpful for testing:
     * Example input byte array [0xAB, 0xCD, 0xEF] returns "ABCDEF"
     *
     * @param bytes array of bytes
     * @return human readable string representation os byte array
     */
    public static String toByteString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    /**
     * Returns a byte array from its string representation. Helpful for testing
     * Example if string is "ABCDEF", returned array is [0xAB, 0xCD, 0xEF]
     *
     * @param s input string representing the byte stream
     * @return the converted bytes as an array
     */
    public static byte[] fromByteString(String s) {
        if (s.length() % 2 != 0) throw new RuntimeException("String is not even. Length = " + s.length());
        byte[] out = new byte[s.length() >> 1];
        for (int i = 0; i < s.length() - 1; i += 2) {
            out[i >> 1] = (byte) Integer.parseInt(s, i, i + 2, 16);
        }
        return out;
    }

    /**
     * Reads size number of bytes from file at path
     *
     * @param path file path to be read
     * @param size number of bytes to return
     * @return byte array
     * @throws IOException
     */
    public static byte[] fromFile(String path, int size) {
        byte[] bytes = new byte[size];
        int read = 0;
        BufferedInputStream bis = null;
        try {
            while (read < size) {
                bis = new BufferedInputStream(new FileInputStream(path));
                read += bis.readNBytes(bytes, read, size - read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(bis);
        }
        return bytes;
    }

    private static void closeSilently(BufferedInputStream bis) {
        try {
            if(bis != null) bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
