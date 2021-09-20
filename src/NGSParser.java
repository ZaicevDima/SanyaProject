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

    public void fixNGSFile() throws IOException {
        int counterEndMarker = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String tmp = "";
        int lineNum = 1;
        while (counterEndMarker != 3 && (tmp = reader.readLine()) != null) {
            if (tmp.equals("$END")) {
                counterEndMarker++;
                System.out.println(counterEndMarker);
            }
        }

        fixIncorrectLine(reader);
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
    private void fixIncorrectLine(BufferedReader reader) throws IOException {
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
            String fix = fixLine(tmp, lineWithStationName);
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

    private String fixLine(String incorrectLine, String lineWithStationName) {
        if (!(incorrectLine.contains("-999.000") || incorrectLine.contains("-99900.000"))) {
            return null;
        }

        int type = getTypeError(incorrectLine);
        List<String> nameStation = getNameStation(lineWithStationName, type);
        for (String s : nameStation) {
            System.out.println(s);
        }

        System.out.println("?????????");

        return "nameStation";
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
}
