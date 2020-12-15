package com.samberro.utils;

import com.samberro.SuffixTrie;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Random;

import static com.samberro.SuffixTrie.MAX_DISTANCE;
import static com.samberro.SuffixTrie.MAX_SUFFIX_LENGTH;

public class Utils {

    public static byte rightShiftUnsigned(byte b, int shift) {
        if (shift == 0) return b;
        if (shift >= 8) return 0;
        return (byte) ((b >> shift) & ~(((byte) 0x80) >> (shift - 1)));
    }

    public static byte[] generateRandomInput(int size) {
        Random rand = new Random(System.currentTimeMillis());
        byte[] bytes = new byte[size];
        rand.nextBytes(bytes);
        return bytes;
    }

    public static String toByteString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    public static byte[] fromByteString(String s) {
        if (s.length() % 2 != 0) throw new RuntimeException("String is not even. Length = " + s.length());
        byte[] out = new byte[s.length() >> 1];
        for (int i = 0; i < s.length() - 1; i += 2) {
            out[i >> 1] = (byte) Integer.parseInt(s, i, i + 2, 16);
        }
        return out;
    }

    public static byte[] fromFile(int size) throws IOException {
        FileInputStream is = new FileInputStream("/Users/i850563/Desktop/sample.txt");
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] bytes = bis.readNBytes(size);
        bis.close();
        return bytes;
    }

    public static void testTrie(byte[] inputBytes, SuffixTrie suffixTrie) {
        String inputString = toByteString(inputBytes); // Helps with testing
        byte[] testBytes;
        int i = 0;
        Random random = new Random(System.currentTimeMillis());
        int maxLength = inputBytes.length;
        while (i++ < 1000) {
            int start = maxLength - random.nextInt(MAX_DISTANCE - 1) - 1;
            int length = Math.min(random.nextInt(63) + 3, Math.min(maxLength - start, MAX_SUFFIX_LENGTH));
            testBytes = Arrays.copyOfRange(inputBytes, start, start + length);
            int foundIndex = suffixTrie.find(testBytes);
            if (foundIndex == -1)
                throw new RuntimeException("was not found: (" + start + ", " + length + ")");
            if (!checkMatch(inputString, start, length, foundIndex))
                throw new RuntimeException("mismatch (" + start + ", " + length + ")");
        }
    }

    private static boolean checkMatch(String byteString, int start, int length, int foundIndex) {
        String substring = byteString.substring(start << 1, (start + length) << 1);
        int lastIndexOf = byteString.lastIndexOf(substring);
        if (lastIndexOf % 2 == 1)
            return checkMatch(byteString.substring(0, lastIndexOf + (length << 1) - 1), start, length, foundIndex);
        else return lastIndexOf == foundIndex << 1;
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMG");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    private static void log(String format, Object... opts) {
        System.out.printf(format, opts).println();
    }

}
