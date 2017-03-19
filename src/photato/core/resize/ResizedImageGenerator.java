package photato.core.resize;

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
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import photato.helpers.FileHelper;
import photato.helpers.ImageHelper;
import photato.helpers.JpegEncoder;
import photato.helpers.Md5;

public abstract class ResizedImageGenerator {

    private final int resizedPictureQuality;
    private final boolean forceResize;
    protected final FileSystem fileSystem;
    protected final Path resizedPicturesFolder;
    private final Path rootFolder;
    private final Set<Path> resizedPicturesSet;
    private final Object lock = new Object();

    public ResizedImageGenerator(FileSystem fileSystem, Path rootFolder, String resizedPicturesFolderName, int resizedPictureQuality, boolean forceResize) throws IOException {
        this.fileSystem = fileSystem;
        this.resizedPicturesFolder = this.fileSystem.getPath(resizedPicturesFolderName);
        this.resizedPicturesSet = new HashSet<>();
        this.forceResize = forceResize;

        this.resizedPictureQuality = resizedPictureQuality;

        if (!Files.exists(this.resizedPicturesFolder)) {
            Files.createDirectory(this.resizedPicturesFolder);
        }
        this.rootFolder = rootFolder;

        this.initAndCleanOutdatedResizedPictures();
    }

    /**
     * Generate a resizedPicture return true if the picture has been properly
     * regenerated, false if it already existed
     */
    protected final void generateResizedPicture(Path originalFilename, long lastModifiedTimestamp, int rotationId) throws IOException {
        Path path = this.getResizedPicturePath(originalFilename, lastModifiedTimestamp);

        synchronized (this.lock) {
            if (resizedPicturesSet.contains(path)) {
                return;
            }

            this.resizedPicturesSet.add(path);
        }

        BufferedImage originalImage = ImageHelper.readImage(originalFilename, rotationId);
        int newWidth = getResizedPictureWidth(originalImage.getWidth(), originalImage.getHeight());
        int newHeight = getResizedPictureHeight(originalImage.getWidth(), originalImage.getHeight());

        BufferedImage image;
        if (this.forceResize || (newWidth <= originalImage.getWidth() && newHeight <= originalImage.getHeight())) {
            image = ImageHelper.resizeImageSmooth(originalImage, newWidth, newHeight);
        } else {
            image = originalImage;
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            new JpegEncoder(image, this.resizedPictureQuality, out).Compress();
        }
    }

    protected final void deleteResizedPicture(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        synchronized (this.lock) {
            Path p = this.resizedPicturesFolder.resolve(getResizedPictureFilename(originalFilename, lastModifiedTimestamp));
            Files.deleteIfExists(p);
            this.resizedPicturesSet.remove(p);
        }
    }

    protected final Path getResizedPicturePath(Path originalFilename, long lastModifiedTimestamp) {
        return this.resizedPicturesFolder.resolve(getResizedPictureFilename(originalFilename, lastModifiedTimestamp));
    }

    protected final String getResizedPictureFilename(Path originalFilename, long lastModifiedTimestamp) {
        String src = originalFilename.toAbsolutePath() + "_" + lastModifiedTimestamp;
        return Md5.encodeString(src) + ".jpg";
    }

    protected abstract int getResizedPictureWidth(int originalWidth, int originalHeight);

    protected abstract int getResizedPictureHeight(int originalWidth, int originalHeight);

    private void initAndCleanOutdatedResizedPictures() {
        try {
            this.resizedPicturesSet.addAll(Files.list(this.resizedPicturesFolder).collect(Collectors.toSet()));
            Set<Path> toDeleteResizedPictures = new HashSet<>(this.resizedPicturesSet);

            Queue<Path> toExplore = new LinkedList<>();
            toExplore.add(this.rootFolder);
            while (!toExplore.isEmpty()) {
                File currentFolder = toExplore.remove().toFile();

                if (currentFolder.canRead() && !FileHelper.folderContainsIgnoreFile(currentFolder.toPath())) {
                    File[] files = currentFolder.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                toExplore.add(file.toPath());
                            } else {
                                Path p = this.resizedPicturesFolder.resolve(getResizedPictureFilename(file.toPath(), file.lastModified()));
                                toDeleteResizedPictures.remove(p);
                            }
                        }
                    }
                }
            }

            // ResizedPictures with no real picture anymore
            for (Path toDeleteResizedPicture : toDeleteResizedPictures) {
                toDeleteResizedPicture.toFile().delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
