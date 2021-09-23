package alex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class StationDictionary {
    private final InputStream in;

    StationDictionary(InputStream in) {
        this.in = in;
    }

    public Map<String, List<Double>> getStationDictionary() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Map<String, List<Double>> stationDictionary = new HashMap<>();
        String tmp;
        while ((tmp = reader.readLine()) != null){
            String[] splittingLine = tmp.split("\s+");
            stationDictionary.put(splittingLine[0], new ArrayList<>(Arrays.asList(
                    Double.parseDouble(splittingLine[1]),
                    Double.parseDouble(splittingLine[2]))));
        }
        reader.close();

        return stationDictionary;
    }
}
