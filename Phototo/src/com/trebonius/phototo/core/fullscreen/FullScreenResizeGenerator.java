package com.trebonius.phototo.core.fullscreen;

import com.trebonius.phototo.helpers.ImageHelper;
import com.trebonius.phototo.helpers.JpegEncoder;
import com.trebonius.phototo.helpers.Md5;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

public class FullScreenResizeGenerator implements IFullScreenResizeGenerator {

    private static final long maxFileAgeBeforeCleaning = 30 * 86400 * 1000L;
    private static final int wantedQuality = 90;
    private final FileSystem fileSystem;
    private final String folderName;
    private final Timer timer;

    public FullScreenResizeGenerator(FileSystem fileSystem, String folderName) throws IOException {
        this.fileSystem = fileSystem;
        this.folderName = folderName;

        if (!Files.exists(this.fileSystem.getPath(folderName))) {
            Files.createDirectory(this.fileSystem.getPath(folderName));
        }

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (File file : fileSystem.getPath(folderName).toFile().listFiles()) {
                    if (file.lastModified() < System.currentTimeMillis() - maxFileAgeBeforeCleaning) {
                        file.delete();
                    }
                }
            }
        }, 86400 * 1000, 86400 * 1000);

    }

    @Override
    public FileEntity getImage(File localFile, ContentType contentType, int height, int width) {
        File fullScreenFile = this.fileSystem.getPath(folderName, getCachedFilename(localFile)).toFile();
        if (!fullScreenFile.exists()) {
            try {
                BufferedImage image = ImageIO.read(localFile);

                if (height != image.getHeight() || width != image.getWidth()) {
                    image = ImageHelper.resizeImageSmooth(image, width, height);
                }

                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fullScreenFile))) {
                    new JpegEncoder(image, 90, outputStream).Compress();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        fullScreenFile.setLastModified(System.currentTimeMillis());
        return new FileEntity(fullScreenFile, contentType);
    }

    private static String getCachedFilename(File localFile) {
        long lastModified = localFile.lastModified();

        return Md5.encodeString(localFile.getAbsolutePath() + "_" + lastModified) + ".jpg";
    }
}
