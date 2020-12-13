package com.samberro.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ByteCollector implements Collector<Byte, List<Byte>, byte[]>{
    @Override
    public Supplier<List<Byte>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<Byte>, Byte> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<Byte>> combiner() {
        return (bytes, bytes2) -> {
            bytes.addAll(bytes2);
            return bytes;
        };
    }

    @Override
    public Function<List<Byte>, byte[]> finisher() {
        return bytes -> {
            byte[] a = new byte[bytes.size()];
            for(int i = 0; i < a.length; i++) {
                a[i] = bytes.get(i);
            }
            return a;
        };
    }

    @Override
    public Set<Collector.Characteristics> characteristics() {
        return Set.of();
    }
}
