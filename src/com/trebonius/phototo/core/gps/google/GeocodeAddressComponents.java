package com.trebonius.phototo.core.gps.google;

import com.google.gson.annotations.SerializedName;
import java.util.List;

class GeocodeAddressComponents {

    @SerializedName("long_name")
    public String longName;

    @SerializedName("short_name")
    public String shortName;

    public List<String> types;
}
