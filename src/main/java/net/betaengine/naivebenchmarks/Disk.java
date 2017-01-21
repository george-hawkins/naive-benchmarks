package net.betaengine.naivebenchmarks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.google.common.base.Verify;

public class Disk extends AbstractBenchmark {
    private final static int OVERHEAD = 1024 * 1024; // 1MiB.
    
    public Disk(int cycles, long len) {
        super(cycles, len);
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        randomFill(buffer);
        
        String filename = UUID.randomUUID().toString() + ".tmp";
        File file = new File(filename);
        long steps = getLen() / BUFFER_SIZE;
        long len = steps * BUFFER_SIZE; // len is definitely a multiple of BUFFER_SIZE, unlike getLen() value.
        
        byte[] stolenMemory = stealFreeMemory();

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
    
    // Ideally the benchmarks should be given nearly all the system's free memory using e.g. -Xms4g - this routine grabs all the memory
    // it can so that it's not available to the OS for the disk caching purposes that the OS can otherwise put free memory to use as.
    private byte[] stealFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalFreeMemory = runtime.maxMemory() - usedMemory;
        
        long len = totalFreeMemory - OVERHEAD;
        
        // http://stackoverflow.com/a/8381338/245602
        Verify.verify(len < Integer.MAX_VALUE - 8);
        
        byte[] memory = new byte[(int)len];
        
        randomFill(memory);
        
        return memory;
    }
}