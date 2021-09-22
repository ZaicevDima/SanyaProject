import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class NGSParser {
    private static final int AMOUNT_LINES = 7;
    private final InputStream in;

    NGSParser(InputStream in) {
        this.in = in;
    }

    public void fixNGSFile(Map<String, List<Double>> stationDictionary) throws IOException {
        int counterEndMarker = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String tmp = "";
        Map<String, List<Double>> mapOfStationCoord = getStationCoord(reader); //odin "$END" zdes'

        while (counterEndMarker != 2 && (tmp = reader.readLine()) != null) {
            if (tmp.equals("$END")) {
                counterEndMarker++;
                System.out.println(counterEndMarker);
            }
        }

        fixIncorrectLine(reader, mapOfStationCoord, stationDictionary);
        //System.out.println(tmp);
        /*    if (counterEndMarker == 3) {
                if (tmp.contains("-999.000") || tmp.contains("-99900.000")) { //regex
                    System.out.println(lineNum);
                    System.out.println(tmp);
                }
            }
            lineNum++;
        }*/
        reader.close();
    }

    //private fixLine()
    private void fixIncorrectLine(BufferedReader reader, Map<String, List<Double>> mapOfStationCoord, Map<String, List<Double>> stationDictionary) throws IOException {
        int counterLines = 0;

        String lineWithStationName = "";
        String tmp = "";
        //for (int i = 0; i < 5; i++) {
        //lineWithStationName = reader.readLine();
        while ((lineWithStationName = reader.readLine()) != null) {
            System.out.println(lineWithStationName);
            while (counterLines < AMOUNT_LINES && (tmp = reader.readLine()) != null
                    && !((tmp.contains("-999.000") || tmp.contains("-99900.000")))) {
                counterLines++;
                //System.out.println(!((tmp.contains("-999.000") || tmp.contains("-99900.000"))));
            }

            //System.out.println(tmp);
            String fix = fixLine(tmp, lineWithStationName, mapOfStationCoord, stationDictionary);
            if (counterLines == AMOUNT_LINES) {
                counterLines = 0;
                continue;
            }
            System.out.println(tmp + "!!!!");
            reader.readLine();
            reader.readLine();
            counterLines = 0;
        }

    }

    private String fixLine(String incorrectLine, String lineWithStationName, Map<String, List<Double>> mapOfStationCoord, Map<String, List<Double>> stationDictionary) {
        if (!(incorrectLine.contains("-999.000") || incorrectLine.contains("-99900.000"))) {
            return null;
        }

        String dateOfCrash = getDateOfCrash(lineWithStationName);
        System.out.println(dateOfCrash);

        int type = getTypeError(incorrectLine);
        List<String> nameStation = getNameStation(lineWithStationName, type);
        for (String s : nameStation) {
            List<Double> stationCoords = mapOfStationCoord.get(s);
            System.out.println(s + " " + stationCoords);
            String theNearestStation = getTheNearestStationInThatDay(stationCoords, dateOfCrash, stationDictionary);
            System.out.println(theNearestStation + " " + stationDictionary.get(theNearestStation));
        }

        System.out.println("?????????");


        return "nameStation";
    }

    private String getTheNearestStationInThatDay(List<Double> stationCoords, String dateOfCrash, Map<String, List<Double>> stationDictionary) {

        String theNearestStation = null;
        Double distance = 99999999999999999.0;
        Map<String, StationProperty> stationPropertyMap = getStationPropertyMap();

        for (Map.Entry<String, List<Double>> meteoStation : stationDictionary.entrySet()) {
            Double tmpDistnce = getDistance(stationCoords, meteoStation.getValue());
            if (tmpDistnce < distance) {
                theNearestStation = meteoStation.getKey();
                distance = tmpDistnce;
            }
        }
        return theNearestStation;
    }

    private Map<String, StationProperty> getStationPropertyMap() {
        return null;
    }

    private Double getDistance(List<Double> stationCoords, List<Double> meteoStationCoords) {
        double deltaLat = Math.toRadians(meteoStationCoords.get(0) - stationCoords.get(0));
        double deltaLon = Math.toRadians(meteoStationCoords.get(1) - stationCoords.get(1));

        double angle = Math.pow(Math.sin(deltaLat/2), 2) + Math.pow(Math.sin(deltaLon/2) , 2)
                * Math.cos(Math.toRadians(stationCoords.get(0))) * Math.cos((Math.toRadians(meteoStationCoords.get(0))));

        return 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1-angle));
    }

    private int getTypeError(String incorrectLine) {
        boolean isFirst = false;
        boolean isSecond = false;
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < incorrectLine.length(); i++) {
            if (incorrectLine.charAt(i) == '-') {
                line.append(' ');
            }
            line.append(incorrectLine.charAt(i));
        }

        String[] properties = line.toString().split("\s+");
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].equals("-999.000") || properties[i].equals("-99900.000")) {
                if (i % 2 == 0) {
                    isSecond = true;
                } else {
                    isFirst = true;
                }
            }
        }

        if (isFirst && isSecond) {
            return 3;
        } else if (isFirst) {
            return 1;
        } else {
            return 2;
        }
    }

    private List<String> getNameStation(String lineWithStationName, int type) {
        if (type == 1) {
            return Collections.singletonList(lineWithStationName.split("\s+")[0]);
        } else if (type == 2) {
            return Collections.singletonList(lineWithStationName.split("\s+")[1]);
        } else {
            return new ArrayList<>(Arrays.asList(lineWithStationName.split("\s+")[0], lineWithStationName.split("\s+")[1]));
        }
    }

    private Map<String, List<Double>> getStationCoord(BufferedReader reader) throws IOException {
        reader.readLine();
        reader.readLine();
        //Now third stroka
        String tmp = "";
        Coords coords = new Coords();
        Map<String, List<Double>> mapStationCoord = new HashMap<>();
        while (((tmp = reader.readLine()) != null) && (!tmp.equals("$END"))) {
            String[] stCoord = tmp.split("\s+");
            List<Double> listCoord = new ArrayList<>();
            listCoord.add(coords.getLat(Double.parseDouble(stCoord[1]), Double.parseDouble(stCoord[2]), Double.parseDouble(stCoord[3])));
            listCoord.add(coords.getLon(Double.parseDouble(stCoord[1]), Double.parseDouble(stCoord[2]), Double.parseDouble(stCoord[3])));
            mapStationCoord.put(stCoord[0], listCoord);
        }
        System.out.println(mapStationCoord);
        return mapStationCoord;
    }

    private String getDateOfCrash(String lineWithStationName) {
        String dateOfCrash = lineWithStationName.split("\s+")[3] + lineWithStationName.split("\s+")[4] + lineWithStationName.split("\s+")[5];
        return dateOfCrash;
    }

    public Map<String, List<StationProperty>> getStationPropertyMap(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Map<String, List<StationProperty>> result = new HashMap<>();
        String tmp = "";
        while ((tmp = reader.readLine()) != null) {
            String[] properties = tmp.split(",");
            if (!result.containsKey(properties[1])) {
                List<StationProperty> tmpList = new ArrayList<>();
                StationProperty stationProperty = new StationProperty(properties[0]);
                setStationProperty(stationProperty, properties[3], properties[4]);
                tmpList.add(stationProperty);
                result.put(properties[1], tmpList);
            } else {
                List<StationProperty> tmpList = result.get(properties[1]);
                //boolean isContains = false;
                StationProperty currentStationProperty = null;
                //java stream api find
                for (StationProperty stationProperty : tmpList) {
                    if (stationProperty.getStationKey().equals(properties[0])) {
                        //isContains = true;
                        currentStationProperty = stationProperty;
                        break;
                    }
                }

                if (currentStationProperty == null) {
                    StationProperty stationProperty = new StationProperty(properties[0]);
                    setStationProperty(stationProperty, properties[3], properties[4]);
                    tmpList.add(stationProperty);
                    result.put(properties[1], tmpList);
                } else {
                    setStationProperty(currentStationProperty,  properties[3], properties[4]);
                }
            }
        }

        System.out.println(result);

        return  result;
    }

    private void setStationProperty(StationProperty stationProperty, String property, String value) {
        switch (property) {
            case "TMIN" -> stationProperty.setTMIN(Double.parseDouble(value));
            case "TMAX" -> stationProperty.setTMAX(Double.parseDouble(value));
            case "TAVG" -> stationProperty.setTAVG(Double.parseDouble(value));
        }
    }
}
