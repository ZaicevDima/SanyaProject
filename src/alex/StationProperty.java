package alex;

public class StationProperty {
    public static final double invalidValue = -2750.0;
    private double TMIN = invalidValue;
    private double TMAX = invalidValue;
    private double TAVG = invalidValue;

    StationProperty() {
    }
    StationProperty(String property, String value) {
        switch (property) {
            case "TMIN" -> this.TMIN = Double.parseDouble(value);
            case "TMAX" -> this.TMAX = Double.parseDouble(value);
            case "TAVG" -> this.TAVG = Double.parseDouble(value);
        }
    }

    static StationProperty createStationPropertyByTMIN(double TMIN) {
        StationProperty property = new StationProperty();
        property.setTMIN(TMIN);

        return property;
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

    public String toString() {
        return "[TMIN = " + TMIN + "; TMAX = " + TMAX + "; TAVG = " + TAVG + "]";
    }
}
