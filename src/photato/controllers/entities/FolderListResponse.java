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

    public FolderListResponse(Collection<PhotatoFolder> folders, Collection<PhotatoPicture> pictures) {
        this.folders = folders;
        this.pictures = pictures;
    }

}
