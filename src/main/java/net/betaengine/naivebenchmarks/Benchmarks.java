package net.betaengine.naivebenchmarks;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Benchmarks {
    private final Logger logger = LoggerFactory.getLogger(Benchmarks.class);
    private final Config config = ConfigFactory.load(); // Specify file with -Dconfig.file=...
    
    private void showSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        // totalMemory() is whatever the JVM has *currently* allocated while maxMemory is the limit established by -Xmx4g or whatever.
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalFreeMemory = runtime.maxMemory() - usedMemory;
        
        logger.info("total free JVM memory - {}", HumanReadable.byteCount(totalFreeMemory, false));

        for (File root : File.listRoots()) {
            logger.info("file system \"{}\" has {} usable space", root.getAbsolutePath(), HumanReadable.byteCount(root.getUsableSpace(), false));
        }
    }

    
    private void run() {
        Processor processor = new Processor(config.getInt("processor.cycles"), config.getInt("processor.width"));
        
        processor.run();
//        Disk disk = new Disk(config.getInt("disk.cycles"), config.getBytes("disk.size"));
//        
//        disk.run();
//        
//        Memory memory = new Memory(config.getInt("memory.cycles"), config.getBytes("memory.size"));
//        
//        memory.run();
        
        logger.info("finished");
    }

    public static void main(String[] args) {
        Benchmarks benchmarks = new Benchmarks();
        
        benchmarks.showSystemInfo();
        benchmarks.run();
    }
}
