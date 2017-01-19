package com.trebonius.phototo.core.metadata;

import com.trebonius.phototo.core.metadata.gps.Position;
import com.trebonius.phototo.core.metadata.exif.ExifMetadata;

public class Metadata {

    public String title;
    public String[] tags;
    public String[] persons;
    public Position position;
    public long pictureCreationDate;
    public int width;
    public int height;

    public Metadata() {
    }

    public Metadata(ExifMetadata exifMetadata, Position position) {
        this.title = exifMetadata.getTitle();
        this.tags = exifMetadata.getTags();
        this.persons = exifMetadata.getPersons();
        this.position = position;
        this.pictureCreationDate = exifMetadata.getPictureDate();
        this.height = exifMetadata.getImageHeight();
        this.width = exifMetadata.getImageWidth();
    }

}
