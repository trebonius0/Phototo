package com.trebonius.phototo.core;

import com.trebonius.phototo.core.entities.PhototoFolder;
import com.trebonius.phototo.core.entities.PhototoPicture;
import com.trebonius.phototo.core.metadata.IMetadataGetter;
import com.trebonius.phototo.core.thumbnails.IThumbnailGenerator;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PhototoFilesManager implements Closeable {

    private static final String[] supportedExtensions = new String[]{".jpg", ".jpeg", ".png", ".bmp"};
    private final FileSystem fileSystem;
    private final IMetadataGetter metadataGetter;
    private final IThumbnailGenerator thumbnailGenerator;
    private final PhototoFolder rootFolder;
    private final SearchManager searchManager;
    private final WatchServiceThread watchServiceThread;
    private final Map<Path, WatchKey> watchedDirectoriesKeys;
    private final Map<WatchKey, Path> watchedDirectoriesPaths;

    public PhototoFilesManager(String rootFolder, FileSystem fileSystem, IMetadataGetter metadataGetter, IThumbnailGenerator thumbnailGenerator, boolean prefixOnlyMode, boolean indexFolderName) throws IOException {
        this.fileSystem = fileSystem;
        this.metadataGetter = metadataGetter;
        this.thumbnailGenerator = thumbnailGenerator;
        this.rootFolder = new PhototoFolder(this.fileSystem.getPath(rootFolder), this.fileSystem.getPath(rootFolder));
        this.searchManager = new SearchManager(prefixOnlyMode, indexFolderName);

        WatchService watcher = this.fileSystem.newWatchService();
        this.watchedDirectoriesKeys = new HashMap<>();
        this.watchedDirectoriesPaths = new HashMap<>();
        this.runInitialFolderExploration(watcher, this.rootFolder);
        this.watchServiceThread = new WatchServiceThread(watcher);
        this.watchServiceThread.start();
    }


    public List<PhototoFolder> getFoldersInFolder(String folder) {
        PhototoFolder currentFolder = this.getCurrentFolder(this.fileSystem.getPath(folder));

        if (currentFolder != null) {
            return currentFolder.subFolders.values().stream().filter((PhototoFolder f) -> !f.isEmpty()).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public List<PhototoPicture> getPicturesInFolder(String folder) {
        PhototoFolder currentFolder = this.getCurrentFolder(this.fileSystem.getPath(folder));

        if (currentFolder != null) {
            return new ArrayList<>(currentFolder.pictures);
        } else {
            return new ArrayList<>();
        }
    }


    public List<PhototoPicture> searchPicturesInFolder(String folder, String searchQuery) {
        return this.searchManager.searchPictureInFolder(folder, searchQuery);
    }

    public List<PhototoFolder> searchFoldersInFolder(String folder, String searchQuery) {
        List<String> searchQuerySplit = SearchQueryHelper.getSplittedTerms(searchQuery);
        List<PhototoFolder> result = new ArrayList<>();

        if (!searchQuerySplit.isEmpty()) {
            PhototoFolder currentFolder = this.getCurrentFolder(this.fileSystem.getPath(folder));

            Queue<PhototoFolder> queue = new LinkedList<>();
            queue.add(currentFolder);

            while (!queue.isEmpty()) {
                currentFolder = queue.remove();
                queue.addAll(currentFolder.subFolders.values());

                if (!currentFolder.isEmpty()) {
                    List<String> currentFolderCleanedFilename = SearchQueryHelper.getSplittedTerms(currentFolder.filename);
                    boolean ok = searchQuerySplit.stream().allMatch((s) -> (currentFolderCleanedFilename.stream().anyMatch((String t) -> t.startsWith(s))));

                    if (ok) {
                        result.add(currentFolder);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        this.watchServiceThread.shutdown();
        try {
            this.watchServiceThread.join();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    private void runInitialFolderExploration(WatchService watcher, PhototoFolder baseFolder) throws IOException {
        synchronized (this.rootFolder) {
            Queue<PhototoFolder> foldersToExplore = new LinkedList<>();
            foldersToExplore.add(baseFolder);

            while (!foldersToExplore.isEmpty()) {
                PhototoFolder currentFolder = foldersToExplore.remove();
                System.out.println("Exploring " + currentFolder);

                // Registering currentDirectory to watcher
                WatchKey key = currentFolder.fsPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                this.watchedDirectoriesKeys.put(currentFolder.fsPath, key);
                this.watchedDirectoriesPaths.put(key, currentFolder.fsPath);

                List<PhototoFolder> folders = Files.list(currentFolder.fsPath)
                        .filter((Path path) -> Files.isDirectory(path))
                        .map((Path path) -> new PhototoFolder(this.rootFolder.fsPath, path))
                        .collect(Collectors.toList());
                foldersToExplore.addAll(folders);

                // Renaming pictures with 2+ spaces in a row since this will cause trouble then
                Files.list(currentFolder.fsPath).forEach((Path path) -> {
                    if (path.getFileName().toString().contains("  ")) {
                        String newFilename = path.getFileName().toString().replaceAll("[ ]{2,}", " ");
                        Path newPath = path.resolveSibling(newFilename);
                        path.toFile().renameTo(newPath.toFile());

                        System.err.println("[WARNING] Renamed \"" + path + "\" to \"" + newPath + "\"");
                    }
                });

                List<PhototoPicture> pictures = Files.list(currentFolder.fsPath).parallel()
                        .filter((Path path) -> isPictureFile(path))
                        .map((Path path) -> new PhototoPicture(this.rootFolder.fsPath, path, this.metadataGetter.getMetadata(path, tryGetLastModifiedTimestamp(path), this.thumbnailGenerator), this.thumbnailGenerator.getThumbnailUrl(path, tryGetLastModifiedTimestamp(path)), tryGetLastModifiedTimestamp(path)))
                        .collect(Collectors.toList());

                for (PhototoFolder folder : folders) {
                    currentFolder.subFolders.put(folder.fsPath.getFileName().toString(), folder);
                }

                pictures.stream().forEach((PhototoPicture picture) -> { // This could be a parallel stream. However, since thumbnail generation takes a lot of RAM, having it parallel would take too much ram (bad on small machines)
                    try {
                        currentFolder.pictures.add(picture);
                        searchManager.addPicture(rootFolder, picture);
                        thumbnailGenerator.generateThumbnail(picture.fsPath, picture.lastModificationTimestamp);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            this.thumbnailGenerator.cleanOutdated();
            this.metadataGetter.startAutoSave();
        }
    }

    private PhototoFolder getCurrentFolder(Path path) {
        synchronized (this.rootFolder) {
            Path relativePath = this.rootFolder.fsPath.relativize(path);
            String[] elmnts = relativePath.toString().replace("\\", "/").split("/");

            if (elmnts.length == 1 && elmnts[0].isEmpty()) {
                return this.rootFolder;
            }

            PhototoFolder currentFolder = this.rootFolder;
            for (int i = 0; i < elmnts.length; i++) {
                currentFolder = currentFolder.subFolders.get(elmnts[i]);

                if (currentFolder == null) {
                    return null;
                }
            }

            return currentFolder;
        }
    }

    private static boolean isPictureFile(Path path) {
        String pathStr = path.toString().toLowerCase();
        for (String supportedExtension : supportedExtensions) {
            if (pathStr.endsWith(supportedExtension)) {
                return true;
            }
        }

        return false;
    }

    private class WatchServiceThread extends Thread {

        private final WatchService watcher;
        private boolean shouldRun;

        public WatchServiceThread(WatchService watchService) {
            super("WatchServiceThread");
            this.watcher = watchService;
            this.shouldRun = true;
        }

        @Override
        public void run() {
            while (this.shouldRun) {
                try {
                    WatchKey key = this.watcher.poll(100, TimeUnit.MILLISECONDS);

                    if (key != null) {
                        for (WatchEvent event : key.pollEvents()) {
                            try {
                                WatchEvent.Kind kind = event.kind();

                                if (kind == StandardWatchEventKinds.OVERFLOW) {
                                    continue;
                                } else {
                                    synchronized (rootFolder) {
                                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                                        Path folder = watchedDirectoriesPaths.get(key);
                                        if (folder == null) {
                                            throw new Exception("Unknown watchKey!");
                                        }

                                        Path filename = folder.resolve(ev.context());

                                        System.out.println("[" + (new Date()) + "] Detected event: " + kind + " on " + filename);
                                        if (Files.isDirectory(filename)) {
                                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                                this.manageDirectoryCreation(filename);
                                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                                this.manageDirectoryDeletion(filename);
                                            }
                                        } else if (isPictureFile(filename)) {
                                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                                this.manageFileCreation(filename);
                                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                                this.manageFileDeletion(filename);
                                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                                this.manageFileDeletion(filename);
                                                this.manageFileCreation(filename);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            System.err.println("Not valid key, breaking");
                            // break;
                        }
                    }
                } catch (InterruptedException ex) {
                }
            }
        }

        public void shutdown() {
            this.shouldRun = false;
        }

        private void manageFileCreation(Path filename) throws IOException {
            PhototoFolder folder = getCurrentFolder(filename.getParent());
            if (folder.pictures.stream().noneMatch((PhototoPicture p) -> p.fsPath.equals(filename))) {
                long lastModificationTimestamp = Files.getLastModifiedTime(filename).toMillis();
                PhototoPicture picture = new PhototoPicture(rootFolder.fsPath, filename, metadataGetter.getMetadata(filename, lastModificationTimestamp, thumbnailGenerator), thumbnailGenerator.getThumbnailUrl(filename, lastModificationTimestamp), lastModificationTimestamp);
                folder.pictures.add(picture);
                searchManager.addPicture(rootFolder, picture);
                thumbnailGenerator.generateThumbnail(picture.fsPath, picture.lastModificationTimestamp);
            }
        }

        private void manageDirectoryCreation(Path filename) throws IOException {
            PhototoFolder newFolder = new PhototoFolder(rootFolder.fsPath, filename);
            PhototoFolder parentFolder = getCurrentFolder(filename.getParent());

            parentFolder.subFolders.put(filename.getFileName().toString(), newFolder);

            WatchKey key = filename.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            watchedDirectoriesKeys.put(filename, key);
            watchedDirectoriesPaths.put(key, filename);

            runInitialFolderExploration(watcher, newFolder);
        }

        private void manageFileDeletion(Path filename) throws IOException {
            PhototoFolder folder = getCurrentFolder(filename.getParent());
            Optional<PhototoPicture> findAny = folder.pictures.stream().filter((PhototoPicture p) -> p.fsPath.equals(filename)).findAny();
            if (findAny.isPresent()) {
                PhototoPicture picture = findAny.get();
                folder.pictures.remove(picture);
                searchManager.removePicture(picture);
                thumbnailGenerator.deleteThumbnail(picture.fsPath, picture.lastModificationTimestamp);
            }
        }

        private void manageDirectoryDeletion(Path filename) throws IOException {
            PhototoFolder parentFolder = getCurrentFolder(filename.getParent());
            parentFolder.subFolders.remove(filename.getFileName().toString());
            WatchKey removed = watchedDirectoriesKeys.remove(filename);
            if (removed != null) {
                removed.cancel();
                watchedDirectoriesPaths.remove(removed);
            }

            PhototoFolder currentFolder = getCurrentFolder(filename);
            if (currentFolder.pictures != null) {
                for (PhototoPicture picture : currentFolder.pictures) {
                    try {
                        searchManager.removePicture(picture);
                        thumbnailGenerator.deleteThumbnail(picture.fsPath, picture.lastModificationTimestamp);
                    } catch (IOException ex) {
                    }
                }
            }
        }

    }

    private static long tryGetLastModifiedTimestamp(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
