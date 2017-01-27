package photato.core;

import photato.helpers.SearchQueryHelper;
import photato.Photato;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoPicture;
import photato.core.entities.PictureInfos;
import photato.core.metadata.IMetadataAggregator;
import photato.core.metadata.Metadata;
import photato.helpers.FileHelper;
import photato.helpers.Tuple;
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
import java.util.stream.Stream;
import photato.core.resize.IResizedPictureGenerator;

public class PhotatoFilesManager implements Closeable {

    private final FileSystem fileSystem;
    private final IMetadataAggregator metadataAggregator;
    private final IResizedPictureGenerator thumbnailGenerator;
    private final PhotatoFolder rootFolder;
    private final SearchManager searchManager;
    private final WatchServiceThread watchServiceThread;
    private final Map<Path, WatchKey> watchedDirectoriesKeys;
    private final Map<WatchKey, Path> watchedDirectoriesPaths;
    private final boolean prefixOnlyMode;
    private final boolean useParallelThumbnailGeneration;

    public PhotatoFilesManager(Path rootFolder, FileSystem fileSystem, IMetadataAggregator metadataGetter, IResizedPictureGenerator thumbnailGenerator, boolean prefixOnlyMode, boolean indexFolderName, boolean useParallelThumbnailGeneration) throws IOException {
        this.fileSystem = fileSystem;
        this.metadataAggregator = metadataGetter;
        this.thumbnailGenerator = thumbnailGenerator;
        this.rootFolder = new PhotatoFolder(rootFolder, rootFolder);
        this.searchManager = new SearchManager(prefixOnlyMode, indexFolderName);
        this.prefixOnlyMode = prefixOnlyMode;
        this.useParallelThumbnailGeneration = useParallelThumbnailGeneration;

        WatchService watcher = this.fileSystem.newWatchService();
        this.watchedDirectoriesKeys = new HashMap<>();
        this.watchedDirectoriesPaths = new HashMap<>();
        this.runInitialFolderExploration(watcher, this.rootFolder);
        this.watchServiceThread = new WatchServiceThread(watcher);
        this.watchServiceThread.start();
    }

