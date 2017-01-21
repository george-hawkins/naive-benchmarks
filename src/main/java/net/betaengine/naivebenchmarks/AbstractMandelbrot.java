package net.betaengine.naivebenchmarks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.common.base.Verify;

public abstract class AbstractMandelbrot {
    public static void main(String[] args) {
        ImageMandlebrot mandlebrot = new ImageMandlebrot();
        
        mandlebrot.calculate();
    }
    
    private final int width;
    private final int height;
    private final double realMin;
    private final double realMax;
    private final double imMin;
    private final double imMax;
    
    protected AbstractMandelbrot(int width, double realMin, double realMax, double imMin, double imMax) {
        this.width = width;
        this.realMin = realMin;
        this.realMax = realMax;
        this.imMin = imMin;
        this.imMax = imMax;
        
        height = (int)((imMax - imMin) * width / (realMax - realMin));
    }
    
    protected int getHeight() { return height; }
    
    private static class ImageMandlebrot extends AbstractMandelbrot {
        private final static int BLACK = 0x000000;
        private final static int WHITE = 0xFFFFFF;
        
        private final static int WIDTH = 1920;
        
        private final BufferedImage image;
        
        public ImageMandlebrot() {
            super(WIDTH, -2, 2, -2, 2);
            
            image = new BufferedImage(WIDTH, getHeight(), BufferedImage.TYPE_INT_RGB);
        }
        
        @Override
        protected void plot(int row, int col, double c_re, double c_im, int iterations, boolean inside) {
            image.setRGB(col, row, inside ? BLACK : WHITE);
        }
        
        @Override
        protected void finished() {
            try {
                ImageIO.write(image, "png", new File("mandelbrot.png"));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    // How max affects apparent bounds:
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
    
    // Fairly obvious facts about the Mandelbrot set:
    // * All points within the set must fall within a circle of radius 2 centered on the origin.
    // * It's symmetric about the real axis.
    // So if we ignore negative imaginary values we can work out (by experimentation) that if you do at
    // least ITERATIONS_MIN iterations it is bounded by these values (see max and apparent bounds above):
    private static double REAL_MIN = -2.00;
    private static double REAL_MAX = 0.47;
    private static double IM_MIN = 0.00;
    private static double IM_MAX = 1.11;
    // If you do less than ITERATIONS_MIN you'll end up with points outside the above bounds.
    private static int ITERATIONS_MIN = 52;
    
    // See https://github.com/joni/fractals/blob/master/mandelbrot/MandelbrotBW.java
    protected void calculate() {
        int max = 1000;
        
        Verify.verify(max >= ITERATIONS_MIN);
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double c_re = realMin + (realMax - realMin) * col / width;
                double c_im = imMin + (imMax - imMin) * row / height;
                double x = 0;
                double y = 0;
                int iterations = 0;
                while ((x * x + y * y) < 4 && iterations++ <= max) {
                    double x_new = x * x - y * y + c_re;
                    
                    y = 2 * x * y + c_im;
                    x = x_new;
                }
                
                plot(row, col, c_re, c_im, iterations, iterations > max);
            }
        }
        
        finished();
    }
    
    protected abstract void plot(int row, int col, double c_re, double c_im, int iterations, boolean inside);
    
    protected abstract void finished();
}