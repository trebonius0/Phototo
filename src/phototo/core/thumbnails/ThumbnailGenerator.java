package phototo.core.thumbnails;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        String src = originalFilename.toAbsolutePath() + "_" + lastModifiedTimestamp;
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
            this.thumbnailsSet.addAll(Files.list(this.thumbnailsFolderName).collect(Collectors.toSet()));
            Set<Path> toDeleteThumbnails = new HashSet<>(this.thumbnailsSet);

            Queue<Path> toExplore = new LinkedList<>();
            toExplore.add(this.rootFolder);
            while (!toExplore.isEmpty()) {
                File currentFolder = toExplore.remove().toFile();

                if (currentFolder.canRead()) {
                    File[] files = currentFolder.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                toExplore.add(file.toPath());
                            } else {
                                Path p = this.thumbnailsFolderName.resolve(getThumbnailFilename(file.toPath(), file.lastModified()));
                                toDeleteThumbnails.remove(p);
                            }
                        }
                    }
                }
            }

            // Thumbnails with no real picture anymore
            for (Path toDeleteThumbnail : toDeleteThumbnails) {
                toDeleteThumbnail.toFile().delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
