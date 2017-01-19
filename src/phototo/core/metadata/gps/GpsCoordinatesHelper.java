package phototo.core.metadata.gps;

import phototo.helpers.Tuple;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GpsCoordinatesHelper {

    private static final Pattern parsingPattern = Pattern.compile("([0-9\\.]+) ([NS]), ([0-9\\.]+) ([EW])");

    public static Tuple<Double, Double> getCoordinates(String coordinates) {
        if (coordinates == null || coordinates.trim().isEmpty()) {
            return new Tuple<>(null, null);
        } else {
            Matcher m = parsingPattern.matcher(coordinates);

            if (m.find()) {
                double latitude = Double.parseDouble(m.group(1));
                double longitude = Double.parseDouble(m.group(3));

                if (m.group(2).equals("S")) {
                    latitude *= -1;
                }
                if (m.group(4).equals("W")) {
                    longitude *= -1;
                }

                return new Tuple<>(latitude, longitude);
            } else {
                throw new IllegalArgumentException("Argument \"" + coordinates + "\" does not have the expected format");
            }
        }
    }
}
