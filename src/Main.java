import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream stationInput = Files.newInputStream(Path.of("resources\\ghcnd-stations.txt"));
        InputStream in = Files.newInputStream(Path.of("resources\\090112XA.ngs"));
        InputStream propertyInput = Files.newInputStream((Path.of("resources\\2009_csv")));

        StationDictionary dictionary = new StationDictionary(stationInput);
        Map<String, List<Double>> stationDictionary = dictionary.getStationDictionary();
        stationInput.close();
        //System.out.println(stationDictionary.size());


        NGSParser parser = new NGSParser(in);
        Map<String, Map<String, StationProperty>> propertyMap = parser.getStationPropertyMap(propertyInput);
        parser.fixNGSFile(stationDictionary, propertyMap);

        in.close();
    }

}
