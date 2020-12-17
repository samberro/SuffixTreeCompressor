package com.samberro.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ConsoleByteStringWriter extends ByteArrayOutputStream {
    @Override
    public void close() throws IOException {
        super.close();
        System.out.println("OUTPUT: " + InputDataGenerator.toByteString(this.toByteArray()));
    }
}
