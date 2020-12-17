package com.samberro.utils;

import com.samberro.codec.Decoder;
import com.samberro.trie.SuffixTrie;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Random;

import static com.samberro.trie.SuffixTrie.MAX_DISTANCE;
import static com.samberro.trie.SuffixTrie.MAX_SUFFIX_LENGTH;
import static com.samberro.utils.InputDataGenerator.toByteString;

public class Utils {

    /**
     * Right shift unsigned byte. Java has no notion of unsigned and will widen as negative if top bit is 1
     * @param b byte to be shifted
     * @param shift number of right shits
     * @return result of unsigned shift
     */
    public static byte rightShiftUnsigned(byte b, int shift) {
        if (shift == 0) return b;
        if (shift >= 8) return 0;
        return (byte) ((b >> shift) & ~(((byte) 0x80) >> (shift - 1)));
    }

    /**
     * Return a formatted number of bytes. 1000 bytes => 1k, 1_000_000 => 1M
     * @param bytes number of bytes
     * @return human readable representation
     */
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

    /**
     * Decompresses and compares against the original array
     * @param original the original byte array used to generate the compressed version
     * @param compressed the compressed version of the original
     * @throws IOException
     */
    public static void decodeAndTest(byte[] original, byte[] compressed) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String compressedState = new Decoder(new BufferedOutputStream(os))
                .withDebug(true, original)
                .decode(compressed)
                .getStringRepresentation();

        os.close();
        byte[] uncompressed = os.toByteArray();
        String inputStr = toByteString(original);
        System.out.println("INPUT:        " + inputStr.substring(0, Math.min(100, inputStr.length())));
        System.out.println("COMPRESSED  : " + compressedState.substring(0, Math.min(100, compressedState.length())));
        String s = toByteString(uncompressed);
        System.out.println("UNCOMPRESSED: " + s.substring(0, Math.min(100, s.length())));
        if (!s.equals(inputStr)) System.err.println("Not equal");
    }

    /**
     * Stress test the trie after building by generating random string of bytes and attempting to find
     * This is tested against the string representation of input bytes
     * @param inputBytes the byte array used to build the trie
     * @param suffixTrie the trie to be stress tested
     */
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
                System.err.println("was not found: (" + start + ", " + length + ")");
            else if (!checkMatch(inputString, start, length, foundIndex))
                System.err.println("mismatch (" + start + ", " + length + ")");
        }
    }

    private static boolean checkMatch(String byteString, int start, int length, int foundIndex) {
        String substring = byteString.substring(start << 1, (start + length) << 1);
        int lastIndexOf = byteString.lastIndexOf(substring);
        if (lastIndexOf % 2 == 1)
            return checkMatch(byteString.substring(0, lastIndexOf + (length << 1) - 1), start, length, foundIndex);
        else return lastIndexOf == foundIndex << 1;
    }
}
