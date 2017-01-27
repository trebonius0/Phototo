package photato.core.resize;

import java.io.IOException;
import java.nio.file.Path;
import photato.core.metadata.Metadata;

public interface IResizedPictureGenerator {

    void generateResizedPicture(Path originalFilename, long lastModifiedTimestamp, Metadata metadata) throws IOException;

    void deleteResizedPicture(Path originalFilename, long lastModifiedTimestamp) throws IOException;

    String getResizedPictureUrl(Path originalFilename, long lastModifiedTimestamp);

    int getResizedPictureWidth(int originalWidth, int originalHeight);

    int getResizedPictureHeight(int originalWidth, int originalHeight);
}
