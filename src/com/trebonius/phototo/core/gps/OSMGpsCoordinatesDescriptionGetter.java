package com.trebonius.phototo.core.gps;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class OSMGpsCoordinatesDescriptionGetter implements IGpsCoordinatesDescriptionGetter {

    private static class OpenStreetMapResult {

        @SerializedName("display_name")
        public String displayName;
    }

    private final HttpClient httpClient;
    private final GpsCoordinatesDescriptionCache cache;

    public OSMGpsCoordinatesDescriptionGetter(GpsCoordinatesDescriptionCache cache) {
        this.httpClient = HttpClientBuilder.create().build();
        this.cache = cache;
    }

    @Override
    public String getCoordinatesDescription(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        String cachedData = this.cache.getFromCache(latitude, longitude);
        if (cachedData != null) {
            return cachedData;
        }

        try {
            HttpGet request = new HttpGet("http://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude);
            request.addHeader("User-Agent", "Phototo 1.0");
            HttpResponse response = this.httpClient.execute(request);

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                Gson gson = new Gson();
                OpenStreetMapResult result = gson.fromJson(rd, OpenStreetMapResult.class);
                this.cache.addToCache(latitude, longitude, result.displayName);
                return result.displayName;
            }
        } catch (IOException ex) {
            System.err.println("Cannot get data from google api " + ex);
            return null;
        }
    }

}
