package net.betaengine.naivebenchmarks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.common.base.Verify;

// See https://github.com/joni/fractals/blob/master/mandelbrot/MandelbrotBW.java
public class Mandelbrot {
    public static void main(String[] args) throws Exception {
        bounds();
    }
    
    private static void draw() throws IOException {
        int width = 1920, height = 1080, max = 8;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int black = 0x000000, white = 0xFFFFFF;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double c_re = (col - width/2)*4.0/width;
                double c_im = (row - height/2)*4.0/width;
                double x = 0, y = 0;
                int iterations = 0;
                while (x*x+y*y < 4 && iterations < max) {
                    double x_new = x*x-y*y+c_re;
                    y = 2*x*y+c_im;
                    x = x_new;
                    iterations++;
                } 
                if (iterations < max) image.setRGB(col, row, white);
                else image.setRGB(col, row, black);
            }
        }

        ImageIO.write(image, "png", new File("mandelbrot.png"));
    }

// 2, 0.47, 1.11
// max = 8
// real min = -1.9999
// real max = 0.4757
// i max = 1.1236
// max = 50
// real min = -1.9999
// real max = 0.4707
 // i max = 1.1144

 // REAL_MIN, REAL_MAX, IM_MIN=0, IM_MAX
// max = 52
// real min = -1.9999
// real max = 0.4707
// i max = 1.1009 
// max = 53
// real min = -1.9999
// real max = 0.4694
// i max = 1.1009

// max = 100
// real min = -1.9999
// real max = 0.4628
// i max = 1.1009
// max = 1000
// real min = -1.9999
// real max = 0.4617
// i max = 1.1008
    
    // Fairly obvious facts about the Mandelbrot set:
    // * All points within the set must fall within a circle of radius 2 centered on the origin.
    // * It's symmetric about the real axis.
    // So if we ignore negative imaginary values we can work out (by experimentation) that if
    // you do at least ITERATIONS_MIN iterations it is bounded by this rectangle defined by:
    private static double REAL_MIN = -2;
    private static double REAL_MAX = 0.47;
    private static double IM_MIN = 0;
    private static double IM_MAX = 1.11;
    // If you do less than ITERATIONS_MIN you'll end up with points outside the above bounds.
    private static int ITERATIONS_MIN = 53;
    
    private static void bounds() {
        int side = 40000;
        int max = 53;
        double cRealMin = Double.MAX_VALUE;
        double cRealMax = Double.MIN_VALUE;
        double cImaginaryMax = Double.MIN_VALUE;
        
        Verify.verify(max >= ITERATIONS_MIN);

        for (int row = 0; row < side; row++) {
            for (int col = 0; col < side; col++) {
                double c_re = (col - side/2)*4.0/side;
                double c_im = (row - side/2)*4.0/side;
                double x = 0, y = 0;
                int iterations = 0;
                while (x*x+y*y < 4 && iterations < max) {
                    double x_new = x*x-y*y+c_re;
                    y = 2*x*y+c_im;
                    x = x_new;
                    iterations++;
                }
                
                if (iterations >= max) {
                    cRealMin = Math.min(cRealMin, c_re);
                    cRealMax = Math.max(cRealMax, c_re);
                    cImaginaryMax = Math.max(cImaginaryMax, c_im);
                }
            }
        }

        System.out.println("real min = " + cRealMin);
        System.out.println("real max = " + cRealMax);
        System.out.println("i max = " + cImaginaryMax);
    }
}
