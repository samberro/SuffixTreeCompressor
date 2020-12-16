package com.samberro;

import com.samberro.codec.Coder;
import com.samberro.codec.Decoder;
import com.samberro.matcher.Matcher;
import com.samberro.matcher.Matcher.MatchListener;
import com.samberro.matcher.MatcherImp;
import com.samberro.trie.NodeRecycler;
import com.samberro.trie.SuffixTrie;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.samberro.utils.Utils.*;

public class Main {

    public static void main(String[] args) throws IOException {
        byte[] bytes = fromFile(2_000_000);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Coder packer = new Coder(new BufferedOutputStream(out));
        Matcher matcher = new MatcherImp(new MatchListener() {
            @Override
            public void onMatchReady(int originIndex, int destIndex, int length) {
                packer.writeMatchedBytes(originIndex, destIndex, length);
            }

            @Override
            public void onMatchFailed(int destIndex, int length) {
                for (int i = 0; i < length; i++) {
                    packer.writeUncompressedByte(bytes[destIndex + i]);
                }
            }
        });
        SuffixTrie suffixTrie = new SuffixTrie(matcher);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            suffixTrie.insertByte(b, i);
        }
        matcher.finish();
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
        if (!s.equals(inputStr)) throw new RuntimeException("Not equal");
    }
}
