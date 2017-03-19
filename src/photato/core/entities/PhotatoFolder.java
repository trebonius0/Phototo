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

    public final Set<PhotatoPicture> pictures;
    
    public final Set<PhotatoVideo> videos;

    public PhotatoFolder(Path rootFolder, Path path) {
        super(rootFolder, path);
        this.subFolders = new HashMap<>();
        this.pictures = new HashSet<>();
        this.videos = new HashSet<>();
    }

    @ExposeMethodResult("isEmpty")
    public boolean isEmpty() {
        if (!this.pictures.isEmpty()) {
            return false;
        } else {
            return this.subFolders.values().stream().noneMatch((folder) -> (!folder.isEmpty()));
        }
    }

    @ExposeMethodResult("thumbnail")
    public PictureInfos getThumbnail() {
        if (!this.pictures.isEmpty()) {
            // We try to find first horizontal thumbails, since they won't have a scaling problem when displayed
            for (PhotatoPicture picture : this.pictures) {
                if (picture.thumbnail.height < picture.thumbnail.width) {
                    return picture.thumbnail;
                }
            }

            return this.pictures.iterator().next().thumbnail;
        } else if(!this.videos.isEmpty()){
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
