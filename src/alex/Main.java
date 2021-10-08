package alex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
//            long start = System.currentTimeMillis();
            InputStream propertyInput = Files.newInputStream((Path.of("resources\\"+args[0]+"_csv")));
            InputStream in = Files.newInputStream(Path.of(args[1]));
            OutputStream out = Files.newOutputStream(Path.of(args[2]));

            InputStream stationInput = Files.newInputStream(Path.of("resources\\ghcnd-stations.txt"));
            StationDictionary dictionary = new StationDictionary(stationInput);
            Map<String, List<Double>> stationDictionary = dictionary.getStationDictionary();
            stationInput.close();
            //System.out.println(stationDictionary.size());


            NGSParser parser = new NGSParser(in, out);
            Map<String, Map<String, StationProperty>> propertyMap = parser.getStationPropertyMap(propertyInput);
            parser.fixNGSFile(stationDictionary, propertyMap);
            propertyInput.close();

            in.close();
            out.close();
//            long finish = System.currentTimeMillis();
//            long elapsed = finish - start;
//            System.out.println((elapsed / (1000.0)));
        } else {
            System.out.println("Usage : NGSfix <year> <input> <output>");
        }
    }

}
