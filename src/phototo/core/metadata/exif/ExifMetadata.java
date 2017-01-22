package phototo.core.metadata.exif;

import com.google.gson.annotations.SerializedName;
import phototo.helpers.SafeSimpleDateFormat;
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

    @SerializedName("Orientation")
    private String orientation;

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
        int rotationId = this.getRotationId();

        if (rotationId < 5) {
            return Integer.parseInt(this.imageWidth);
        } else {
            return Integer.parseInt(this.imageHeight);
        }
    }

    public int getImageHeight() {
        int rotationId = this.getRotationId();

        if (rotationId < 5) {
            return Integer.parseInt(this.imageHeight);
        } else {
            return Integer.parseInt(this.imageWidth);
        }
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

    public int getRotationId() {
        if (this.orientation == null) {
            return 1;
        }

        switch (this.orientation) {
            case "Horizontal (normal)":
                return 1;
            case "Mirror horizontal":
                return 2;
            case "Rotate 180":
                return 3;
            case "Mirror vertical":
                return 4;
            case "Mirror horizontal and rotate 270 CW":
                return 5;
            case "Rotate 90 CW":
                return 6;
            case "Mirror horizontal and rotate 90 CW":
                return 7;
            case "Rotate 270 CW":
                return 8;
            default:
                return 1;
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
