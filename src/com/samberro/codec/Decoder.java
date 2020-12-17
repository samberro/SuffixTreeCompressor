package com.samberro.codec;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.samberro.trie.SuffixTrie.MAX_SUFFIX_LENGTH;
import static com.samberro.codec.Decoder.Action.*;
import static com.samberro.trie.SuffixTrie.MIN_MATCH;
import static com.samberro.utils.Utils.rightShiftUnsigned;

public class Decoder {
    private Action action;
    private int index;
    private int fence;
    private byte[] in;
    private byte[] buffer;
    private CircularByteBuffer decodedBuffer;
    private OutputStream outputStream;
    private StringBuilder stringBuilder;
    private byte[] original;
    private int originalIndex;

    public Decoder(byte[] array, BufferedOutputStream outputStream) {
        this.in = array;
        this.outputStream = outputStream;
        action = READ_ACTION_FLAG;
        index = 0;
        fence = 0;
        buffer = new byte[MAX_SUFFIX_LENGTH];
        decodedBuffer = new CircularByteBuffer(0xFFFF);
        stringBuilder = new StringBuilder();
    }

    public Decoder decode() throws IOException {
        while (action != DONE) {
            int read = 0;
            switch (action) {
                case READ_ACTION_FLAG:
                    decodeAction();
                    break;
                case READ_BYTE_VAL:
                    read = decodeByteValue(buffer);
                    break;
                case READ_COMPRESSED_INFO:
                    read = decodeCompressedBytes(buffer);
                    break;
            }
            updateString();
            writeBytes(read);
        }
        outputStream.flush();

        return this;
    }

    private void writeBytes(int read) throws IOException {
        if (read != 0) {
            outputStream.write(buffer, 0, read);
            decodedBuffer.push(buffer, read);
//            checkWithOriginal(buffer, read);
        }
    }

    private void checkWithOriginal(byte[] buffer, int read) {
        for (int i = 0; i < read; i++) {
            if (buffer[i] != original[originalIndex++])
                throw new RuntimeException("Mismatch at index " + (originalIndex - 1) + ", " +
                        stringBuilder.substring(stringBuilder.lastIndexOf("(0"), stringBuilder.length()));
        }
    }

    private void updateString() {
        if (action == READ_BYTE_VAL) appendRawByteEncoded(peekByte(index, fence));
        else if (action == READ_COMPRESSED_INFO) appendCompressedInfo(getReferencedOffset(), getReferencedLength());
    }

    private void appendCompressedInfo(int offset, byte length) {
        stringBuilder.append("(1,-").append(offset).append(",").append(length).append(")");
    }

    private void appendRawByteEncoded(byte b) {
        stringBuilder.append("(0,").append(String.format("%02X)", b));
    }

    private int decodeCompressedBytes(byte[] buffer) {
        int offset = getReferencedOffset();
        byte length = getReferencedLength();
        index += 2;
        incrementBoundaryStart(6);
        action = READ_ACTION_FLAG;
        return decodedBuffer.at(offset, length, buffer);
    }

    private byte getReferencedLength() {
        return (byte) (rightShiftUnsigned(peekByte(index + 2, fence), 2) + MIN_MATCH);
    }

    private int getReferencedOffset() {
        return (peekByte(index, fence) << 8 & 0xFFFF) | ((int) peekByte(index + 1, fence) & 0xFF);
    }

    private int decodeByteValue(byte[] buffer) {
        buffer[0] = peekByte(index++, fence);
        action = READ_ACTION_FLAG;
        return 1;
    }

    private byte peekByte(int index, int boundaryStart) {
        byte val = in[index];
        if (boundaryStart != 0) {
            // Address edge case where we are peeking length value (6 bits) and
            // it was written into the last bit of the stream
            byte lowerBits = index + 1 < in.length ? rightShiftUnsigned(in[index + 1], 8 - boundaryStart) : 0x00;
            val = (byte) ((val << boundaryStart) | lowerBits);
        }
        return val;
    }

    private void decodeAction() {
        if (index >= in.length - 1) action = DONE;
        else {
            byte b = (byte) (in[index] << fence);
            boolean isCompressed = (b & 0x80) == 0x80;
            incrementBoundaryStart(1);
            action = isCompressed ? Action.READ_COMPRESSED_INFO : READ_BYTE_VAL;
        }
    }

    private void incrementBoundaryStart(int inc) {
        fence += inc;
        index += fence >> 3;
        fence = fence % 8;
    }

    public String getStringRepresentation() {
        return stringBuilder.toString();
    }

    public Decoder withDebug(byte[] input) {
        this.original = input;
        return this;
    }

    public enum Action {
        READ_ACTION_FLAG, READ_BYTE_VAL, READ_COMPRESSED_INFO, DONE
    }

}
