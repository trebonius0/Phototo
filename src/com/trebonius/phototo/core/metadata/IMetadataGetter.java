package com.trebonius.phototo.core.metadata;

import com.trebonius.phototo.core.metadata.exif.ExifMetadata;
import java.nio.file.Path;

public interface IMetadataGetter {

    ExifMetadata getMetadata(Path path, long lastModificationTimestamp);
}
