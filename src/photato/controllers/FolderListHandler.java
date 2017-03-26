package photato.controllers;

import photato.controllers.entities.FolderListResponse;
import photato.helpers.SerialisationGsonBuilder;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import photato.core.PhotatoFilesManager;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoMedia;
import java.util.Map;

public class FolderListHandler extends PhotatoHandler {

    private final PhotatoFilesManager photatoFilesManager;

    public FolderListHandler(String prefix, PhotatoFilesManager photatoFilesManager) {
        super(prefix, new String[]{"GET"});
        this.photatoFilesManager = photatoFilesManager;
    }

    @Override
    protected Response getResponse(String path, Map<String, String> queryStringMap) throws Exception {
        if (queryStringMap.containsKey("folder")) {
            String folder = queryStringMap.get("folder");
            while (!folder.isEmpty() && folder.startsWith("/")) { // Remove the leading slashes if needed
                folder = folder.substring(1);
            }

            String query = queryStringMap.get("query"); // Can be null

            List<PhotatoFolder> folders = query == null ? this.photatoFilesManager.getFoldersInFolder(folder) : this.photatoFilesManager.searchFoldersInFolder(folder, query);
            List<PhotatoMedia> medias = query == null ? this.photatoFilesManager.getMediasInFolder(folder) : this.photatoFilesManager.searchMediasInFolder(folder, query);

            folders.sort((PhotatoFolder f1, PhotatoFolder f2) -> f1.filename.compareTo(f2.filename));
            medias.sort((PhotatoMedia m1, PhotatoMedia m2) -> {
                int c = Long.compare(m1.timestamp, m2.timestamp);
                if (c == 0) {
                    return m1.filename.compareTo(m2.filename);
                } else {
                    return c;
                }
            });

            FolderListResponse result = new FolderListResponse(folders, medias);

            return new Response(HttpStatus.SC_OK, new StringEntity(SerialisationGsonBuilder.getGson().toJson(result), ContentType.create("application/json", "UTF-8")));
        } else {
            return PhotatoHandler.http404;
        }
    }
}
