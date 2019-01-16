package xyz.kripthor.nfcexfil;

/**
 * Created by kripthor on 22-11-2017.
 */

public class DataPoint {
    public long arrived;
    public int bit;

    public DataPoint() {

    }

    public DataPoint(int bit) {
        this.arrived = System.currentTimeMillis();
        this.bit = bit;
    }

    public DataPoint(long arrived, int bit) {
        this.arrived = arrived;
        this.bit = bit;
    }

    @Override
    public String toString() {
        return (this.arrived % 10000)+ " : "+this.bit;
    }
}
