package photato.core.entities;

import io.gsonfire.annotations.ExposeMethodResult;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PhotatoFolder extends PhotatoItem {

    public final Map<String, PhotatoFolder> subFolders;

    public final Set<PhotatoMedia> medias;

    public final Set<PhotatoVideo> videos;

    public PhotatoFolder(Path rootFolder, Path path) {
        super(rootFolder, path);
        this.subFolders = new HashMap<>();
        this.medias = new HashSet<>();
        this.videos = new HashSet<>();
    }

    @ExposeMethodResult("isEmpty")
    public boolean isEmpty() {
        if (!this.medias.isEmpty()) {
            return false;
        } else {
            return this.subFolders.values().stream().noneMatch((folder) -> (!folder.isEmpty()));
        }
    }

    @ExposeMethodResult("thumbnail")
    public PictureInfos getThumbnail() {
        if (!this.medias.isEmpty()) {
            // We try to find first horizontal thumbails, since they won't have a scaling problem when displayed
            for (PhotatoMedia media : this.medias) {
                if (media.thumbnail.height < media.thumbnail.width) {
                    return media.thumbnail;
                }
            }

            return this.medias.iterator().next().thumbnail;
        } else if (!this.videos.isEmpty()) {
            return this.videos.iterator().next().thumbnail;
        } else {
            // If the folder does not contain pictures, will return the thumbail of one of its subfolders (if any is available)
            Optional<PhotatoFolder> folderOptional = this.subFolders.values().stream().filter((PhotatoFolder folder) -> !folder.isEmpty()).findAny();
            if (folderOptional.isPresent()) {
                return folderOptional.get().getThumbnail();
            } else {
                return null;
            }
        }
    }

}
