package photato.core.metadata.gps;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import photato.helpers.CollectorsHelper;

public class OpenStreetMapResult {

    @SerializedName("display_name")
    public String displayName;

    public LinkedHashMap<String, String> address;

    public String getFormattedAddress(int elementsCount) {
        String[] ignore = new String[]{"postcode", "country_code", "county", "state_district"};

        List<String> l = this.address.entrySet().stream()
                .filter((Map.Entry<String, String> e) -> !Arrays.asList(ignore).contains(e.getKey()))
                .map(Map.Entry::getValue)
                .distinct()
                .collect(CollectorsHelper.lastN(elementsCount));

        return String.join(", ", l);
    }
}
