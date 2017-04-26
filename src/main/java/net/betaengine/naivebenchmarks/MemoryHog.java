package net.betaengine.naivebenchmarks;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

import com.google.common.base.Verify;

public class MemoryHog {
    // https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtils.html#ONE_MB
    public final static long ONE_MB = 1024 * 1024;

    // Find the last x in the range [lo, hi] for which p(x) is false.
    // https://www.topcoder.com/community/data-science/data-science-tutorials/binary-search/
    private static long binarySearch(long lo, long hi, Predicate<Long> p) {
        while (lo < hi) {
            long mid = lo + (hi - lo + 1) / 2;
            if (p.test(mid)) {
                hi = mid - 1;
            } else {
                lo = mid;
            }
        }
        
        if (p.test(lo)) {
            throw new NoSuchElementException("predicate is false for no element");
        }
        
        return lo;
    }
    
    private static long[] allocate(long bytes) {
        long len = bytes / Long.BYTES;
        
        // http://stackoverflow.com/a/8381338/245602
        Verify.verify(len < (Integer.MAX_VALUE - 8));
        
        return new long[(int)len];
    }

    public static long[] hog(long maxMemory, long headroom) {
        Predicate<Long> p = l -> {
            try {
                allocate(l * ONE_MB);
                return true;
            } catch (OutOfMemoryError e) {
                return false;
            }
        };
        
        // Allocation attempts are surprisingly slow so binary search is essential (along with 1MiB minimum block size). 
        long actualFreeMemory = binarySearch(0, (maxMemory / ONE_MB), l -> !p.test(l)) * ONE_MB;
        
        long[] result = allocate(actualFreeMemory - headroom);
        
        XorShift64.randomFill(result);
        
        return result;
    }
}
