package photato.core.metadata.gps;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

public class OpenStreetMapResultTest {

    @Test
    public void testGetFormattedAddress() {
        String testStr = "{\"place_id\":\"6881401\",\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. http:\\/\\/www.openstreetmap.org\\/copyright\",\"osm_type\":\"node\",\"osm_id\":\"764458604\",\"lat\":\"48.8381667\",\"lon\":\"2.3479283\",\"display_name\":\"34, Rue Broca, Quartier du Val-de-Grâce, Paris 5e Arrondissement, Paris, Île-de-France, 75005, France\",\"address\":{\"house_number\":\"34\",\"road\":\"Rue Broca\",\"suburb\":\"Quartier du Val-de-Grâce\",\"city_district\":\"Paris 5e Arrondissement\",\"city\":\"Paris\",\"county\":\"Paris\",\"state\":\"Île-de-France\",\"postcode\":\"75005\",\"country\":\"France\",\"country_code\":\"fr\"},\"boundingbox\":[\"48.8380667\",\"48.8382667\",\"2.3478283\",\"2.3480283\"]}";
        Gson gson = new Gson();
        OpenStreetMapResult v = gson.fromJson(testStr, OpenStreetMapResult.class);
        Assert.assertEquals("Paris 5e Arrondissement, Paris, Île-de-France, France", v.getFormattedAddress(4));
        Assert.assertEquals("34, Rue Broca, Quartier du Val-de-Grâce, Paris 5e Arrondissement, Paris, Île-de-France, France", v.getFormattedAddress(999));
    }

}
