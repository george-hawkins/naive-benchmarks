package net.betaengine.naivebenchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Verify;

public class Processor extends AbstractBenchmark {
    public Processor(int cycles, int len) {
        super(cycles, len);
    }
    
    @Override
    public void run() {
        int workerCount = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(workerCount);
        List<Callable<Double>> tasks = new ArrayList<>(workerCount);
        
        for (int i = 0; i < workerCount; i++) {
            tasks.add(createTask());
        }
        
        measure("processor load", () -> {
            try {
                double totalWork = service.invokeAll(tasks).stream().mapToDouble(this::getWork).sum();
                
                Verify.verify(totalWork > 0);
            } catch (InterruptedException e) {
                fatal(e);
            }
        });
        
        service.shutdown();
    }
    
    private double getWork(Future<Double> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            fatal(e);
            return -1;
        }
    }
    
    private Callable<Double> createTask() {
        WorkMandlebrot mandlebrot = new WorkMandlebrot((int)getLen());
        
        return () -> {
            mandlebrot.calculate();
            
            return mandlebrot.getWork();
        };
    }
    
    public static class WorkMandlebrot extends AbstractMandelbrot {
        private final static int MAX_ITERATIONS = 100;
        
        private double work = 0;
        
        public WorkMandlebrot(int width) {
            super(width, BOUNDS_REAL_MIN, BOUNDS_REAL_MAX, BOUNDS_IM_MIN, BOUNDS_IM_MAX, MAX_ITERATIONS);
        }
        
        @Override
        protected void plot(int row, int col, double c_re, double c_im, int iterations, boolean inside) {
            work += iterations;
        }
        
        public double getWork() { return work; }
    }
}
