package alex;

import java.io.*;
import java.util.*;

import static alex.StationProperty.invalidValue;

public class NGSParser {
    private static final int AMOUNT_LINES = 7;
    private final InputStream in;
    private final OutputStream out;
    private final double humidity = 70.0;
    private final double pressure = 1010.0;

    private Map<String, Double> nearestStationTemperatureMap = new HashMap<>();
    private Map<String, Double> nearestStationDistanceMap = new HashMap<>();
    private Map<String, String> nearestStationKeyMap = new HashMap<>();

    NGSParser(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void fixNGSFile(Map<String, List<Double>> stationDictionary, Map<String, Map<String, StationProperty>> propertyMap) throws IOException {
        int counterEndMarker = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        String tmp = "";
        Map<String, List<Double>> mapOfStationCoord = getStationCoord(reader, writer); //odin "$END" zdes'

        while (counterEndMarker != 2 && (tmp = reader.readLine()) != null) {
            writer.write(tmp);
            writer.newLine();
            if (tmp.equals("$END")) {
                counterEndMarker++;
                System.out.println(counterEndMarker);
            }
        }

        fixIncorrectLine(reader, mapOfStationCoord, stationDictionary, propertyMap, writer);
        //System.out.println(tmp);
        /*    if (counterEndMarker == 3) {
                if (tmp.contains("-999.000") || tmp.contains("-99900.000")) { //regex
                    System.out.println(lineNum);
                    System.out.println(tmp);
                }
            }
            lineNum++;
        }*/
        writer.flush();
        writer.close();
        reader.close();
    }

    //private fixLine()
    private void fixIncorrectLine(BufferedReader reader, Map<String, List<Double>> mapOfStationCoord, Map<String, List<Double>> stationDictionary, Map<String, Map<String, StationProperty>> propertyMap, BufferedWriter writer) throws IOException {
        int counterLines = 0;

        String lineWithStationName = "";
        String tmp = "";
        //for (int i = 0; i < 5; i++) {
        //lineWithStationName = reader.readLine();
        nearestStationTemperatureMap.clear();
        nearestStationDistanceMap.clear();
        nearestStationKeyMap.clear();

        //for (int i = 0; i < 10; i++) {
          //  lineWithStationName = reader.readLine();
        while ((lineWithStationName = reader.readLine()) != null) {
            writer.write(lineWithStationName);
            writer.newLine();
            System.out.println(lineWithStationName);
            while (counterLines < AMOUNT_LINES && (tmp = reader.readLine()) != null
                    && !((tmp.contains("-999.000") || tmp.contains("-99900.000")))) {
                writer.write(tmp);
                writer.newLine();
                counterLines++;
            }

            if (counterLines == AMOUNT_LINES) {
                counterLines = 0;
                continue;
            }
            String fix = fixLine(tmp, lineWithStationName, mapOfStationCoord, stationDictionary, propertyMap);
            writer.write(fix);
            writer.newLine();

            System.out.println(tmp + "!!!!");
            writer.write(reader.readLine());
            writer.newLine();
            writer.write(reader.readLine());
            writer.newLine();
            counterLines = 0;
        }
    }

    private String fixLine(String incorrectLine, String lineWithStationName, Map<String, List<Double>> mapOfStationCoord, Map<String, List<Double>> stationDictionary, Map<String, Map<String, StationProperty>> propertyMap) {
        if (!(incorrectLine.contains("-999.000") || incorrectLine.contains("-99900.000"))) {
            return incorrectLine;
        }

        String dateOfCrash = getDateOfCrash(lineWithStationName);
        System.out.println(dateOfCrash);

        int type = getTypeError(incorrectLine);
        //мб 1 станция или 2, в зависимости от ошибки
        List<String> nameStation = getNameStation(lineWithStationName, type);

        for (String name : nameStation) {
            String theNearestStation = getTheNearestStationInThatDay(name, mapOfStationCoord, dateOfCrash, stationDictionary, propertyMap);
            System.out.println(theNearestStation + " " + stationDictionary.get(theNearestStation.split("\s+")[0]));
        }


        System.out.println("?????????");

        String tail = incorrectLine.substring(10 * 6);
        StringBuilder incLine = new StringBuilder(incorrectLine);
        incLine = new StringBuilder(incorrectLine.substring(0, incorrectLine.indexOf("-99900.000")) + " -99900.000");
        String tmpTail = incorrectLine.substring(incorrectLine.indexOf("-99900.000") + "-99900.000".length());
        if (!tmpTail.contains("-99900.000")) {
            incLine = new StringBuilder(incLine + tmpTail);
        } else {
            incLine = new StringBuilder(incLine + tmpTail.substring(0, tmpTail.indexOf("-99900.000")) + " "
                    + tmpTail.substring(tmpTail.indexOf("-99900.000")));
        }

        incorrectLine = String.valueOf(incLine);

        System.out.println("INCORRECT LINE   " + incorrectLine);
        String[] values = incorrectLine.trim().split("\s+");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            if ((i < 2) && (values[i].equals("-999.000"))) {
                if (nameStation.size() == 1) {
                    values[i] = String.format(Locale.ROOT, "%.3f",nearestStationTemperatureMap.get(nameStation.get(0)));
                } else {
                    values[i] = String.format(Locale.ROOT,"%.3f", nearestStationTemperatureMap.get(nameStation.get(i % 2)));
                }
            } else if (values[i].equals("-999.000")) {
                values[i] = String.format(Locale.ROOT, "%.3f", pressure);
            } else if (values[i].equals("-99900.000")) {
                values[i] = String.format(Locale.ROOT,"%.3f", humidity);
            }
            builder.append(spaceLine(10 - values[i].length()));
            builder.append(values[i]);
        }

        //10 size of column element ex. ___-999.00
        //6 amount of columns
        return String.valueOf(builder.append(tail));
    }

