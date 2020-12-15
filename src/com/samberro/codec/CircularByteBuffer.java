package com.samberro.codec;

public class CircularByteBuffer {
    private byte[] buffer;
    private int bufferHead;
    private int cap;

    public CircularByteBuffer(int capacity) {
        cap = capacity;
        buffer = new byte[capacity];
        bufferHead = 0;
    }

    public void push(byte b) {
        buffer[bufferHead++] = b;
        if (bufferHead >= cap) bufferHead = 0;
    }

    public void push(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) push(bytes[i]);
    }

    public byte at(int index) {
        return buffer[relativeIndexFromTop(index)];
    }

    public int at(int index, int length, byte[] out) {
        for (int i = 0; i < length; i++) {
            out[i] = at(index - i);
        }
        return length;
    }

    private int relativeIndexFromTop(int i) {
        int index = bufferHead - i;
        if (index < 0) index += cap;
        return index;
    }
}
