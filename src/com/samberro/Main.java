package com.samberro;

import com.samberro.codec.Coder;
import com.samberro.codec.Decoder;
import com.samberro.trie.Node;
import com.samberro.trie.NodeRecycler;
import com.samberro.trie.SuffixTrie;
import com.samberro.utils.ByteCaptureOutputStream;
import com.samberro.utils.ConsoleByteStringWriter;
import com.samberro.utils.Options;
import com.samberro.utils.PostProcessFileWriter;

import java.io.*;

import static com.samberro.trie.SuffixTrie.MIN_MATCH;
import static com.samberro.utils.InputDataGenerator.fromByteString;
import static com.samberro.utils.Options.parseOptions;
import static com.samberro.utils.Utils.decodeAndTest;
import static com.samberro.utils.Utils.humanReadableByteCountSI;

public class Main {

    public static void main(String[] args) throws IOException {
        Options opts = parseOptions(args);

        InputStream is = opts.getInputType() == Options.InputOutputType.ByteString ?
                new ByteArrayInputStream(fromByteString(opts.getInputByteString())) :
                new BufferedInputStream(new FileInputStream(opts.getInputFilename()));

        OutputStream os = opts.getOutputType() == Options.InputOutputType.ByteString ?
                new ConsoleByteStringWriter() :
                new PostProcessFileWriter(opts.getOutputFilename());

        byte[] input = is.readAllBytes();
        is.close();

        if(opts.compress()) {
            compress(input, os, opts.isDebugMode());
        } else {
            decompress(input, os, opts.isDebugMode());
        }
    }

    private static void decompress(byte[] input, OutputStream os, boolean debug) throws IOException {
        Decoder decoder = new Decoder(os)
                .withDebug(debug, null)
                .decode(input);
        os.close();
        if(debug) System.out.println("Decoded: " + decoder.getStringRepresentation());
    }

    private static void compress(byte[] input, OutputStream os, boolean debug) throws IOException {
        if(debug) os = new ByteCaptureOutputStream(os);
        Coder coder = new Coder(os).withDebug(debug);
        SuffixTrie suffixTrie = new SuffixTrie();
        int streamIndex = 0;
        long startTime = System.currentTimeMillis();
        int bytesProcessed = -1;
        for (int i = 0; i < input.length; i++) {
            byte b = input[i];

            if(bytesProcessed < streamIndex + i) {
                Node n = suffixTrie.findLongestPrefix(input, i);
                if (n != null && n.getDepth() >= MIN_MATCH) {
                    coder.writeMatchedBytes(n.getLastIndex() - n.getDepth() + 1, i, n.getDepth());
                    bytesProcessed += n.getDepth();
                } else {
                    coder.writeUncompressedByte(b);
                    bytesProcessed++;
                }
            }
            suffixTrie.insertByte(b, i);
        }
        coder.close();

        System.out.printf("Finished in %d ms\n", System.currentTimeMillis() - startTime);
        System.out.printf("Nodes created: %.2fmil, Recycled: %.2fmil\n",
                NodeRecycler.Stats.nodesCreated / 1_000_000f, NodeRecycler.Stats.nodesRecycled / 1_000_000f);
        System.out.printf("Required %s bytes to compress %s\n", humanReadableByteCountSI(coder.getBytesWritten()),
                humanReadableByteCountSI(input.length));

        if(debug) decodeAndTest(input, ((ByteArrayOutputStream)os).toByteArray());
    }
}
