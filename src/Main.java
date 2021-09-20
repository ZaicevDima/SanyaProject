import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream stationInput = Files.newInputStream(Path.of("resources\\ghcnd-stations.txt"));
        InputStream in = Files.newInputStream(Path.of("resources\\090112XA.ngs"));

        //StationDictionary dictionary = new StationDictionary(stationInput);
        //Map<String, List<Double>> stationDictionary = dictionary.getStationDictionary();
        //stationInput.close();
        //System.out.println(stationDictionary.size());

        NGSParser parser = new NGSParser(in);
        parser.fixNGSFile();
        in.close();
    }

}
