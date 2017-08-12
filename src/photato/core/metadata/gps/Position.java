package photato.core.metadata.gps;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;

public class Position {

    public final Double latitude;
    public final Double longitude;

    @Expose
    public final String coordinatesDescription;

    public Position(Double latitude, Double longitude, String hardcodedPosition, String coordinatesDescription) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.coordinatesDescription = hardcodedPosition != null ? hardcodedPosition : coordinatesDescription;
    }

    public List<String> getCoordinatesDescriptionTags() {
        if (this.coordinatesDescription == null) {
            return new ArrayList<>();
        } else {
            String[] splitted = this.coordinatesDescription.split(",");

            List<String> result = new ArrayList<>();
            for (int i = splitted.length - 1; i >= Math.max(0, splitted.length - 3); i--) {
                List<String> elmts = new ArrayList<>();
                for (int j = splitted.length - 1; j >= i; j--) {
                    elmts.add(splitted[j].trim().replace("/", "-"));
                }
                result.add(String.join("/", elmts).trim());
            }

            return result;
        }
    }

}
