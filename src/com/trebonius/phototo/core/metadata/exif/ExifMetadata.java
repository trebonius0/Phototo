package com.trebonius.phototo.core.metadata.exif;

import com.google.gson.annotations.SerializedName;
import com.trebonius.phototo.helpers.SafeSimpleDateFormat;
import java.text.ParseException;

public class ExifMetadata {

    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    @SerializedName("SourceFile")
    private String sourceFile;

    @SerializedName("RegionPersonDisplayName")
    private String regionPersonDisplayName;

    @SerializedName("Title")
    private String title;

    @SerializedName("Subject")
    private String subject;

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
        return this.regionPersonDisplayName.split(","); // TODO: check if ", " instead
    }

    public String[] getTags() {
        return this.subject.split(","); // TODO: check if ", " instead
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
            System.err.println(ex);
            return 0;
        }
    }

}
