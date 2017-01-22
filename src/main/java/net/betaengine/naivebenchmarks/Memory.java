package net.betaengine.naivebenchmarks;

import java.nio.LongBuffer;

import com.google.common.base.Verify;

public class Memory extends AbstractBenchmark {
    public Memory(int cycles, long len) {
        super(cycles, len);
    }
    
    @Override
    public void run() {
        int bufferSize = BUFFER_SIZE / Long.BYTES;
        int len = normalize(getLen(), bufferSize);
        long[] memory = new long[len];
        
        randomFill(LongBuffer.wrap(memory));
        
        long[] buffer = new long[bufferSize];
        
        measure("memory read", () -> {
            for (int offset = 0; offset < memory.length; offset += buffer.length) {
                System.arraycopy(memory, offset, buffer, 0, buffer.length);
            }
        });
        
        measure("memory write", () -> {
            for (int offset = 0; offset < memory.length; offset += buffer.length) {
                System.arraycopy(buffer, 0, memory, offset, buffer.length);
            }
        });
    }
    
    private int normalize(long len, int bufferSize) {
        len /= Long.BYTES; // Convert to 8-byte words.
        
        // Make len a multiple of BUFFER_SIZE.
        len /= bufferSize;
        len *= bufferSize;
        
        // http://stackoverflow.com/a/8381338/245602
        Verify.verify(len < Integer.MAX_VALUE - 8);

        return (int)len;
    }
}