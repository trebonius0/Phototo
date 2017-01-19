package phototo.core.thumbnails;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import phototo.Routes;
import phototo.helpers.ImageHelper;
import phototo.helpers.JpegEncoder;
import phototo.helpers.Md5;

public class ThumbnailGenerator implements IThumbnailGenerator {

    private static final int wantedHeight = 170;
    private static final int wantedQuality = 85;
    private final FileSystem fileSystem;
    private final Path thumbnailsFolderName;
    private final Path rootFolder;
    private final Set<Path> thumbnailsSet;
    private final Object lock = new Object();

    public ThumbnailGenerator(FileSystem fileSystem, Path rootFolder, String thumbnailsFolderName) throws IOException {
        this.fileSystem = fileSystem;
        this.thumbnailsFolderName = this.fileSystem.getPath(thumbnailsFolderName);
        this.thumbnailsSet = new HashSet<>();

        if (!Files.exists(this.thumbnailsFolderName)) {
            Files.createDirectory(this.thumbnailsFolderName);
        }
        this.rootFolder = rootFolder;

        this.initAndCleanOutdatedThumbnails();
    }

    @Override
    public void generateThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        Path path = this.thumbnailsFolderName.resolve(getThumbnailFilename(originalFilename, lastModifiedTimestamp));

        synchronized (this.lock) {
            if (thumbnailsSet.contains(path)) {
                return;
            }

            this.thumbnailsSet.add(path);
        }

        BufferedImage originalImage = ImageIO.read(originalFilename.toFile());
        int newWidth = getThumbnailWidth(originalImage.getWidth(), originalImage.getHeight());
        int newHeight = getThumbnailHeight(originalImage.getWidth(), originalImage.getHeight());
        BufferedImage resized = ImageHelper.resizeImageSmooth(originalImage, newWidth, newHeight);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            new JpegEncoder(resized, wantedQuality, out).Compress();
        }

    }

    @Override
    public void deleteThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        synchronized (this.lock) {
            Path p = this.thumbnailsFolderName.resolve(getThumbnailFilename(originalFilename, lastModifiedTimestamp));
            Files.deleteIfExists(p);
            this.thumbnailsSet.remove(p);
        }
    }

    @Override
    public String getThumbnailUrl(Path originalFilename, long lastModifiedTimestamp) {
        return Routes.thumbnailRootUrl + "/" + this.getThumbnailFilename(originalFilename, lastModifiedTimestamp);
    }

    private String getThumbnailFilename(Path originalFilename, long lastModifiedTimestamp) {
        String src = this.rootFolder.relativize(originalFilename) + "_" + lastModifiedTimestamp;
        return Md5.encodeString(src) + ".jpg";
    }

    @Override
    public int getThumbnailWidth(int originalWidth, int originalHeight) {
        return originalWidth * wantedHeight / Math.max(1, originalHeight);
    }

    @Override
    public int getThumbnailHeight(int originalWidth, int originalHeight) {
        return wantedHeight;
    }

    private void initAndCleanOutdatedThumbnails() {
        try {
            Set<Path> existingThumbnails = Files.list(this.thumbnailsFolderName).collect(Collectors.toSet());

            Files.find(this.rootFolder, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
                    .forEach((Path path) -> {
                        Path p = this.thumbnailsFolderName.resolve(getThumbnailFilename(path, path.toFile().lastModified()));
                        existingThumbnails.remove(p);
                        this.thumbnailsSet.add(p);
                    });

            // Thumbnails with no real picture anymore
            for (Path existingThumbnail : existingThumbnails) {
                existingThumbnail.toFile().delete();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