    private String getTheNearestStationInThatDay(String name, Map<String, List<Double>> mapOfStationCoord, String dateOfCrash, Map<String, List<Double>> stationDictionary, Map<String, Map<String, StationProperty>> propertyMap) {
        List<Double> stationCoords = mapOfStationCoord.get(name);
        System.out.println(name + " " + stationCoords);

        String theNearestStation = null;
        double distance = Double.MAX_VALUE;
        double resultT = invalidValue;
        for (Map.Entry<String, List<Double>> meteoStation : stationDictionary.entrySet()) {
            if (nearestStationKeyMap.containsKey(name)) {
                return nearestStationKeyMap.get(name) + "   " + nearestStationTemperatureMap.get(name) + " " + nearestStationDistanceMap.get(name);
            }
            double tmpDistance = getDistance(stationCoords, meteoStation.getValue());

            Map<String, StationProperty> stationPropertyMap = propertyMap.get(dateOfCrash);
            StationProperty stationProperty = stationPropertyMap.get(meteoStation.getKey());
            if (stationPropertyMap.containsKey(meteoStation.getKey()) && (stationProperty.getTAVG() != invalidValue)) {
                if (tmpDistance < distance) {
                    theNearestStation = meteoStation.getKey();
                    distance = tmpDistance;
                    resultT = stationProperty.getTAVG() / 10.0;
                }
            } else {
                if (stationPropertyMap.containsKey(meteoStation.getKey()) &&
                        stationProperty.getTMIN() != invalidValue && stationProperty.getTMAX() != invalidValue) {
                    if (tmpDistance < distance) {
                        theNearestStation = meteoStation.getKey();
                        distance = tmpDistance;
                        resultT = (stationProperty.getTMIN() / 10.0 + stationProperty.getTMAX() / 10.0) / 2.0;
                    }
                }
            }
        }

        System.out.println("------------");
        nearestStationTemperatureMap.put(name, resultT);
        nearestStationDistanceMap.put(name, distance);
        nearestStationKeyMap.put(name, theNearestStation);
        return theNearestStation + "   " + resultT + " " + distance;

    }

