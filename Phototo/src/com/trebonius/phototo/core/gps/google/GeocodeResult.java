package com.trebonius.phototo.core.gps.google;

import com.google.gson.annotations.SerializedName;
import java.util.List;

class GeocodeResult {

    @SerializedName("address_components")
    public List<GeocodeAddressComponents> addressComponents;

    @SerializedName("formatted_address")
    public String formattedAddress;

    public List<String> types;
}
