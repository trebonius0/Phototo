package photato.core.entities;

import photato.core.metadata.gps.Position;
import com.google.gson.annotations.Expose;
import java.nio.file.Path;
import photato.Routes;
import photato.core.metadata.Metadata;

public class PhotatoPicture extends PhotatoItem {

    @Expose
    public final String title;

    @Expose
    public final String pictureName;

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

    @Expose
    public final PictureInfos rawPicture;

    public final int rotationId;

    public final long lastModificationTimestamp; // This field is used to generated the appropriate thumbnail. It is the modifiaction date on the filesystem

    @Expose
    public final long pictureDate;

    public PhotatoPicture(Path rootFolder, Path path, Metadata metadata, PictureInfos thumbnailInfos, PictureInfos fullScreenInfos, long lastModificationTimestamp) {
        super(rootFolder, path);
        this.title = metadata.title;
        this.tags = metadata.tags == null ? new String[]{} : metadata.tags;
        this.persons = metadata.persons == null ? new String[]{} : metadata.persons;
        this.position = metadata.position;
        this.thumbnail = thumbnailInfos;
        this.lastModificationTimestamp = lastModificationTimestamp;
        this.pictureDate = metadata.pictureDate;
        this.fullscreenPicture = fullScreenInfos;
        this.rotationId = metadata.rotationId;
        this.rawPicture = new PictureInfos(Routes.rawPicturesRootUrl + "/" + this.path, metadata.width, metadata.height);

        if (this.filename.length() > 40 || this.filename.contains("_")) {
            this.pictureName = path.getParent().getFileName().toString();
        } else {
            this.pictureName = path.getParent().getFileName() + "/" + this.filename;
        }
    }

}
