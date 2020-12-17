package com.samberro.codec;

import com.samberro.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;

import static com.samberro.trie.SuffixTrie.MIN_MATCH;

/**
 * Packs and writes the compressed data to a stream
 */
public class Coder {
    private static final int NUM_BITS_UNCOMPRESSED_BYTE = 9;
    private static final int NUM_BITS_COMPRESSED_STRING = 23;

    private OutputStream outputStream;
    private int fence = 0; //byte fence
    private int outVal = 0;
    byte[] outBytes = new byte[4];
    private int bytesWritten = 0;
    private boolean debug;
    private StringBuilder stringBuilder = new StringBuilder();

    public Coder(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Coder withDebug(boolean debug) {this.debug = debug; return this;}

    public void writeUncompressedByte(byte b) {
        if (fence + NUM_BITS_UNCOMPRESSED_BYTE <= 32) {
            outVal |= (b & 0xFF) << (23 - fence);
            fence = (fence + NUM_BITS_UNCOMPRESSED_BYTE) % 32;
            if (fence == 0) {
                write();
                outVal = 0;
            }
        } else {
            fence = (fence + NUM_BITS_UNCOMPRESSED_BYTE) % 32;
            outVal |= (b & 0xFF) >>> fence;
            write();
            outVal = (b & 0xFF) << (32 - fence);
        }

        if(debug) stringBuilder.append("(0,").append(String.format("%02X", b)).append(")");
    }

    private void write() {
        byteSize(outVal);
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
            outVal |= val << (9 - fence);
            fence = (fence + NUM_BITS_COMPRESSED_STRING) % 32;
            if (fence == 0) {
                write();
                outVal = 0;
            }
        } else {
            fence = (fence + NUM_BITS_COMPRESSED_STRING) % 32;
            outVal |= val >>> fence;
            write();
            outVal = val << (32 - fence);
        }

        if(debug) stringBuilder.append("(1,").append(String.format("-%d", relativePos))
                .append(",").append(length).append(")");
    }

    public void close() throws IOException {
        if (fence != 0) {
            byteSize(outVal);
            int numBytes = fence / 8 + 1;
            writeToStream(outBytes, numBytes);
        }

        outputStream.flush();
        outputStream.close();

//        System.out.println("Bytes written: " + bytesWritten);
        if(debug) System.out.println("COMPRESSED: " + stringBuilder.toString());
    }

    private void writeToStream(byte[] byteVals, int numBytes) {
        try {
            outputStream.write(byteVals, 0, numBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytesWritten += numBytes;
        if(bytesWritten % 10_000 == 0) System.out.println("Bytes Written: " +
                Utils.humanReadableByteCountSI(bytesWritten));
    }

    public int getBytesWritten() {
        return bytesWritten;
    }
}
