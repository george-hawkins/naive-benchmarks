package net.betaengine.naivebenchmarks;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Benchmarks {
    public final static Config CONFIG = ConfigFactory.load(); // Specify file with -Dconfig.file=...
    public final static int READ_PORT = CONFIG.getInt("network.read.port");
    public final static int WRITE_PORT = CONFIG.getInt("network.write.port");
    
    private final Logger logger = LoggerFactory.getLogger(Benchmarks.class);
    
    private void showSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        // totalMemory() is whatever the JVM has *currently* allocated while maxMemory is the limit established by -Xmx4g or whatever.
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalFreeMemory = runtime.maxMemory() - usedMemory;
        
        logger.info("total free JVM memory - {}B", HumanReadable.toString(totalFreeMemory, false));

        for (File root : File.listRoots()) {
            logger.info("file system \"{}\" has {}B usable space", root.getAbsolutePath(), HumanReadable.toString(root.getUsableSpace(), false));
        }
    }
    
    private void run() {
        // It's a pity the config details aren't more specific.
        logger.info("using config {}", CONFIG.origin());
        
        if (CONFIG.getBoolean("processor.enabled")) {
            logger.info("running processor benchmark");
                
            Processor processor = new Processor(CONFIG.getInt("processor.cycles"), CONFIG.getInt("processor.width"));
            
            processor.run();
        }
        
        if (CONFIG.getBoolean("memory.enabled")) {
            logger.info("running memory benchmark");
                
            Memory memory = new Memory(CONFIG.getInt("memory.cycles"), CONFIG.getBytes("memory.size"));
            
            memory.run();
        }
        
        if (CONFIG.getBoolean("disk.enabled")) {
            logger.info("running disk benchmark");
                
            Disk disk = new Disk(CONFIG.getInt("disk.cycles"), CONFIG.getBytes("disk.size"));
            
            disk.run();
        }
        
        if (CONFIG.getBoolean("network.enabled")) {
            logger.info("running network benchmark");
            
            Network network = new Network(CONFIG.getInt("network.cycles"), CONFIG.getInt("network.count"),
                    CONFIG.getString("network.server"), READ_PORT, WRITE_PORT);
            
            network.run();
        }
        
        logger.info("finished");
    }

    public static void main(String[] args) {
        Benchmarks benchmarks = new Benchmarks();
        
        benchmarks.showSystemInfo();
        benchmarks.run();
    }
}
