package photato.core.resize.fullscreen;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.Routes;
import photato.core.entities.PhotatoPicture;
import photato.core.resize.ResizedImageGenerator;
import photato.helpers.LRUSet;

public class FullScreenImageGetter extends ResizedImageGenerator implements IFullScreenImageGetter {

    private final Long maxCacheSize;
    private final int maxPictureWidth;
    private final int maxPictureHeight;
    private final LRUSet<Path> picturesLRUSet;

    public FullScreenImageGetter(FileSystem fileSystem, Path rootFolder, String cacheFolderName, int wantedQuality, int maxPictureWidth, int maxPictureHeight, Long maxCacheSize) throws IOException {
        super(fileSystem, rootFolder, cacheFolderName, wantedQuality, false);
        this.maxCacheSize = maxCacheSize;
        this.maxPictureHeight = maxPictureHeight;
        this.maxPictureWidth = maxPictureWidth;

        if (maxCacheSize == null) {
            this.picturesLRUSet = null;
        } else {
            this.picturesLRUSet = new LRUSet<>();
            try {
                for (Path path : Files.list(this.resizedPicturesFolder).collect(Collectors.toList())) {
                    long fileSize = Files.size(path);
                    this.picturesLRUSet.add(path, fileSize);
                }

                this.cleanLRUCache();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void generateImage(PhotatoPicture picture) throws IOException {
        this.cleanLRUCache();

        boolean hasGeneratedNewPicture = this.generateResizedPicture(picture.fsPath, picture.lastModificationTimestamp, picture.rotationId);

        if (this.picturesLRUSet != null) {
            Path resizedPicturePath = this.getResizedPicturePath(picture.fsPath, picture.lastModificationTimestamp);
            if (hasGeneratedNewPicture) {
                this.picturesLRUSet.add(resizedPicturePath, Files.size(resizedPicturePath));
            } else {
                this.picturesLRUSet.ping(resizedPicturePath);
            }
        }

    }

    @Override
    public void deleteImage(PhotatoPicture picture) throws IOException {
        if (this.picturesLRUSet != null) {
            this.picturesLRUSet.remove(picture.fsPath);
        }
        this.deleteResizedPicture(picture.fsPath, picture.lastModificationTimestamp);
    }

    @Override
    public FileEntity getImage(PhotatoPicture picture, ContentType contentType) throws IOException {
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

    @Override
    public boolean precomputationsEnabled() {
        return this.maxCacheSize == null;
    }

    private void cleanLRUCache() throws IOException {
        if (this.maxCacheSize != null && this.picturesLRUSet != null) {
            while (this.picturesLRUSet.totalWeight() >= this.maxCacheSize) {
                Path lastPicture = this.picturesLRUSet.removeLast();
                this.deleteResizedPicture(lastPicture, Files.getLastModifiedTime(lastPicture).toMillis());
            }
        }
    }

}