    private Map<String, StationProperty> getStationPropertyMap() {
        return null;
    }

    private Double getDistance(List<Double> stationCoords, List<Double> meteoStationCoords) {
        double deltaLat = Math.toRadians(meteoStationCoords.get(0) - stationCoords.get(0));
        double deltaLon = Math.toRadians(meteoStationCoords.get(1) - stationCoords.get(1));

        double angle = Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
                * Math.cos(Math.toRadians(stationCoords.get(0))) * Math.cos((Math.toRadians(meteoStationCoords.get(0))));

        return 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1 - angle));
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

    private Map<String, List<Double>> getStationCoord(BufferedReader reader, BufferedWriter writer) throws IOException {
        writer.write(reader.readLine());
        writer.newLine();
        writer.write(reader.readLine());
        writer.newLine();
        //Now third stroka
        String tmp = "";
        Coords coords = new Coords();
        Map<String, List<Double>> mapStationCoord = new HashMap<>();
        while (((tmp = reader.readLine()) != null) && (!tmp.equals("$END"))) {
            writer.write(tmp);
            writer.newLine();
            String[] stCoord = tmp.split("\s+");
            List<Double> listCoord = new ArrayList<>();
            listCoord.add(coords.getLat(Double.parseDouble(stCoord[1]), Double.parseDouble(stCoord[2]), Double.parseDouble(stCoord[3])));
            listCoord.add(coords.getLon(Double.parseDouble(stCoord[1]), Double.parseDouble(stCoord[2]), Double.parseDouble(stCoord[3])));
            mapStationCoord.put(stCoord[0], listCoord);
        }
        writer.write("$END");
        writer.newLine();
        System.out.println(mapStationCoord);
        return mapStationCoord;
    }

    private String getDateOfCrash(String lineWithStationName) {
        return lineWithStationName.split("\s+")[3] + lineWithStationName.split("\s+")[4] + lineWithStationName.split("\s+")[5];
    }

    public Map<String, Map<String, StationProperty>> getStationPropertyMap(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Map<String, Map<String, StationProperty>> result = new HashMap<>();
        String tmp = "";

        Map<String, StationProperty> tmpPropertyMap = new HashMap<>();

        String t = "";
        while ((tmp = reader.readLine()) != null) {
            String[] properties = tmp.split(",");
            if (!t.equals(properties[1])) {
                t = properties[1];
                tmpPropertyMap = new HashMap<>();
            }
            if (!result.containsKey(properties[1])) {
                StationProperty stationProperty = new StationProperty(properties[2], properties[3]);
                tmpPropertyMap.put(properties[0], stationProperty);
            } else {
                if (!result.get(properties[1]).containsKey(properties[0])) {
                    StationProperty stationProperty = new StationProperty();
                    setStationProperty(stationProperty, properties[2], properties[3]);
                    tmpPropertyMap.put(properties[0], stationProperty);
                } else {
                    setStationProperty(result.get(properties[1]).get(properties[0]), properties[2], properties[3]);
                    tmpPropertyMap.put(properties[0], result.get(properties[1]).get(properties[0]));
                }
            }
            result.put(properties[1], tmpPropertyMap);
        }

        return result;
    }

    private void setStationProperty(StationProperty stationProperty, String property, String value) {
        switch (property) {
            case "TMIN" -> stationProperty.setTMIN(Double.parseDouble(value));
            case "TMAX" -> stationProperty.setTMAX(Double.parseDouble(value));
            case "TAVG" -> stationProperty.setTAVG(Double.parseDouble(value));
        }
    }

    private String spaceLine(int k) {
        StringBuilder builder = new StringBuilder();
        return String.valueOf(builder.append(" ".repeat(Math.max(0, k))));
    }
}
