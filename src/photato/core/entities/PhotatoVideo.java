package photato.core.entities;

import java.nio.file.Path;
import photato.core.metadata.Metadata;
import photato.helpers.Md5;

public class PhotatoVideo extends PhotatoMedia {

    public PhotatoVideo(Path rootFolder, Path path, Metadata metadata, PictureInfos thumbnailInfos, PictureInfos fullScreenInfos, long lastModificationTimestamp) {
        super("video", rootFolder, path, metadata, thumbnailInfos, fullScreenInfos, lastModificationTimestamp);

        if (this.filename.length() > 40 || this.filename.contains("_") || this.filename.toLowerCase().startsWith("vid")) {
            this.name = path.getParent().getFileName().toString();
        } else {
            this.name = path.getParent().getFileName() + "/" + this.filename;
        }
    }

    public static Path getExtractedPicturePath(Path extractedVideoPicturesFolders, Path videoFsPath, long videoLastModificationTimestamp) {
        String src = videoFsPath + "_" + videoLastModificationTimestamp;
        return extractedVideoPicturesFolders.resolve(Md5.encodeString(src) + ".jpg");
    }

}