    public List<PhotatoFolder> getFoldersInFolder(String folder) {
        PhotatoFolder currentFolder = this.getCurrentFolder(this.fileSystem.getPath(folder));

        if (currentFolder != null) {
            return currentFolder.subFolders.values().stream().filter((PhotatoFolder f) -> !f.isEmpty()).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public List<PhotatoPicture> getPicturesInFolder(String folder) {
        PhotatoFolder currentFolder = this.getCurrentFolder(this.fileSystem.getPath(folder));

        if (currentFolder != null) {
            return new ArrayList<>(currentFolder.pictures);
        } else {
            return new ArrayList<>();
        }
    }

    public List<PhotatoPicture> searchPicturesInFolder(String folder, String searchQuery) {
        return this.searchManager.searchPictureInFolder(this.fileSystem.getPath(folder), searchQuery);
    }

    public List<PhotatoFolder> searchFoldersInFolder(String folder, String searchQuery) {
        // Search for a folder with the correct name. This is just a recursive exploration since we suppose the number of folders will be low enough and thus we would be able to "bruteforce" it
        List<String> searchQuerySplit = SearchQueryHelper.getSplittedTerms(searchQuery);
        List<PhotatoFolder> result = new ArrayList<>();

        if (!searchQuerySplit.isEmpty()) {
            PhotatoFolder currentFolder = this.getCurrentFolder(this.fileSystem.getPath(folder));

            Queue<PhotatoFolder> queue = new LinkedList<>();
            queue.add(currentFolder);

            while (!queue.isEmpty()) {
                currentFolder = queue.remove();
                queue.addAll(currentFolder.subFolders.values());

                if (!currentFolder.isEmpty()) {
                    List<String> currentFolderCleanedFilename = SearchQueryHelper.getSplittedTerms(currentFolder.filename);
                    boolean ok = searchQuerySplit.stream().allMatch((s) -> (currentFolderCleanedFilename.stream().anyMatch((String t) -> (prefixOnlyMode && t.startsWith(s)) || (!prefixOnlyMode && t.contains(s)))));

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

    private void runInitialFolderExploration(WatchService watcher, PhotatoFolder baseFolder) throws IOException {
        synchronized (this.rootFolder) {
            Queue<PhotatoFolder> foldersToExplore = new LinkedList<>();
            foldersToExplore.add(baseFolder);

            while (!foldersToExplore.isEmpty()) {
                PhotatoFolder currentFolder = foldersToExplore.remove();
                System.out.println("Exploring " + currentFolder);

                // Registering currentDirectory to watcher
                WatchKey key = currentFolder.fsPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                this.watchedDirectoriesKeys.put(currentFolder.fsPath, key);
                this.watchedDirectoriesPaths.put(key, currentFolder.fsPath);

                List<PhotatoFolder> folders = Files.list(currentFolder.fsPath)
                        .filter((Path path) -> Files.isReadable(path) && Files.isDirectory(path) && !FileHelper.folderContainsIgnoreFile(path))
                        .map((Path path) -> new PhotatoFolder(this.rootFolder.fsPath, path))
                        .collect(Collectors.toList());
                foldersToExplore.addAll(folders);

                for (PhotatoFolder folder : folders) {
                    currentFolder.subFolders.put(folder.fsPath.getFileName().toString(), folder);
                }

                // Renaming pictures with 2+ spaces in a row since this will cause trouble then
                Files.list(currentFolder.fsPath).forEach((Path path) -> {
                    if (path.getFileName().toString().contains("  ")) {
                        String newFilename = path.getFileName().toString().replaceAll("[ ]{2,}", " ");
                        Path newPath = path.resolveSibling(newFilename);
                        path.toFile().renameTo(newPath.toFile());

                        System.err.println("[WARNING] Renamed \"" + path + "\" to \"" + newPath + "\"");
                    }
                });

                // Extraction of pictures metadata
                Map<Path, Metadata> metadatas = this.metadataAggregator.getMetadatas(
                        Files.list(currentFolder.fsPath).parallel()
                                .filter((Path path) -> isPictureFile(path))
                                .map((path) -> new Tuple<>(path, tryGetLastModifiedTimestamp(path)))
                                .collect(Collectors.toList()));

                List<PhotatoPicture> pictures = metadatas.entrySet().parallelStream()
                        .map((Map.Entry<Path, Metadata> entry) -> new PhotatoPicture(this.rootFolder.fsPath, entry.getKey(), entry.getValue(), new PictureInfos(this.thumbnailGenerator.getResizedPictureUrl(entry.getKey(), tryGetLastModifiedTimestamp(entry.getKey())), this.thumbnailGenerator.getResizedPictureWidth(entry.getValue().width, entry.getValue().height), this.thumbnailGenerator.getResizedPictureHeight(entry.getValue().width, entry.getValue().height), 0), tryGetLastModifiedTimestamp(entry.getKey())))
                        .collect(Collectors.toList());

                pictures.forEach((PhotatoPicture picture) -> {
                    currentFolder.pictures.add(picture);
                    searchManager.addPicture(rootFolder, picture);
                });

                Stream<PhotatoPicture> thumbnailStream = this.useParallelThumbnailGeneration ? pictures.parallelStream() : pictures.stream(); // This could be a parallel stream. However, since thumbnail generation takes a lot of RAM, having it parallel would take too much ram (bad on small machines)
                thumbnailStream.forEach((PhotatoPicture picture) -> {
                    try {
                        thumbnailGenerator.generateResizedPicture(picture.fsPath, picture.lastModificationTimestamp, metadatas.get(picture.fsPath));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
    }

    private PhotatoFolder getCurrentFolder(Path path) {
        synchronized (this.rootFolder) {
            Path relativePath = this.rootFolder.fsPath.relativize(path);
            String[] elmnts = relativePath.toString().replace("\\", "/").split("/");

            if (elmnts.length == 1 && elmnts[0].isEmpty()) {
                return this.rootFolder;
            }

            PhotatoFolder currentFolder = this.rootFolder;
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
        String extension = FileHelper.getExtension(pathStr);
        for (String supportedExtension : Photato.supportedPictureExtensions) {
            if (supportedExtension.equalsIgnoreCase(extension)) {
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
            PhotatoFolder folder = getCurrentFolder(filename.getParent());
            if (folder.pictures.stream().noneMatch((PhotatoPicture p) -> p.fsPath.equals(filename))) {
                long lastModificationTimestamp = Files.getLastModifiedTime(filename).toMillis();
                Metadata metadata = metadataAggregator.getMetadata(filename, lastModificationTimestamp);
                PictureInfos thumbnailInfos = new PictureInfos(thumbnailGenerator.getResizedPictureUrl(filename, lastModificationTimestamp), thumbnailGenerator.getResizedPictureWidth(metadata.width, metadata.height), thumbnailGenerator.getResizedPictureHeight(metadata.width, metadata.height), 0);
                PhotatoPicture picture = new PhotatoPicture(rootFolder.fsPath, filename, metadataAggregator.getMetadata(filename, lastModificationTimestamp), thumbnailInfos, lastModificationTimestamp);
                folder.pictures.add(picture);
                searchManager.addPicture(rootFolder, picture);
                thumbnailGenerator.generateResizedPicture(picture.fsPath, picture.lastModificationTimestamp, metadata);
            }
        }

        private void manageDirectoryCreation(Path filename) throws IOException {
            PhotatoFolder newFolder = new PhotatoFolder(rootFolder.fsPath, filename);
            PhotatoFolder parentFolder = getCurrentFolder(filename.getParent());

            parentFolder.subFolders.put(filename.getFileName().toString(), newFolder);

            WatchKey key = filename.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            watchedDirectoriesKeys.put(filename, key);
            watchedDirectoriesPaths.put(key, filename);

            runInitialFolderExploration(watcher, newFolder);
        }

        private void manageFileDeletion(Path filename) throws IOException {
            PhotatoFolder folder = getCurrentFolder(filename.getParent());
            Optional<PhotatoPicture> findAny = folder.pictures.stream().filter((PhotatoPicture p) -> p.fsPath.equals(filename)).findAny();
            if (findAny.isPresent()) {
                PhotatoPicture picture = findAny.get();
                folder.pictures.remove(picture);
                searchManager.removePicture(picture);
                thumbnailGenerator.deleteResizedPicture(picture.fsPath, picture.lastModificationTimestamp);
            }
        }

        private void manageDirectoryDeletion(Path filename) throws IOException {
            PhotatoFolder parentFolder = getCurrentFolder(filename.getParent());
            parentFolder.subFolders.remove(filename.getFileName().toString());
            WatchKey removed = watchedDirectoriesKeys.remove(filename);
            if (removed != null) {
                removed.cancel();
                watchedDirectoriesPaths.remove(removed);
            }

            PhotatoFolder currentFolder = getCurrentFolder(filename);
            if (currentFolder.pictures != null) {
                for (PhotatoPicture picture : currentFolder.pictures) {
                    try {
                        searchManager.removePicture(picture);
                        thumbnailGenerator.deleteResizedPicture(picture.fsPath, picture.lastModificationTimestamp);
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
