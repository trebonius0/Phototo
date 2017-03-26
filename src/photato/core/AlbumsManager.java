package photato.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoMedia;
import photato.helpers.SearchQueryHelper;

public class AlbumsManager {

    public static final String albumsVirtualRootFolderName = "Albums";
    public static final String personsFolderName = "Persons";
    public static final String tagsFolderName = "Tags";
    public static final String placesFolderName = "Places";
    private static final Path virtualRootPath = Paths.get("/");

    private final PhotatoFolder virtualRootFolder;

    public AlbumsManager() {
        this.virtualRootFolder = new PhotatoFolder(virtualRootPath, virtualRootPath);
    }

    public synchronized void addMedia(PhotatoMedia media) {
        for (Path virtualPath : media.virtualPaths) {
            PhotatoFolder currentFolder = this.virtualRootFolder;

            for (int i = 0; i < virtualPath.getNameCount(); i++) {
                String folderName = virtualPath.getName(i).toString();
                String normalizedFolderName = SearchQueryHelper.normalizeString(folderName);

                PhotatoFolder folder = currentFolder.subFolders.get(normalizedFolderName);
                if (folder == null) {
                    folder = new PhotatoFolder(virtualRootPath, currentFolder.fsPath.resolve(folderName));
                    currentFolder.subFolders.put(normalizedFolderName, folder);
                }

                currentFolder = folder;
            }

            currentFolder.medias.add(media);
        }
    }

    public synchronized void removeMedia(PhotatoMedia media) {
        for (Path virtualPath : media.virtualPaths) {
            List<PhotatoFolder> foldersStack = new ArrayList<>();
            foldersStack.add(this.virtualRootFolder);

            for (int i = 0; i < virtualPath.getNameCount(); i++) {
                String folderName = virtualPath.getName(i).toString();

                PhotatoFolder folder = foldersStack.get(foldersStack.size() - 1).subFolders.get(folderName);
                if (folder == null) {
                    break;
                }

                foldersStack.add(folder);
            }

            // Remove from all parents folders
            for (int i = 0; i < foldersStack.size(); i++) {
                foldersStack.get(i).medias.remove(media);
            }

            // Remove empty folders
            for (int i = foldersStack.size() - 1; i >= 1; i--) {
                if (foldersStack.get(i).isEmpty()) {
                    foldersStack.get(i - 1).subFolders.remove(SearchQueryHelper.normalizeString(foldersStack.get(i).filename));
                }
            }
        }
    }

    public synchronized PhotatoFolder getCurrentFolder(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        Path relativePath = this.virtualRootFolder.fsPath.relativize(Paths.get(path));
        String[] elmnts = relativePath.toString().replace("\\", "/").split("/");

        if (elmnts.length == 1 && elmnts[0].isEmpty()) {
            return this.virtualRootFolder;
        }

        PhotatoFolder currentFolder = this.virtualRootFolder;
        for (int i = 0; i < elmnts.length; i++) {
            currentFolder = currentFolder.subFolders.get(SearchQueryHelper.normalizeString(elmnts[i]));

            if (currentFolder == null) {
                return null;
            }
        }

        return currentFolder;
    }

}
