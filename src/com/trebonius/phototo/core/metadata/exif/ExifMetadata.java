package com.trebonius.phototo.core.metadata.exif;

import com.google.gson.annotations.SerializedName;

public class ExifMetadata {

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

    public String[] getKeywords() {
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

}
