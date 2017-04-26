package net.betaengine.naivebenchmarks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Verify;

public class Disk extends AbstractBenchmark {
    private final Logger logger = LoggerFactory.getLogger(Disk.class);
    
    public Disk(int cycles, long len) {
        super(cycles, len);
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        XorShift64.randomFill(buffer);
        
        String filename = UUID.randomUUID().toString() + ".tmp";
        File file = new File(filename);
        long steps = getLen() / BUFFER_SIZE;
        long len = steps * BUFFER_SIZE; // len is definitely a multiple of BUFFER_SIZE, unlike getLen() value.
        
        long[] stolenMemory = stealFreeMemory();

        // If `len` is small then the first write is noticeably quicker than the subsequent ones (presumably for some OS related reason).
        measure("file write", () -> {
            try {
                long total = 0;
                FileOutputStream output = new FileOutputStream(file);
                
                for (long step = 0; step < steps; step++) {
                    output.write(buffer, 0, buffer.length);
                    total += buffer.length;
                }
                
                output.close();
                Verify.verify(total == len);
            } catch (IOException e) {
                fatal(e);
            }
        });
        
        // If `len` is small then read is massively faster than write - presumably because the OS has what was just written in cache.
        measure("file read", () -> {
            try {
                FileInputStream input = new FileInputStream(file);
                long total = 0;
                int c;
                
                while ((c = input.read(buffer)) != -1) {
                    total += c;
                }
                
                input.close();
                Verify.verify(total == len);
            } catch (IOException e) {
                fatal(e);
            }
        });
        
        file.delete();
        
        // Just here to stop VM from being able to release referenced memory earlier (and stop IDE complaining about "unused").
        stolenMemory[0] = stolenMemory[1];
    }
    
    // Ideally the benchmarks should be given nearly all the system's free memory using e.g. -Xms4g
    // This routine grabs all the memory it can so that it's not available to the OS for disk caching purposes.
    private long[] stealFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalFreeMemory = runtime.maxMemory() - usedMemory;

        // I used to use totalFreeMemory as an upper limit. However uncollected garbage and various other factors affect the numbers
        // and depending on the situation one can often allocate more than the apparent total free memory or often allocate far less.
        long[] result = MemoryHog.hog(runtime.maxMemory(), MemoryHog.ONE_MB);
        
        // So the amount allocated may be greater than the amount that was apparently free.
        logger.info("was able to allocate {}B of {}B apparently free memory", HumanReadable.toString((long) result.length * Long.BYTES, false), HumanReadable.toString(totalFreeMemory, false));
        
        return result;
    }
}