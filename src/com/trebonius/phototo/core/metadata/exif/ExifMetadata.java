package com.trebonius.phototo.core.metadata.exif;

import com.google.gson.annotations.SerializedName;
import com.trebonius.phototo.helpers.SafeSimpleDateFormat;
import java.text.ParseException;
import java.util.List;

public class ExifMetadata {

    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    @SerializedName("SourceFile")
    private String sourceFile;

    @SerializedName("RegionPersonDisplayName")
    private Object regionPersonDisplayName;

    @SerializedName("Title")
    private String title;

    @SerializedName("Subject")
    private Object subject;

    @SerializedName("CreateDate")
    private String createDate;

    @SerializedName("ModifyDate")
    private String modifyDate;

    @SerializedName("ImageWidth")
    private String imageWidth;

    @SerializedName("ImageHeight")
    private String imageHeight;

    @SerializedName("LocationCreatedCity")
    private String locationCreatedCity;

    @SerializedName("LocationCreatedProvinceState")
    private String locationCreatedProvinceState;

    @SerializedName("LocationCreatedCountryName")
    private String locationCreatedCountryName;

    @SerializedName("GPSPosition")
    private String GPSPosition;

    public String getSourceFile() {
        return sourceFile;
    }

    public String[] getPersons() {
        return getStringArrayFromField(this.regionPersonDisplayName);
    }

    public String[] getTags() {
        return getStringArrayFromField(this.subject);
    }

    public String getTitle() {
        return this.title;
    }

    public int getImageWidth() {
        return Integer.parseInt(this.imageWidth);
    }

    public int getImageHeight() {
        return Integer.parseInt(this.imageHeight);
    }

    public String getGPSPositionString() {
        return this.GPSPosition;
    }

    public String getHardcodedPosition() {
        if (this.locationCreatedCity != null && !this.locationCreatedCity.trim().isEmpty()
                && this.locationCreatedProvinceState != null && !this.locationCreatedProvinceState.trim().isEmpty()
                && this.locationCreatedCountryName != null && !this.locationCreatedCountryName.trim().isEmpty()) {
            return this.locationCreatedCity + " " + this.locationCreatedProvinceState + " " + this.locationCreatedCountryName;
        } else {
            return null;
        }
    }

    public long getPictureDate() {
        try {
            if (this.createDate != null) {
                return DATE_FORMAT.parse(this.createDate).getTime();
            } else if (this.modifyDate != null) {
                return DATE_FORMAT.parse(this.modifyDate).getTime();
            } else {
                return 0;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private static String[] getStringArrayFromField(Object field) {
        if (field == null) {
            return null;
        } else if (field instanceof String[]) {
            return (String[]) field;
        } else if (field instanceof List) {
            return (String[]) ((List) field).stream().map((o) -> o.toString()).toArray(String[]::new);
        } else if (field instanceof String) {
            return new String[]{(String) field};
        } else {
            throw new IllegalArgumentException();
        }
    }

}
