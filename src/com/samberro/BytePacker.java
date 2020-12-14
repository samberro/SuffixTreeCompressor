package com.samberro;

import com.samberro.matcher.MatchInfo;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class BytePacker {
    private static final int NUM_BITS_UNCOMPRESSED_BYTE = 9;
    private static final int NUM_BITS_COMPRESSED_STRING = 23;

    private BufferedOutputStream outputStream;
    private int fence = 0; //byte fence
    private int outval = 0;
    private boolean needFlush = false;

    public BytePacker(BufferedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeUncompressedByte(byte b) throws IOException {
        if (fence + NUM_BITS_UNCOMPRESSED_BYTE <= 32) {
            outval |= (b & 0xFF) << (23 - fence);
            fence = (fence + NUM_BITS_UNCOMPRESSED_BYTE) % 32;
            if (fence == 0) {
                outputStream.write(outval);
                outval = 0;
                needFlush = false;
            }
        } else {
            fence = (fence + NUM_BITS_UNCOMPRESSED_BYTE) % 32;
            outval |= (b & 0xFF) >>> fence;
            outputStream.write(outval);
            outval = b << (32 - fence);
            needFlush = true;
        }
    }

    public void writeMatchedBytes(MatchInfo matchInfo, int streamIndex) throws IOException {
        int relativePos = streamIndex - matchInfo.getMatchPos();
        int val = 1 << 22;
        val |= (relativePos & 0xFFFF) << 6;
        val |= matchInfo.getMatchLength() & 0x3F;

        if (fence + NUM_BITS_COMPRESSED_STRING <= 32) {
            outval |= val << (9 - fence);
            fence = (fence + NUM_BITS_COMPRESSED_STRING) % 32;
            if (fence == 0) {
                outputStream.write(outval);
                outval = 0;
                needFlush = false;
            }
        } else {
            fence = (fence + NUM_BITS_COMPRESSED_STRING) % 32;
            outval |= val >>> fence;
            outputStream.write(outval);
            outval = val << (32 - fence);
            needFlush = true;
        }
    }

    public void close() throws IOException {
        if (needFlush) {
            byte[] byteVals = new byte[]{
                    (byte) (outval >> 24),
                    (byte) (outval >> 16),
                    (byte) (outval >> 8),
                    (byte) (outval)
            };
            int numBytes = fence / 8 + 1;
            outputStream.write(byteVals, 0, numBytes);
        }

        outputStream.flush();
        outputStream.close();
    }
}
