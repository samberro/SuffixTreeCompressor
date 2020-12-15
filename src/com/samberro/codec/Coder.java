package com.samberro.codec;

import java.io.BufferedOutputStream;
import java.io.IOException;

import static com.samberro.matcher.Matcher.MIN_MATCH;

public class Coder {
    private static final int NUM_BITS_UNCOMPRESSED_BYTE = 9;
    private static final int NUM_BITS_COMPRESSED_STRING = 23;

    private BufferedOutputStream outputStream;
    private int fence = 0; //byte fence
    private int outval = 0;
    byte[] outBytes = new byte[4];
    private int bytesWritten = 0;

    public Coder(BufferedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeUncompressedByte(byte b) {
        if (fence + NUM_BITS_UNCOMPRESSED_BYTE <= 32) {
            outval |= (b & 0xFF) << (23 - fence);
            fence = (fence + NUM_BITS_UNCOMPRESSED_BYTE) % 32;
            if (fence == 0) {
                write();
                outval = 0;
            }
        } else {
            fence = (fence + NUM_BITS_UNCOMPRESSED_BYTE) % 32;
            outval |= (b & 0xFF) >>> fence;
            write();
            outval = (b & 0xFF) << (32 - fence);
        }
    }

    private void write() {
        byteSize(outval);
        writeToStream(outBytes, 4);
    }

    private void byteSize(int val) {
        outBytes[0] = (byte) (val >> 24);
        outBytes[1] = (byte) (val >> 16);
        outBytes[2] = (byte) (val >> 8);
        outBytes[3] = (byte) (val);
    }

    public void writeMatchedBytes(int originIndex, int destIndex, int length) {
        int relativePos = destIndex - originIndex;
        int encodedLength = length - MIN_MATCH;
        int val = 1 << 22;
        val |= (relativePos & 0xFFFF) << 6;
        val |= encodedLength & 0x3F;

        if (fence + NUM_BITS_COMPRESSED_STRING <= 32) {
            outval |= val << (9 - fence);
            fence = (fence + NUM_BITS_COMPRESSED_STRING) % 32;
            if (fence == 0) {
                write();
                outval = 0;
            }
        } else {
            fence = (fence + NUM_BITS_COMPRESSED_STRING) % 32;
            outval |= val >>> fence;
            write();
            outval = val << (32 - fence);
        }
    }

    public void close() throws IOException {
        if (fence != 0) {
            byteSize(outval);
            int numBytes = fence / 8 + 1;
            writeToStream(outBytes, numBytes);
        }

        outputStream.flush();
        outputStream.close();

        System.out.println("Bytes written: " + bytesWritten);
    }

    private void writeToStream(byte[] byteVals, int numBytes) {
        try {
            outputStream.write(byteVals, 0, numBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytesWritten += numBytes;
    }
}
