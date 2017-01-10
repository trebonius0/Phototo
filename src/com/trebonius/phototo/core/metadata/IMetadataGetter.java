package com.trebonius.phototo.core.metadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import com.trebonius.phototo.core.thumbnails.IThumbnailGenerator;

public interface IMetadataGetter {

    Metadata getMetadata(Path path, long lastModificationTimestamp, IThumbnailGenerator thumbnailGenerator);
    
    void startAutoSave();
}
