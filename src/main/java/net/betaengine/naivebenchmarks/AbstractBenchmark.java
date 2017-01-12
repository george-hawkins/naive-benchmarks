package net.betaengine.naivebenchmarks;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBenchmark {
    private final Logger logger = LoggerFactory.getLogger(AbstractBenchmark.class);
    
    protected final static int BUFFER_SIZE = 8192 / Long.BYTES; // Same as used by BufferedWriter and similar classes.
    
    private final int cycles;
    private final long len;
    
    public AbstractBenchmark(int cycles, long len) {
        this.cycles = cycles;
        this.len = len;
    }
    
    protected int getCycles() { return cycles; }
    
    protected long getLen() { return len; }
    
    public abstract void run();
    
    protected void measure(String name, int cycles, Runnable job) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 0; i < cycles; i++) {
            long start = System.currentTimeMillis();
            
            job.run();
            
            long duration = System.currentTimeMillis() - start;
            stats.addValue(duration);
        }
        
        logger.info("{} - {}", name, summarize(stats));
    }
    
    protected void fatal(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }
    
    private String summarize(DescriptiveStatistics stats) {
        return String.format("min=%dms, max=%dms, median=%dms, std-dev=%f", (int)stats.getMin(), (int)stats.getMax(), (int)stats.getPercentile(50), stats.getStandardDeviation());
    }
}