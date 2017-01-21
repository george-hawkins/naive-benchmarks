package net.betaengine.naivebenchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public class Network extends AbstractBenchmark {
    private final static int WARMUP_LOOP = 8;
    private final static int WARMUP_COUNT = 1024;
    
    private final Logger logger = LoggerFactory.getLogger(Network.class);
    
    private final String hostname;
    private final int readPort;
    private final int writePort;
    
    public Network(int cycles, int len, String hostname, int readPort, int writePort) {
        super(cycles, len);
        
        this.hostname = hostname;
        this.readPort = readPort;
        this.writePort = writePort;
        
        logger.info("client will write to {} and read from {} on {}", writePort, readPort, hostname);
    }
    
    @Override
    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            LongBuffer longBuffer = ByteBuffer.wrap(buffer).asLongBuffer();
            XorShift64 rand = new XorShift64();

            while (longBuffer.hasRemaining()) {
                longBuffer.put(rand.next());
            }
            
            run((int)getLen(), buffer);
        } catch (IOException e) {
            fatal(e);
        }
    }
    
    private void run(int len, byte[] buffer) throws IOException {
        logger.info("warming up server");
        
        // If you exercise the server fully a few times with a light load the subsequent measurements come out fairly consistently.
        // If you don't then the read step can end up running shockingly slowly. 
        for (int j = 0; j < WARMUP_LOOP; j++) {
            try (Socket socket = new Socket(hostname, writePort)) {
                OutputStream output = socket.getOutputStream();
                
                for (int i = 0; i < WARMUP_COUNT; i++) {
                    write(output, buffer);
                }
            }
    
            try (Socket socket = new Socket(hostname, readPort)) {
                InputStream input = socket.getInputStream();
                
                for (int i = 0; i < WARMUP_COUNT; i++) {
                    read(input, buffer);
                }
            }
        }
        
        logger.info("completed server warmup");
        
        try (Socket socket = new Socket(hostname, writePort)) {
            OutputStream output = socket.getOutputStream();
            
            int medianMs = measure("network write", () -> {
                for (int i = 0; i < len; i++) {
                    write(output, buffer);
                }
            });
            
            // Network speed is in powers of 1000 rather than 1024.
            logger.info("network write speed is {}b", HumanReadable.toString(getBps(len, buffer.length, medianMs), true));
        }

        try (Socket socket = new Socket(hostname, readPort)) {
            InputStream input = socket.getInputStream();
            
            int medianMs = measure("network read", () -> {
                for (int i = 0; i < len; i++) {
                    read(input, buffer);
                }
            });
            
            // Network speed is in powers of 1000 rather than 1024.
            logger.info("network read speed is {}b", HumanReadable.toString(getBps(len, buffer.length, medianMs), true));
        }
    }
    
    private void write(OutputStream output, byte[] buffer) {
        try {
            output.write(buffer);
        } catch (IOException e) {
            fatal(e);
        }
    }
    
    private void read(InputStream input, byte[] buffer) {
        try {
            ByteStreams.readFully(input, buffer);
        } catch (IOException e) {
            fatal(e);
        }
    }
    
    private long getBps(long len, int bufferLen, int medianMs) {
        long bits = len * bufferLen * 8;
        
        return medianMs == 0 ? 0 : bits * 1000 / medianMs;
    }
}