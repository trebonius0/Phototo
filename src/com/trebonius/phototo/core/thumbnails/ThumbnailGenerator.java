package com.trebonius.phototo.core.thumbnails;

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
import com.trebonius.phototo.Routes;
import com.trebonius.phototo.helpers.FileHelper;
import com.trebonius.phototo.helpers.ImageHelper;
import com.trebonius.phototo.helpers.JpegEncoder;

public class ThumbnailGenerator implements IThumbnailGenerator {

    private static final int wantedHeight = 170;
    private static final int wantedQuality = 85;
    private final FileSystem fileSystem;
    private final String thumbnailsFolderName;
    private final Path rootFolder;
    private final Set<Path> thisTimeGeneratedThumbnails;
    private final Set<Path> initiallyGeneratedThumbnails;
    private final Object lock = new Object();

    public ThumbnailGenerator(FileSystem fileSystem, Path rootFolder, String thumbnailsFolderName) throws IOException {
        this.fileSystem = fileSystem;
        this.thumbnailsFolderName = thumbnailsFolderName;
        this.rootFolder = rootFolder;
        this.thisTimeGeneratedThumbnails = new HashSet<>();

        if (!Files.exists(this.fileSystem.getPath(this.thumbnailsFolderName))) {
            Files.createDirectory(this.fileSystem.getPath(this.thumbnailsFolderName));
        }
        this.initiallyGeneratedThumbnails = Files.list(this.fileSystem.getPath(this.thumbnailsFolderName)).collect(Collectors.toSet());
    }

    @Override
    public void generateThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        Path path = this.fileSystem.getPath(this.thumbnailsFolderName, getThumbnailFilename(originalFilename, lastModifiedTimestamp));

        synchronized (this.lock) {
            if (thisTimeGeneratedThumbnails.contains(path)) {
                return;
            }

            this.thisTimeGeneratedThumbnails.add(path);

            if (this.initiallyGeneratedThumbnails.contains(path)) {
                return;
            }
        }

        BufferedImage originalImage = ImageIO.read(originalFilename.toFile());
        int newWidth = getThumbnailWidth(originalImage.getWidth(), originalImage.getHeight());
        BufferedImage resized = ImageHelper.resizeImageSmooth(originalImage, newWidth, wantedHeight);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            new JpegEncoder(resized, wantedQuality, out).Compress();
        }

    }

    @Override
    public void deleteThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        synchronized (this.lock) {
            Path p = this.fileSystem.getPath(this.thumbnailsFolderName, getThumbnailFilename(originalFilename, lastModifiedTimestamp));
            Files.deleteIfExists(p);
            this.thisTimeGeneratedThumbnails.remove(p);
        }
    }

    @Override
    public void cleanOutdated() {
        synchronized (this.lock) {
            for (Path initiallyGeneratedThumbnail : this.initiallyGeneratedThumbnails) {
                if (!this.thisTimeGeneratedThumbnails.contains(initiallyGeneratedThumbnail)) {
                    try {
                        Files.delete(initiallyGeneratedThumbnail);
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    @Override
    public String getThumbnailUrl(Path originalFilename, long lastModifiedTimestamp) {
        return Routes.thumbnailRootUrl + "/" + this.getThumbnailFilename(originalFilename, lastModifiedTimestamp);
    }

    private String getThumbnailFilename(Path originalFilename, long lastModifiedTimestamp) {
        return FileHelper.encodeToFilesystemString(this.rootFolder.relativize(originalFilename) + "_" + lastModifiedTimestamp) + ".jpg";
    }

    @Override
    public int getThumbnailWidth(int originalWidth, int originalHeight) {
        return originalWidth * wantedHeight / Math.max(1, originalHeight);
    }

    @Override
    public int getThumbnailHeight(int originalWidth, int originalHeight) {
        return wantedHeight;
    }

}
