package net.betaengine.naivebenchmarks;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

// Fast and pretty good PRNG - https://en.wikipedia.org/wiki/Xorshift
public class XorShift64 {
    private long x64 = System.nanoTime();
    
    public long next() {
      x64 ^= x64 << 13;
      x64 ^= x64 >> 7;
      x64 ^= x64 << 17;
      
      return x64;
    }
    
    private static void randomFill(LongBuffer longBuffer) {
        XorShift64 rand = new XorShift64();

        while (longBuffer.hasRemaining()) {
            longBuffer.put(rand.next());
        }
    }
    
    public static void randomFill(long[] buffer) {
        randomFill(LongBuffer.wrap(buffer));
    }
    
    // Fill memory with random values - useful to force the system to really allocate rather than just promise memory.
    public static void randomFill(byte[] buffer) {
        randomFill(ByteBuffer.wrap(buffer).asLongBuffer());
    }
}
