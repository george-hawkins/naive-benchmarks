package net.betaengine.naivebenchmarks;

import com.google.common.base.Preconditions;

// How max iterations affects apparent bounds:
//
// max = 8
// real min = -1.9999
// real max = 0.4735
// i max = 1.1231
//
// max = 51
// real min = -1.9999
// real max = 0.4707
// i max = 1.1009 
//    
// max = 52
// real min = -1.9999
// real max = 0.4694
// i max = 1.1009
//
// max = 1000
// real min = -1.9999
// real max = 0.4617
// i max = 1.1008
//
public abstract class AbstractMandelbrot {
    // Fairly obvious facts about the Mandelbrot set:
    // * All points within the set must fall within a circle of radius 2 centered on the origin.
    // * It's symmetric about the real axis.
    // So if we ignore negative imaginary values we can work out (by experimentation) that if you do at
    // least ITERATIONS_MIN iterations it is bounded by these values (see max and apparent bounds above):
    public static double BOUNDS_REAL_MIN = -2.00;
    public static double BOUNDS_REAL_MAX = 0.47;
    public static double BOUNDS_IM_MIN = 0.00;
    public static double BOUNDS_IM_MAX = 1.11;
    // If you do less than ITERATIONS_MIN you'll end up with points outside the above bounds.
    public static int ITERATIONS_MIN = 52;
    
    private final int width;
    private final int height;
    private final double realMin;
    private final double realMax;
    private final double imMin;
    private final double imMax;
    private final int maxIterations;
    
    protected AbstractMandelbrot(int width, double realMin, double realMax, double imMin, double imMax, int maxIterations) {
        Preconditions.checkArgument(maxIterations >= ITERATIONS_MIN);
        
        this.width = width;
        this.realMin = realMin;
        this.realMax = realMax;
        this.imMin = imMin;
        this.imMax = imMax;
        this.maxIterations = maxIterations;
        
        height = (int)((imMax - imMin) * width / (realMax - realMin));
    }
    
    protected int getHeight() { return height; }

    // See https://github.com/joni/fractals/blob/master/mandelbrot/MandelbrotBW.java
    protected void calculate() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double c_re = realMin + (realMax - realMin) * col / width;
                double c_im = imMin + (imMax - imMin) * row / height;
                double x = 0;
                double y = 0;
                int iterations = 0;
                while ((x * x + y * y) < 4 && iterations++ <= maxIterations) {
                    double x_new = x * x - y * y + c_re;
                    
                    y = 2 * x * y + c_im;
                    x = x_new;
                }
                
                plot(row, col, c_re, c_im, iterations, iterations > maxIterations);
            }
        }
        
        finished();
    }
    
    protected abstract void plot(int row, int col, double c_re, double c_im, int iterations, boolean inside);
    
    protected void finished() { }
}