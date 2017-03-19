package photato.core.resize.fullscreen;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.core.entities.PhotatoMedia;

public interface IFullScreenImageGetter {

    void generateImage(PhotatoMedia media) throws IOException;

    void deleteImage(PhotatoMedia media) throws IOException;

    FileEntity getImage(PhotatoMedia media, ContentType contentType) throws IOException;

    String getImageUrl(Path originalFilename, long lastModifiedTimestamp);

    int getImageWidth(int originalWidth, int originalHeight);

    int getImageHeight(int originalWidth, int originalHeight);
}
