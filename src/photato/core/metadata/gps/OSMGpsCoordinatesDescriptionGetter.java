package photato.core.metadata.gps;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class OSMGpsCoordinatesDescriptionGetter implements IGpsCoordinatesDescriptionGetter {

    private static class OpenStreetMapResult {

        private static class Address {

            public String neighbourhood;
            public String suburb;
            @SerializedName("city_district")
            public String cityDistrict;
            public String village;
            public String town;
            public String city;
            public String state;
            public String country;

        }

        private Address address;
        @SerializedName("display_name")
        private String displayName;

        public String getDisplayName(int maxElementsCount) {
            if (this.address != null) {
                List<String> elements = new ArrayList<>();
                elements.add(this.address.neighbourhood);
                elements.add(this.address.suburb);
                elements.add(this.address.cityDistrict);
                elements.add(this.address.village);
                elements.add(this.address.town);
                elements.add(this.address.city);
                elements.add(this.address.state);
                elements.add(this.address.country);

                List<String> l = elements.stream()
                        .filter((String s) -> (s != null && !s.isEmpty()))
                        .distinct()
                        .collect(Collectors.toList());

                if (l.size() > maxElementsCount) {
                    l = l.subList(l.size() - maxElementsCount, l.size());
                }

                return String.join(", ", l);
            } else {
                return this.displayName;
            }
        }
    }

    private final HttpClient httpClient;
    private final int maxElementsCount;

    public OSMGpsCoordinatesDescriptionGetter(HttpClient httpClient, int maxElementsCount) {
        this.httpClient = httpClient;
        this.maxElementsCount = maxElementsCount;
    }

    @Override
    public synchronized String getCoordinatesDescription(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        try {
            HttpGet request = new HttpGet("http://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude);
            HttpResponse response = this.httpClient.execute(request);

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                Gson gson = new Gson();
                OpenStreetMapResult result = gson.fromJson(rd, OpenStreetMapResult.class);

                return result.getDisplayName(this.maxElementsCount);
            }
        } catch (IOException | JsonSyntaxException ex) {
            System.err.println("Cannot get data from openstreetmap api " + ex);
            return null;
        }
    }

}
