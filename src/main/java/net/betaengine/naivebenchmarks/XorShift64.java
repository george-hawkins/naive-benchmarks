package net.betaengine.naivebenchmarks;

// Fast and pretty good PRNG - https://en.wikipedia.org/wiki/Xorshift
public class XorShift64 {
    private long x64 = System.nanoTime();
    
    public long next() {
      x64 ^= x64 << 13;
      x64 ^= x64 >> 7;
      x64 ^= x64 << 17;
      
      return x64;
    }
}
