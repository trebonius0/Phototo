package com.trebonius.phototo.core.entities;

import com.trebonius.phototo.core.gps.Position;
import com.google.gson.annotations.Expose;
import com.trebonius.phototo.core.metadata.exif.ExifMetadata;
import java.nio.file.Path;
import com.trebonius.phototo.Routes;

public class PhototoPicture extends PhototoItem {

    @Expose
    public final String title;

    @Expose
    public final String parentAndName;

    @Expose
    public final String[] tags;

    @Expose
    public final String[] persons;

    @Expose
    public final Position position;

    @Expose
    public final PictureInfos thumbnail;

    @Expose
    public final PictureInfos picture;

    @Expose
    public final long lastModificationTimestamp;

    @Expose
    public final long pictureCreationDate;

    public PhototoPicture(Path rootFolder, Path path, ExifMetadata metadata, String thumbnailUrl, long lastModificationTimestamp) {
        super(rootFolder, path);
        this.title = metadata.title;
        this.tags = metadata.tags == null ? new String[]{} : metadata.tags;
        this.persons = metadata.persons == null ? new String[]{} : metadata.persons;
        this.position = metadata.position;
        this.thumbnail = new PictureInfos(thumbnailUrl, metadata.thumbnailWidth, metadata.thumbnailHeight);
        this.lastModificationTimestamp = lastModificationTimestamp;
        this.pictureCreationDate = metadata.pictureCreationDate;
        this.picture = new PictureInfos(Routes.fullSizePicturesRootUrl + "/" + this.path, metadata.width, metadata.height);

        if (this.filename.length() > 40) {
            this.parentAndName = path.getParent().getFileName().toString();
        } else {
            this.parentAndName = path.getParent().getFileName() + "/" + this.filename;
        }
    }

}
