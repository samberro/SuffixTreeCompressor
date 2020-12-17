package com.samberro.codec;

/** Array backed circular buffer.
 * This allows us to quickly index bytes within a certain range (capacity) of the bufferHead.
 * Buffer overruns are not guarded by design
 *
 */
public class CircularByteBuffer {
    private byte[] buffer;
    private int bufferHead;
    private int cap;

    public CircularByteBuffer(int capacity) {
        cap = capacity;
        buffer = new byte[capacity];
        bufferHead = 0;
    }

    /**
     * Add byte to the buffer. This updates the buffer head and
     * wraps around if we are past the end of the array
     * @param b byte to add
     */
    public void push(byte b) {
        buffer[bufferHead++] = b;
        if (bufferHead >= cap) bufferHead = 0;
    }

    /**
     * Adds length bytes into the buffer
     * @param bytes the array of bytes to add
     * @param length number of bytes to add
     */
    public void push(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) push(bytes[i]);
    }

    /**
     * Retrieve a byte at a relative position from the last write
     * @param index the relative position to last write of byte to grab
     * @return the byte from the buffer
     */
    public byte at(int index) {
        return buffer[relativeToAbsoluteIndex(index)];
    }

    /**
     * Retrieve length bytes at a relative position from the last write
     * @param index relative position to last write of needed bytes
     * @param length number of bytes to read
     * @param out output array to copy bytes to
     * @return number of bytes read
     */
    public int at(int index, int length, byte[] out) {
        for (int i = 0; i < length; i++) {
            out[i] = at(index - i);
        }
        return length;
    }

    /**
     * Calculates the absolute index into the backing array
     * @param i relative index
     * @return absolute index into the array
     */
    private int relativeToAbsoluteIndex(int i) {
        int index = bufferHead - i;
        if (index < 0) index += cap;
        return index;
    }
}
