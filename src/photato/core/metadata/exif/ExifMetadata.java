package photato.core.metadata.exif;

import com.google.gson.annotations.SerializedName;
import photato.helpers.SafeSimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import photato.helpers.ArrayHelper;

public class ExifMetadata {

    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    @SerializedName("SourceFile")
    private String sourceFile;

    @SerializedName("RegionPersonDisplayName")
    private Object regionPersonDisplayName;

    @SerializedName("PersonInImage")
    private Object personInImage;

    @SerializedName("Title")
    private String title;

    @SerializedName("Subject")
    private Object subject;

    @SerializedName("CreateDate")
    private String createDate;

    @SerializedName("ModifyDate")
    private String modifyDate;

    @SerializedName("FileCreateDate")
    private String createDate2;

    @SerializedName("FileModifyDate")
    private String modifyDate2;

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

    @SerializedName("Rotation")
    private String rotation;

    @SerializedName("AutoRotate")
    private String autorotate;

    public String getSourceFile() {
        return sourceFile;
    }

    public String[] getPersons() {
        String[] p1 = getStringArrayFromField(this.regionPersonDisplayName);
        String[] p2 = getStringArrayFromField(this.personInImage);

        return ArrayHelper.concatenate(p1, p2);
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
        int rotationId = 1;
        int orientationId = 1;
        int autoRotateId = 1;

        if (this.autorotate != null) {
            switch (this.autorotate) {
                case "Horizontal (normal)":
                    autoRotateId = 1;
                    break;
                case "Mirror horizontal":
                    autoRotateId = 2;
                    break;
                case "Rotate 180":
                    autoRotateId = 3;
                    break;
                case "Mirror vertical":
                    autoRotateId = 4;
                    break;
                case "Mirror horizontal and rotate 270 CW":
                    autoRotateId = 5;
                    break;
                case "Rotate 90 CW":
                    autoRotateId = 6;
                    break;
                case "Mirror horizontal and rotate 90 CW":
                    autoRotateId = 7;
                    break;
                case "Rotate 270 CW":
                    autoRotateId = 8;
                    break;
                default:
                    autoRotateId = 1;
                    break;
            }
        }

        if (this.orientation != null) {
            switch (this.orientation) {
                case "Horizontal (normal)":
                    orientationId = 1;
                    break;
                case "Mirror horizontal":
                    orientationId = 2;
                    break;
                case "Rotate 180":
                    orientationId = 3;
                    break;
                case "Mirror vertical":
                    orientationId = 4;
                    break;
                case "Mirror horizontal and rotate 270 CW":
                    orientationId = 5;
                    break;
                case "Rotate 90 CW":
                    orientationId = 6;
                    break;
                case "Mirror horizontal and rotate 90 CW":
                    orientationId = 7;
                    break;
                case "Rotate 270 CW":
                    orientationId = 8;
                    break;
                default:
                    orientationId = 1;
                    break;
            }
        }

        if (this.rotation != null) {
            switch (this.rotation) {
                case "0":
                case "Horizontal":
                    autoRotateId = 1;
                    break;
                case "180":
                case "-180":
                    autoRotateId = 3;
                    break;
                case "90":
                case "-90":
                case "Vertical":
                    autoRotateId = 6;
                    break;
                case "270":
                case "-270":
                    return 8;
                default:
                    autoRotateId = 1;
                    break;
            }
        }

        return Math.max(Math.max(rotationId, orientationId), autoRotateId);
    }

    public long getPictureDate() {
        if (this.createDate != null) {
            try {
                return DATE_FORMAT.parse(this.createDate).getTime();
            } catch (ParseException ex) {
            }
        }

        if (this.modifyDate != null) {
            try {
                return DATE_FORMAT.parse(this.modifyDate).getTime();
            } catch (ParseException ex) {
            }
        }

        if (this.createDate2 != null) {
            try {
                return DATE_FORMAT.parse(this.createDate2).getTime();
            } catch (ParseException ex) {
            }
        }

        if (this.modifyDate2 != null) {
            try {
                return DATE_FORMAT.parse(this.modifyDate2).getTime();
            } catch (ParseException ex) {
            }
        }

        return 0;
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
