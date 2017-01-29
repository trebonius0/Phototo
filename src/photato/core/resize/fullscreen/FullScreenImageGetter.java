package photato.core.resize.fullscreen;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.Routes;
import photato.core.entities.PhotatoPicture;
import photato.core.resize.ResizedImageGenerator;

public class FullScreenImageGetter extends ResizedImageGenerator implements IFullScreenImageGetter {

    private final Long maxCacheSize;
    private final int maxPictureWidth;
    private final int maxPictureHeight;

    public FullScreenImageGetter(FileSystem fileSystem, Path rootFolder, String cacheFolderName, int wantedQuality, int maxPictureWidth, int maxPictureHeight, Long maxCacheSize) throws IOException {
        super(fileSystem, rootFolder, cacheFolderName, wantedQuality, false);
        this.maxCacheSize = maxCacheSize;
        this.maxPictureHeight = maxPictureHeight;
        this.maxPictureWidth = maxPictureWidth;
    }

    @Override
    public void generateImage(Path localFile, long lastModifiedTimestamp, int rotationId) throws IOException {
        this.generateResizedPicture(localFile, lastModifiedTimestamp, rotationId);
    }

    @Override
    public void deleteImage(Path localFile, long lastModifiedTimestamp) throws IOException {
        this.deleteResizedPicture(localFile, lastModifiedTimestamp);
    }

    @Override
    public FileEntity getImage(Path localFile, ContentType contentType, PhotatoPicture picture) throws IOException {
        this.generateImage(localFile, picture.lastModificationTimestamp, picture.rawPicture.rotationId);
        return new FileEntity(this.fileSystem.getPath(this.resizedPicturesFolder.toString(), this.getResizedPictureFilename(localFile, picture.lastModificationTimestamp)).toFile(), contentType);
    }

    @Override
    public final String getImageUrl(Path originalFilename, long lastModifiedTimestamp) {
        return Routes.fullSizePicturesRootUrl + "/" + this.getResizedPictureFilename(originalFilename, lastModifiedTimestamp);
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

    @Override
    public boolean precomputationsEnabled() {
        return this.maxCacheSize == null;
    }

}
