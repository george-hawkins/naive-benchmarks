package net.betaengine.naivebenchmarks;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBenchmark {
    private final Logger logger = LoggerFactory.getLogger(AbstractBenchmark.class);
    
    protected final static int BUFFER_SIZE = 8192; // Same as used by BufferedWriter and similar classes.
    
    private final int cycles;
    private final long len;
    
    public AbstractBenchmark(int cycles, long len) {
        this.cycles = cycles;
        this.len = len;
    }
    
    protected long getLen() { return len; }
    
    public abstract void run();
    
    protected int measure(String name, Runnable job) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 0; i < cycles; i++) {
            long start = System.currentTimeMillis();
            
            job.run();
            
            long duration = System.currentTimeMillis() - start;
            stats.addValue(duration);
        }
        
        logger.info("{} - {}", name, summarize(stats));
        
        return median(stats);
    }
    
    protected void fatal(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }
    
    private int median(DescriptiveStatistics stats) {
        return (int)stats.getPercentile(50);
    }
    
    private String summarize(DescriptiveStatistics stats) {
        return String.format("min=%dms, max=%dms, median=%dms, std-dev=%f", (int)stats.getMin(), (int)stats.getMax(), median(stats), stats.getStandardDeviation());
    }
}