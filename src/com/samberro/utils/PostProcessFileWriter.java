package com.samberro.utils;

import java.io.*;

public class PostProcessFileWriter extends ByteArrayOutputStream {
    private BufferedOutputStream fos;

    public PostProcessFileWriter(String filename) throws FileNotFoundException {
        fos = new BufferedOutputStream(new FileOutputStream(filename));
    }

    @Override
    public void close() throws IOException {
        super.close();
        fos.write(toByteArray());
        fos.flush();
        fos.close();
    }
}
