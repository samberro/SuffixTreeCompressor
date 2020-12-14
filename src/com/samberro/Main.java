package com.samberro;

import com.samberro.utils.ByteCollector;
import com.samberro.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import static com.samberro.utils.Utils.fromString;

public class Main {
    public static List<Byte> INPUT;

    public static StringBuilder OUTPUT = new StringBuilder();
    public static Stack<Byte> COMPRESSED = new Stack<>();

    private static final ByteCollector byteCollector = new ByteCollector();

    public static int index;

    public static void main(String[] args) throws IOException {
//        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
//            System.out.println("COUNT: " + Node.COUNT + ", bytes: " + (index + 1));
//            Thread.currentThread().getThreadGroup().uncaughtException(t, e);
//        });
//        String inputString = generateInput(100_000);
        String inputString = Utils.fromFile(500_000);
        INPUT = fromString(inputString);
        System.out.println("INPUT: " + stringifyByteList(INPUT));
        byte[] bytes = INPUT.stream().collect(byteCollector);

        long startTime = System.currentTimeMillis();
        Compressor compressor = new Compressor();
        for (int i = 0; i < bytes.length; i++) {
            index = i;
//            if(i >0 && i%70_000==0) compressor.cull(i);
            byte b = bytes[i];
            compressor.insertByte(b, i);
        }
        System.out.printf("Finished building in %d ms\n", System.currentTimeMillis() - startTime);

        testTrie(inputString, compressor);

        decode();

        System.out.println("COUNT: " + Node.COUNT + ", bytes: " + (index + 1));
    }

    private static void testTrie(String inputString, Compressor compressor) {
        byte[] bytes;
        int i = 0;
        Random random = new Random(System.currentTimeMillis());
        while (i++ < 1000) {
            int start = INPUT.size() - random.nextInt(0xFFFF);
            int length = Math.min(random.nextInt(63) + 3, INPUT.size() - start);
            bytes = INPUT.subList(start, start + length).stream().collect(byteCollector);
            int foundIndex = compressor.find(bytes);
            if (foundIndex == -1)
                throw new RuntimeException("was not found: (" + start + ", " + length + ")");
            if (!checkMatch(inputString, start, length, foundIndex))
                throw new RuntimeException("mismatch (" + start + ", " + length + ")");
        }
    }

    private static boolean checkMatch(String inputString, int start, int length, int foundIndex) {
        String substring = inputString.substring(start * 2, (start + length) * 2);
        int lastIndexOf = inputString.lastIndexOf(substring);
        if (lastIndexOf % 2 == 1)
            return checkMatch(inputString.substring(0, lastIndexOf + length * 2 - 1), start, length, foundIndex);
        else return lastIndexOf == foundIndex * 2;
    }


    static byte lastBoundary = 0;
//    private static int encodeBytes(byte[] bytes, int posInStream, Match m) {
//        int matchIndex = m == null ? -1 : m.getMatchIndex();
//        int encoded = 0;
//        byte[] a;
//        if(matchIndex == 0) {
//            OUTPUT.append("(1,")
//                    .append("-").append(m.getRelativePos())
//                    .append(",").append(m.getLength()).append(")");
//
//            int val = 1 << 22; // shift 22 bits to fit other fields
//            val |= (m.getRelativePos() & 0xFFFF) << 6; // shift 6 bits to fit size
//            val |= m.getLength() & 0x3F;
//
//            int shiftBy = (1 + ((8 - lastBoundary) % 8)) % 8;
//            val <<= shiftBy; //boundary align
//
//            byte[] byteVals = new byte[] {
//                    (byte) (val >> 24),
//                    (byte) (val >> 16),
//                    (byte) (val >> 8),
//                    (byte) (val)
//            };
//            if(shiftBy == 0) byteVals[1] = (byte) (byteVals[1] | COMPRESSED.pop());
//            else if(shiftBy > 1) {
//                byteVals[0] = (byte) (byteVals[0] | COMPRESSED.pop());
//                COMPRESSED.push(byteVals[0]);
//            }
//            COMPRESSED.push(byteVals[1]);
//            COMPRESSED.push(byteVals[2]);
//            COMPRESSED.push(byteVals[3]);
//
//            lastBoundary = (byte) ((7 + lastBoundary) % 8);
//
//            encoded += m.getLength();
//        } else {
//            int start = encodedLength - posInStream;
//            int end = bytes.length <= Compressor.MIN_MATCH - 1 ? bytes.length : bytes.length - Compressor.MIN_MATCH + 1;
//            for (int i = start; i < end && i != matchIndex; i++) {
//                if(i > matchIndex && matchIndex > 0) throw new RuntimeException("index > matchIndex");
//                OUTPUT.append("(0,'")
//                        .append(String.format("%02X", bytes[i]))
//                        .append("')");
//                a = new byte[]{
//                        (byte) ((byte) (bytes[i] >> (1 + lastBoundary)) & (0x7F >> lastBoundary)),
//                        (byte) (bytes[i] << (7 - lastBoundary))};
//
//                if(lastBoundary != 0) a[0] = (byte) (COMPRESSED.pop() | a[0]);
//
//                lastBoundary = (byte) ((1 + lastBoundary) % 8);
//                COMPRESSED.push(a[0]);
//                COMPRESSED.push(a[1]);
//                encoded++;
//            }
//        }
//        if(m != null) m.recycle();
//        return encoded;
//    }

    private static void decode() {
        Byte[] array = COMPRESSED.toArray(new Byte[0]);
        List<Byte> list = new Decoder(array).decode().container;
//        System.out.printf("Required %d bytes to compress %d in %s\n", COMPRESSED.size(), INPUT.size(), stringifyByteList(COMPRESSED));
//        System.out.println("INPUT:        " + stringifyByteList(INPUT));
//        System.out.println("UNCOMPRESSED: " + stringifyByteList(list));
    }

    private static String stringifyByteList(List<Byte> list) {
        StringBuilder builder = new StringBuilder();
        for (Byte b : list) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
}
