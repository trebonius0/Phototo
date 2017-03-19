package photato.core.resize.fullscreen;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.Routes;
import photato.core.entities.PhotatoMedia;
import photato.core.resize.ResizedImageGenerator;

public class FullScreenImageGetter extends ResizedImageGenerator implements IFullScreenImageGetter {

    private final int maxPictureWidth;
    private final int maxPictureHeight;

    public FullScreenImageGetter(FileSystem fileSystem, Path rootFolder, String cacheFolderName, int wantedQuality, int maxPictureWidth, int maxPictureHeight) throws IOException {
        super(fileSystem, rootFolder, cacheFolderName, wantedQuality, false);
        this.maxPictureHeight = maxPictureHeight;
        this.maxPictureWidth = maxPictureWidth;
    }

    @Override
    public void generateImage(PhotatoMedia media) throws IOException {
        this.generateResizedPicture(media.fsPath, media.lastModificationTimestamp, media.rotationId);
    }

    @Override
    public void deleteImage(PhotatoMedia media) throws IOException {
        this.deleteResizedPicture(media.fsPath, media.lastModificationTimestamp);
    }

    @Override
    public FileEntity getImage(PhotatoMedia picture, ContentType contentType) throws IOException {
        this.generateImage(picture);
        return new FileEntity(this.fileSystem.getPath(this.resizedPicturesFolder.toString(), this.getResizedPictureFilename(picture.fsPath, picture.lastModificationTimestamp)).toFile(), contentType);
    }

    @Override
    public final String getImageUrl(Path originalFilename, long lastModificationTimestamp) {
        return Routes.fullScreenPicturesRootUrl + "/" + this.getResizedPictureFilename(originalFilename, lastModificationTimestamp);
    }

    @Override
    public int getImageWidth(int originalWidth, int originalHeight) {
        return this.getResizedPictureWidth(originalWidth, originalHeight);
    }

    @Override
    public int getImageHeight(int originalWidth, int originalHeight) {
        return this.getResizedPictureHeight(originalWidth, originalHeight);
    }

    @Override
    protected int getResizedPictureWidth(int originalWidth, int originalHeight) {
        int h1 = originalHeight * this.maxPictureWidth / originalWidth;
        if (h1 > this.maxPictureHeight) {
            // maxHeight is the constraint
            return originalWidth * this.maxPictureHeight / originalHeight;
        } else {
            // maxWidth is the constraint
            return this.maxPictureWidth;
        }
    }

    @Override
    protected int getResizedPictureHeight(int originalWidth, int originalHeight) {
        int h1 = originalHeight * this.maxPictureWidth / originalWidth;
        if (h1 > this.maxPictureHeight) {
            // maxHeight is the constraint
            return this.maxPictureHeight;
        } else {
            // maxWidth is the constraint
            return h1;
        }
    }

}
