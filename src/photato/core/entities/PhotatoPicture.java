package photato.core.entities;

import com.google.gson.annotations.Expose;
import java.nio.file.Path;
import photato.Routes;
import photato.core.metadata.Metadata;

public class PhotatoPicture extends PhotatoMedia {

    @Expose
    public final PictureInfos rawPicture;

    public final int rotationId;

    public PhotatoPicture(Path rootFolder, Path path, Metadata metadata, PictureInfos thumbnailInfos, PictureInfos fullScreenInfos, long lastModificationTimestamp) {
        super("picture", rootFolder, path, metadata, thumbnailInfos, fullScreenInfos, lastModificationTimestamp);
        this.rotationId = metadata.rotationId;
        this.rawPicture = new PictureInfos(Routes.rawPicturesRootUrl + "/" + this.path, metadata.width, metadata.height);

        if (this.filename.length() > 40 || this.filename.contains("_") || this.filename.toLowerCase().startsWith("dsc") || this.filename.toLowerCase().startsWith("img")) {
            this.name = path.getParent().getFileName().toString();
        } else {
            this.name = path.getParent().getFileName() + "/" + this.filename;
        }
    }

}
