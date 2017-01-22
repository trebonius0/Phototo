package phototo.core.fullscreen;

import phototo.helpers.ImageHelper;
import phototo.helpers.JpegEncoder;
import phototo.helpers.Md5;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.Closeable;
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

public class FullScreenImageEntityGetter implements IFullScreenImageEntityGetter, Closeable {

    private static final long maxFileAgeBeforeCleaning = 30 * 86400 * 1000L;
    private static final int wantedQuality = 90;
    private final FileSystem fileSystem;
    private final String folderName;
    private final Timer timer;

    public FullScreenImageEntityGetter(FileSystem fileSystem, String cacheFolderName) throws IOException {
        this.fileSystem = fileSystem;
        this.folderName = cacheFolderName;

        if (!Files.exists(this.fileSystem.getPath(cacheFolderName))) {
            Files.createDirectory(this.fileSystem.getPath(cacheFolderName));
        }

        // Setting up the folder cleaning task
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (File file : fileSystem.getPath(cacheFolderName).toFile().listFiles()) {
                    if (file.lastModified() < System.currentTimeMillis() - maxFileAgeBeforeCleaning) {
                        file.delete();
                    }
                }
            }
        }, 86400 * 1000, 86400 * 1000);

    }

    @Override
    public FileEntity getImage(File localFile, ContentType contentType, int height, int width, int orientationId) {
        File fullScreenFile = this.fileSystem.getPath(folderName, getCachedFilename(localFile)).toFile();
        if (!fullScreenFile.exists()) {
            try {
                BufferedImage image = ImageHelper.readImage(localFile.toPath(), orientationId);

                if (height != image.getHeight() || width != image.getWidth()) {
                    image = ImageHelper.resizeImageSmooth(image, width, height);
                }

                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fullScreenFile))) {
                    new JpegEncoder(image, wantedQuality, outputStream).Compress();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        fullScreenFile.setLastModified(System.currentTimeMillis()); // Changing the last modified timestamp to prevent it from being deleted by the folder cleaning task
        return new FileEntity(fullScreenFile, contentType);
    }

    @Override
    public void close() throws IOException {
        this.timer.cancel();
    }

    private static String getCachedFilename(File localFile) {
        long lastModified = localFile.lastModified();

        return Md5.encodeString(localFile.getAbsolutePath() + "_" + lastModified) + ".jpg";
    }

}
