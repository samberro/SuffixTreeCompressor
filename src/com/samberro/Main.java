package com.samberro;

import com.samberro.codec.Coder;
import com.samberro.codec.Decoder;
import com.samberro.trie.Node;
import com.samberro.trie.NodeRecycler;
import com.samberro.trie.SuffixTrie;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.samberro.utils.Utils.*;

public class Main {

    public static void main(String[] args) throws IOException {
        byte[] bytes = fromFile(500_000);
        run(bytes);
    }

    private static void run(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Coder packer = new Coder(new BufferedOutputStream(out));
        SuffixTrie suffixTrie = new SuffixTrie();
        long startTime = System.currentTimeMillis();
        int written = -1;
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];

            if(written < i) {
                Node n = suffixTrie.findLongestPrefix(bytes, i);
                if (n != null && n.getDepth() >= 3) {
                    packer.writeMatchedBytes(n.getLastIndex() - n.getDepth() + 1, i, n.getDepth());
                    written += n.getDepth();
                } else {
                    packer.writeUncompressedByte(b);
                    written++;
                }
            }

            suffixTrie.insertByte(b, i);
        }
        packer.close();
        System.out.printf("Finished building tree in %d ms\n", System.currentTimeMillis() - startTime);
        System.out.printf("Nodes created: %.2fmil, Recycled: %.2fmil\n",
                NodeRecycler.Stats.nodesCreated / 1_000_000f, NodeRecycler.Stats.nodesRecycled / 1_000_000f);
        byte[] compressed = out.toByteArray();
        System.out.printf("Required %s bytes to compress %s\n", humanReadableByteCountSI(compressed.length), humanReadableByteCountSI(bytes.length));

        decode(bytes, compressed);
//        testTrie(bytes, suffixTrie);
    }

    private static void decode(byte[] input, byte[] compressed) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String string = new Decoder(compressed, new BufferedOutputStream(os)).withDebug(input).decode().getStringRepresentation();
        os.close();
        byte[] uncompressed = os.toByteArray();
        String inputStr = toByteString(input);
        System.out.println("INPUT:        " + inputStr.substring(0, Math.min(100, inputStr.length())));
        System.out.println("COMPRESSED  : " + string.substring(0, Math.min(100, string.length())));
        String s = toByteString(uncompressed);
        System.out.println("UNCOMPRESSED: " + s.substring(0, Math.min(100, s.length())));
        if (!s.equals(inputStr)) System.err.println("Not equal");
    }
}
