package com.samberro;

import java.util.ArrayList;
import java.util.List;

import static com.samberro.NextAction.DONE;
import static com.samberro.NextAction.READ_ACTION_FLAG;
import static com.samberro.utils.Utils.rightShiftUnsigned;

public class Decoder {
    private NextAction nextAction;
    private int index;
    private int boundaryStart;
    private Byte[] array;

    public List<Byte> container;

    public Decoder(Byte[] array) {
        this.array = array;
        nextAction = READ_ACTION_FLAG;
        index = 0;
        boundaryStart = 0;
        container = new ArrayList<>();
    }

    public Decoder decode() {
        while (nextAction != DONE) {
            switch (nextAction) {
                case READ_ACTION_FLAG:
                    decodeAction();
                    break;
                case READ_BYTE_VAL:
                    container.add(decodeByteValue());
                    break;
                case READ_COMPRESSED_INFO:
                    container.addAll(decodeCompressedBytes());
                    break;
            }
        }

        return this;
    }

    private List<Byte> decodeCompressedBytes() {
        int offset = (peekByte(index++, boundaryStart) << 8) | peekByte(index++, boundaryStart);
        byte length = rightShiftUnsigned(peekByte(index, boundaryStart), 2);
        incrementBoundaryStart(6);
        nextAction = READ_ACTION_FLAG;
        return container.subList(container.size() - offset, container.size() - offset + length);
    }

    private byte decodeByteValue() {
        byte val = peekByte(index++, boundaryStart);
        nextAction = READ_ACTION_FLAG;
        return val;
    }

    private byte peekByte(int index, int boundaryStart) {
        byte val = array[index];
        if (boundaryStart != 0) {
            val = (byte) ((val << boundaryStart) | (rightShiftUnsigned(array[index + 1], 8 - boundaryStart)));
        }
        return val;
    }

    private void decodeAction() {
        if (index >= array.length - 1) nextAction = DONE;
        else {
            byte b = (byte) (array[index] << boundaryStart);
            incrementBoundaryStart(1);
            boolean isCompressed = (b & 0x80) == 0x80;
            nextAction = isCompressed ? NextAction.READ_COMPRESSED_INFO : NextAction.READ_BYTE_VAL;
        }
    }

    private void incrementBoundaryStart(int inc) {
        boundaryStart += inc;
        index += boundaryStart >> 3;
        boundaryStart = boundaryStart % 8;
    }
}
