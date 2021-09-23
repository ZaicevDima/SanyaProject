package alex;

public class Coords {
    public double getLat(double x, double y, double z) {
        double R = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return Math.asin(z / R) / Math.PI * 180;
    }

    public double getLon(double x, double y, double z) {
        double R = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return Math.atan2(y, x) / Math.PI * 180;
    }
}
