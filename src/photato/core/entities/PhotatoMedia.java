package photato.core.entities;

import com.google.gson.annotations.Expose;
import java.nio.file.Path;
import photato.core.metadata.Metadata;
import photato.core.metadata.gps.Position;

public abstract class PhotatoMedia extends PhotatoItem {

    @Expose
    public final String mediaType;

    @Expose
    public final String title;

    @Expose
    public String name;

    @Expose
    public final String[] tags;

    @Expose
    public final String[] persons;

    @Expose
    public final Position position;

    @Expose
    public final PictureInfos thumbnail;

    @Expose
    public final PictureInfos fullscreenPicture;

    public final long lastModificationTimestamp; // This field is used to generated the appropriate thumbnail. It is the modifiaction date on the filesystem

    @Expose
    public final long timestamp;

    public PhotatoMedia(String mediaType, Path rootFolder, Path path, Metadata metadata, PictureInfos thumbnailInfos, PictureInfos fullScreenInfos, long lastModificationTimestamp) {
        super(rootFolder, path);
        this.mediaType = mediaType;
        this.title = metadata.title;
        this.tags = metadata.tags == null ? new String[]{} : metadata.tags;
        this.persons = metadata.persons == null ? new String[]{} : metadata.persons;
        this.position = metadata.position;
        this.thumbnail = thumbnailInfos;
        this.lastModificationTimestamp = lastModificationTimestamp;
        this.timestamp = metadata.pictureDate;
        this.fullscreenPicture = fullScreenInfos;
    }

}
