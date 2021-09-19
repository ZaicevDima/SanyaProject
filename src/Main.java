import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream in = Files.newInputStream(Path.of("resources\\ghcnd-stations.txt"));
        StationDictionary dictionary = new StationDictionary(in);
        Map<String, List<Double>> stationDictionary = dictionary.getStationDictionary();
        in.close();
        System.out.println(stationDictionary.size());
    }

}
