package net.betaengine.naivebenchmarks;

public class HumanReadable {
    // http://stackoverflow.com/a/3758880/245602
    public static String toString(long count, boolean si) {
        int unit = si ? 1000 : 1024;
        if (count < unit) return count + " ";
        int exp = (int) (Math.log(count) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %s", count / Math.pow(unit, exp), pre);
    }
}
