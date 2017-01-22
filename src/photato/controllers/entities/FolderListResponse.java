package photato.controllers.entities;

import com.google.gson.annotations.Expose;
import java.util.Collection;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoPicture;

public class FolderListResponse {

    @Expose
    public final Collection<PhotatoFolder> folders;

    @Expose
    public final Collection<PhotatoPicture> pictures;

    @Expose
    public final int beginIndex;

    @Expose
    public final int endIndex;

    @Expose
    public final boolean hasMore;

    public FolderListResponse(Collection<PhotatoFolder> folders, Collection<PhotatoPicture> pictures, int beginIndex, int endIndex, boolean hasMore) {
        this.folders = folders;
        this.pictures = pictures;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.hasMore = hasMore;
    }

}
