public class StationProperty {
    private String stationKey;
    private double TMIN;
    private double TMAX;
    private double TAVG;

    StationProperty(String stationKey) {
        this.stationKey = stationKey;
    }

    StationProperty(String stationKey, double TMIN, double TMAX, double TAVG) {
        this.stationKey = stationKey;
        this.TMAX = TMAX;
        this.TMIN = TMIN;
        this.TAVG = TAVG;
    }

    public String getStationKey() {
        return stationKey;
    }

    public void setStationKey(String stationKey) {
        this.stationKey = stationKey;
    }

    public double getTMIN() {
        return TMIN;
    }

    public void setTMIN(double TMIN) {
        this.TMIN = TMIN;
    }

    public double getTMAX() {
        return TMAX;
    }

    public void setTMAX(double TMAX) {
        this.TMAX = TMAX;
    }

    public double getTAVG() {
        return TAVG;
    }

    public void setTAVG(double TAVG) {
        this.TAVG = TAVG;
    }
}
