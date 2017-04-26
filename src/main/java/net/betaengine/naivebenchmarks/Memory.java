package net.betaengine.naivebenchmarks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Verify;

public class Memory extends AbstractBenchmark {
    private final Logger logger = LoggerFactory.getLogger(Memory.class);

    public Memory(int cycles, long len) {
        super(cycles, len);
    }
    
    @Override
    public void run() {
        if (getLen() != 0) {
            runBenchmark();
        } else {
            suggestLen();
        }
    }
    
    private void runBenchmark() {
        int bufferSize = BUFFER_SIZE / Long.BYTES;
        int len = normalize(getLen(), bufferSize);
        long[] memory = new long[len];
        
        XorShift64.randomFill(memory);
        
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
    
    private void suggestLen() {
        long[] result = MemoryHog.hog(Runtime.getRuntime().maxMemory(), MemoryHog.ONE_MB);
        
        logger.info("set memory.size={}B in the conf file for repeatable results on this machine", HumanReadable.toString((long) result.length * Long.BYTES, false));
        System.exit(0);
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