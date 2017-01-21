package net.betaengine.naivebenchmarks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

import com.google.common.base.Verify;

public class Disk extends AbstractBenchmark {
    public Disk(int cycles, long len) {
        super(cycles, len);
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        LongBuffer longBuffer = ByteBuffer.wrap(buffer).asLongBuffer();
        XorShift64 rand = new XorShift64();

        while (longBuffer.hasRemaining()) {
            longBuffer.put(rand.next());
        }
        
        String filename = UUID.randomUUID().toString() + ".tmp";
        File file = new File(filename);
        long steps = getLen() / BUFFER_SIZE;
        long len = steps * BUFFER_SIZE; // len is definitely a multiple of BUFFER_SIZE, unlike getLen() value.

        // If `len` is low then the first write is noticeably quicker than the subsequent ones (presumably for some OS related reason).
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
        
        // If `len` is low then read is massively faster than write - presumably because the OS has what was just written in cache.
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
    }
}