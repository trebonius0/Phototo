package photato.core.entities;

import com.google.gson.annotations.Expose;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import photato.core.AlbumsManager;
import photato.core.metadata.Metadata;
import photato.core.metadata.gps.Position;
import photato.helpers.MediaHelper;

public abstract class PhotatoMedia extends PhotatoItem {

    @Expose
    public final String mediaType;

    @Expose
    public final String title;

    @Expose
    public String name;

    @Expose
    public final String[] tags;

    @Expose
    public final String[] persons;

    @Expose
    public final Position position;

    @Expose
    public final PictureInfos thumbnail;

    @Expose
    public final PictureInfos fullscreenPicture;

    public final long lastModificationTimestamp; // This field is used to generated the appropriate thumbnail. It is the modifiaction date on the filesystem

    @Expose
    public final long timestamp;

    public final Set<Path> virtualPaths;

    public PhotatoMedia(String mediaType, Path rootFolder, Path path, Metadata metadata, PictureInfos thumbnailInfos, PictureInfos fullScreenInfos, long lastModificationTimestamp) {
        super(rootFolder, path);
        this.mediaType = mediaType;
        this.title = metadata.title;
        this.tags = metadata.tags == null ? new String[]{} : metadata.tags;
        this.persons = metadata.persons == null ? new String[]{} : metadata.persons;
        this.position = metadata.position;
        this.thumbnail = thumbnailInfos;
        this.lastModificationTimestamp = lastModificationTimestamp;
        this.timestamp = metadata.pictureDate;
        this.fullscreenPicture = fullScreenInfos;

        this.virtualPaths = this.getMediaVirtualPaths();
    }

    public static PhotatoMedia createMedia(Path rootFolder, Path path, Metadata metadata, PictureInfos thumbnailInfos, PictureInfos fullScreenInfos, long lastModificationTimestamp) {
        if (MediaHelper.isPictureFile(path)) {
            return new PhotatoPicture(rootFolder, path, metadata, thumbnailInfos, fullScreenInfos, lastModificationTimestamp);
        } else if (MediaHelper.isVideoFile(path)) {
            return new PhotatoVideo(rootFolder, path, metadata, thumbnailInfos, fullScreenInfos, lastModificationTimestamp);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Set<Path> getMediaVirtualPaths() {
        Set<Path> result = new HashSet<>();

        if (this.persons != null) {
            for (String person : this.persons) {
                result.add(Paths.get("/" + AlbumsManager.albumsVirtualRootFolderName, AlbumsManager.personsFolderName, person));
            }
        }
        
        if (this.tags != null) {
            for (String tag : this.tags) {
                result.add(Paths.get("/" + AlbumsManager.albumsVirtualRootFolderName, AlbumsManager.tagsFolderName, tag));
            }
        }

        return result;
    }

}
