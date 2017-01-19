package phototo.core.metadata.gps;

import phototo.helpers.FileHelper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GpsCoordinatesDescriptionCache {

    private final String cacheFileName;
    private final Map<String, String> map = new HashMap<>();

    public GpsCoordinatesDescriptionCache(String cacheFileName) {
        this.cacheFileName = cacheFileName;

        try {
            String[] lines = FileHelper.readFileLines(new File(cacheFileName));

            for (String line : lines) {
                String[] data = line.split(";", 2);
                map.put(data[0], data[1]);
            }
        } catch (IOException ex) {
        }
    }

    public synchronized String getFromCache(double latitude, double longitude) {
        return map.get(getKey(latitude, longitude));
    }

    public synchronized void addToCache(double latitude, double longitude, String data) {
        String previous = map.put(getKey(latitude, longitude), data);

        try {
            if (previous == null) {
                FileHelper.appendFileLine(new File(this.cacheFileName), getKey(latitude, longitude) + ";" + data);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getKey(double latitude, double longitude) {
        return latitude + "," + longitude;
    }
}
