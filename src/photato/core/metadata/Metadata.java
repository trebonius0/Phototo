package photato.core.metadata;

import photato.core.metadata.gps.Position;
import photato.core.metadata.exif.ExifMetadata;

public class Metadata {

    public String title;
    public String[] tags;
    public String[] persons;
    public Position position;
    public long pictureDate;
    public int width;
    public int height;
    public int rotationId;

    public Metadata() {
    }

    public Metadata(ExifMetadata exifMetadata, Position position) {
        this.title = exifMetadata.getTitle();
        this.tags = exifMetadata.getTags();
        this.persons = exifMetadata.getPersons();
        this.position = position;
        this.pictureDate = exifMetadata.getPictureDate();
        this.height = exifMetadata.getImageHeight();
        this.width = exifMetadata.getImageWidth();
        this.rotationId = exifMetadata.getRotationId();
    }

}
