package photato.core.resize.fullscreen;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.core.entities.PhotatoPicture;

public interface IFullScreenImageGetter {

    void generateImage(PhotatoPicture picture) throws IOException;

    void deleteImage(PhotatoPicture picture) throws IOException;

    FileEntity getImage(PhotatoPicture picture, ContentType contentType) throws IOException;

    String getImageUrl(Path originalFilename, long lastModifiedTimestamp);

    int getImageWidth(int originalWidth, int originalHeight);

    int getImageHeight(int originalWidth, int originalHeight);

    boolean precomputationsEnabled();
}
