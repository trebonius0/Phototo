package photato.core.metadata.gps;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class OSMGpsCoordinatesDescriptionGetter implements IGpsCoordinatesDescriptionGetter {

    private final HttpClient httpClient;
    private final GpsCoordinatesDescriptionCache cache;
    private final int addressElementsCount;

    public OSMGpsCoordinatesDescriptionGetter(GpsCoordinatesDescriptionCache cache, HttpClient httpClient, int addressElementsCount) {
        this.httpClient = httpClient;
        this.cache = cache;
        this.addressElementsCount = addressElementsCount;
    }

    @Override
    public synchronized String getCoordinatesDescription(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        String cachedData = this.cache.getFromCache(latitude, longitude);
        if (cachedData != null) {
            return cachedData;
        }

        try {
            HttpGet request = new HttpGet("http://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude);
            HttpResponse response = this.httpClient.execute(request);

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                Gson gson = new Gson();
                OpenStreetMapResult result = gson.fromJson(rd, OpenStreetMapResult.class);
                String formattedAddress = result.getFormattedAddress(this.addressElementsCount);
                this.cache.addToCache(latitude, longitude, formattedAddress);
                return formattedAddress;
            }
        } catch (IOException ex) {
            System.err.println("Cannot get data from google api " + ex);
            return null;
        }
    }

}
