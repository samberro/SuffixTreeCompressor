package com.samberro.utils;

import java.util.*;

public class Utils {

    public static byte rightShiftUnsigned(byte b, int shift) {
        if(shift == 0) return b;
        if(shift >= 8) return 0;
        return (byte) ((b >> shift) & ~(((byte)0x80) >> (shift - 1)));
    }

    public static String generateInput(int size) {
        Random rand = new Random(System.currentTimeMillis());
        byte[] bytes = new byte[size];
        rand.nextBytes(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    public static List<Byte> fromString(String s) {
        List<Byte> list = new ArrayList<>();
        if(s.length() % 2 != 0) throw new RuntimeException("String is not even. Length = " + s.length());
        for (int i = 0; i < s.length() - 1; i+=2) {
            list.add((byte) Integer.parseInt(s, i, i + 2, 16));
        }
        return list;
    }
}
