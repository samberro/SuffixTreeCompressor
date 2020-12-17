package com.samberro.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteCaptureOutputStream extends ByteArrayOutputStream {
    private OutputStream os;

    public ByteCaptureOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) {
        try {
            os.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        os.flush();
        os.close();
    }
}
