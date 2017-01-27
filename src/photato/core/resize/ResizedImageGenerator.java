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
import photato.core.metadata.Metadata;
import photato.helpers.FileHelper;
import photato.helpers.ImageHelper;
import photato.helpers.JpegEncoder;
import photato.helpers.Md5;

public abstract class ResizedImageGenerator implements IResizedPictureGenerator {

    private final String routeRoot;
    private final int resizedPictureQuality;
    private final boolean forceResize;
    private final FileSystem fileSystem;
    private final Path resizedPicturesFolderName;
    private final Path rootFolder;
    private final Set<Path> resizedPicturesSet;
    private final Object lock = new Object();

    public ResizedImageGenerator(FileSystem fileSystem, Path rootFolder, String resizedPicturesFolderName, int resizedPictureQuality, boolean forceResize, String routeRoot) throws IOException {
        this.fileSystem = fileSystem;
        this.resizedPicturesFolderName = this.fileSystem.getPath(resizedPicturesFolderName);
        this.resizedPicturesSet = new HashSet<>();
        this.routeRoot = routeRoot;
        this.forceResize = forceResize;

        this.resizedPictureQuality = resizedPictureQuality;

        if (!Files.exists(this.resizedPicturesFolderName)) {
            Files.createDirectory(this.resizedPicturesFolderName);
        }
        this.rootFolder = rootFolder;

        this.initAndCleanOutdatedResizedPictures();
    }

    @Override
    public void generateResizedPicture(Path originalFilename, long lastModifiedTimestamp, Metadata metadata) throws IOException {
        Path path = this.resizedPicturesFolderName.resolve(getResizedPictureFilename(originalFilename, lastModifiedTimestamp));

        synchronized (this.lock) {
            if (resizedPicturesSet.contains(path)) {
                return;
            }

            this.resizedPicturesSet.add(path);
        }

        BufferedImage originalImage = ImageHelper.readImage(originalFilename, metadata.rotationId);
        int newWidth = getResizedPictureWidth(originalImage.getWidth(), originalImage.getHeight());
        int newHeight = getResizedPictureHeight(originalImage.getWidth(), originalImage.getHeight());

        if (this.forceResize || (newWidth <= originalImage.getWidth() && newHeight <= originalImage.getHeight())) {
            BufferedImage resized = ImageHelper.resizeImageSmooth(originalImage, newWidth, newHeight);

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
                new JpegEncoder(resized, resizedPictureQuality, out).Compress();
            }
        } else {
            Files.copy(originalFilename, path);
        }

    }

    @Override
    public void deleteResizedPicture(Path originalFilename, long lastModifiedTimestamp) throws IOException {
        synchronized (this.lock) {
            Path p = this.resizedPicturesFolderName.resolve(getResizedPictureFilename(originalFilename, lastModifiedTimestamp));
            Files.deleteIfExists(p);
            this.resizedPicturesSet.remove(p);
        }
    }

    @Override
    public final String getResizedPictureUrl(Path originalFilename, long lastModifiedTimestamp) {
        return this.routeRoot + "/" + this.getResizedPictureFilename(originalFilename, lastModifiedTimestamp);
    }

    protected final String getResizedPictureFilename(Path originalFilename, long lastModifiedTimestamp) {
        String src = originalFilename.toAbsolutePath() + "_" + lastModifiedTimestamp;
        return Md5.encodeString(src) + ".jpg";
    }

    private void initAndCleanOutdatedResizedPictures() {
        try {
            this.resizedPicturesSet.addAll(Files.list(this.resizedPicturesFolderName).collect(Collectors.toSet()));
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
                                Path p = this.resizedPicturesFolderName.resolve(getResizedPictureFilename(file.toPath(), file.lastModified()));
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
