package com.trebonius.phototo.core.fullscreen;

import java.io.File;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

public interface IFullScreenResizeGenerator {

    FileEntity getImage(File localFile, ContentType contentType, int height, int width);
}
