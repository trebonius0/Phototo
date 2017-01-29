package photato.core.resize.fullscreen;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.core.entities.PhotatoPicture;

public interface IFullScreenImageGetter {

    void generateImage(Path localFile, long lastModifiedTimestamp, int rotationId) throws IOException;

    void deleteImage(Path localFile, long lastModifiedTimestamp) throws IOException;

    FileEntity getImage(Path localFile, ContentType contentType, PhotatoPicture picture) throws IOException;

    String getImageUrl(Path originalFilename, long lastModifiedTimestamp);

    int getImageWidth(int originalWidth, int originalHeight);

    int getImageHeight(int originalWidth, int originalHeight);

    boolean precomputationsEnabled();
}
