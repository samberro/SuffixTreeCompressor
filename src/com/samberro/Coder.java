package com.samberro;

import com.samberro.matcher.MatchInfo;

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

    public void writeUncompressedByte(byte b) throws IOException {
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
            outval = b << (32 - fence);
        }
    }

    private void write() throws IOException {
        byteSize(outval);
        writeToStream(outBytes, 4);
    }

    private void byteSize(int val) {
        outBytes[0] = (byte) (val >> 24);
        outBytes[1] = (byte) (val >> 16);
        outBytes[2] = (byte) (val >> 8);
        outBytes[3] = (byte) (val);
    }

    public void writeMatchedBytes(MatchInfo matchInfo, int streamIndex) throws IOException {
        int relativePos = matchInfo.getDestIndex() - matchInfo.getOriginalIndex();
        int length = matchInfo.getMatchLength() - MIN_MATCH;
        int val = 1 << 22;
        val |= (relativePos & 0xFFFF) << 6;
        val |= length & 0x3F;

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
    }

    private void writeToStream(byte[] byteVals, int numBytes) throws IOException {
        outputStream.write(byteVals, 0, numBytes);
        bytesWritten += numBytes;
    }
}
