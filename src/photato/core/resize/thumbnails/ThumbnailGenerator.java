package photato.core.resize.thumbnails;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import photato.Routes;
import photato.core.entities.PhotatoMedia;
import photato.core.entities.PhotatoPicture;
import photato.core.resize.ResizedImageGenerator;

public class ThumbnailGenerator extends ResizedImageGenerator implements IThumbnailGenerator {

    private final int thumbailHeight;

    public ThumbnailGenerator(FileSystem fileSystem, Path rootFolder, String thumbnailsFolderName, String extractedPicturesFolderName, int thumbnailHeight, int thumbnailQuality) throws IOException {
        super(fileSystem, rootFolder, thumbnailsFolderName, extractedPicturesFolderName, thumbnailQuality, true);
        this.thumbailHeight = thumbnailHeight;
    }

    @Override
    public final String getThumbnailUrl(Path originalFilename, long lastModifiedTimestamp) {
        return Routes.thumbnailRootUrl + "/" + this.getResizedPictureFilename(originalFilename, lastModifiedTimestamp);
    }

    @Override
    protected int getResizedPictureWidth(int originalWidth, int originalHeight) {
        return originalWidth * thumbailHeight / Math.max(1, originalHeight);
    }

    @Override
    protected int getResizedPictureHeight(int originalWidth, int originalHeight) {
        return thumbailHeight;
    }

    @Override
    public void generateThumbnail(PhotatoMedia media) throws IOException {
        this.generateResizedPicture(media.fsPath, media.lastModificationTimestamp, media instanceof PhotatoPicture ? ((PhotatoPicture) media).rotationId : 1);
    }

    @Override
    public void deleteThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        this.deleteResizedPicture(originalFilename, lastModifiedTimestamp);
    }

    @Override
    public int getThumbnailWidth(int originalWidth, int originalHeight) {
        return this.getResizedPictureWidth(originalWidth, originalHeight);
    }

    @Override
    public int getThumbnailHeight(int originalWidth, int originalHeight) {
        return this.getResizedPictureHeight(originalWidth, originalHeight);
    }

}
