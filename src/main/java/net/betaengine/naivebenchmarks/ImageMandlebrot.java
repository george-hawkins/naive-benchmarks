package net.betaengine.naivebenchmarks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageMandlebrot extends AbstractMandelbrot {
    private final static int BLACK = 0x000000;
    private final static int WHITE = 0xFFFFFF;
    
    private final static int WIDTH = 1920;
    private final static int MAX_ITERATIONS = 100;
    
    private final BufferedImage image;
    
    private ImageMandlebrot() {
        super(WIDTH, BOUNDS_REAL_MIN, BOUNDS_REAL_MAX, BOUNDS_IM_MIN, BOUNDS_IM_MAX, MAX_ITERATIONS);
        
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
    
    public static void main(String[] args) {
        ImageMandlebrot mandlebrot = new ImageMandlebrot();
        
        mandlebrot.calculate();
    }
}