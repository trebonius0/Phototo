package phototo.controllers;

import phototo.controllers.entities.FolderListResponse;
import phototo.helpers.SerialisationGsonBuilder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import phototo.core.PhototoFilesManager;
import phototo.core.entities.PhototoFolder;
import phototo.core.entities.PhototoPicture;
import java.util.Arrays;
import java.util.Map;

public class FolderListHandler extends PhototoHandler {

    private final Path rootFolder;
    private final PhototoFilesManager phototoFilesManager;

    public FolderListHandler(String prefix, Path rootFolder, PhototoFilesManager phototoFilesManager) {
        super(prefix, new String[]{"GET"});
        this.rootFolder = rootFolder;
        this.phototoFilesManager = phototoFilesManager;
    }

    @Override
    protected Response getResponse(String path, Map<String, String> queryStringMap) throws Exception {
        if (queryStringMap.keySet().containsAll(Arrays.asList(new String[]{"folder", "beginIndex", "endIndex"}))) {
            String folderTmp = queryStringMap.get("folder");
            while (!folderTmp.isEmpty() && folderTmp.startsWith("/")) { // Remove the leading slashes if needed
                folderTmp = folderTmp.substring(1);
            }

            String folder = this.rootFolder.resolve(folderTmp).toString();

            String query = queryStringMap.get("query"); // Can be null

            int beginIndex = Integer.parseInt(queryStringMap.get("beginIndex"));
            int endIndex = Integer.parseInt(queryStringMap.get("endIndex"));

            List<PhototoFolder> folders = beginIndex == 0 ? (query == null ? this.phototoFilesManager.getFoldersInFolder(folder) : this.phototoFilesManager.searchFoldersInFolder(folder, query)) : new ArrayList<>();
            List<PhototoPicture> pictures = query == null ? this.phototoFilesManager.getPicturesInFolder(folder) : this.phototoFilesManager.searchPicturesInFolder(folder, query);

            folders.sort((PhototoFolder f1, PhototoFolder f2) -> f1.filename.compareTo(f2.filename));
            pictures.sort((PhototoPicture p1, PhototoPicture p2) -> {
                int c = Long.compare(p1.pictureCreationDate, p2.pictureCreationDate);
                if (c == 0) {
                    return p1.filename.compareTo(p2.filename);
                } else {
                    return c;
                }
            }
            );

            boolean hasMore;

            if (pictures.size() > beginIndex) {
                hasMore = Math.min(endIndex, pictures.size()) == endIndex;
                pictures = pictures.subList(beginIndex, Math.min(endIndex, pictures.size()));
            } else {
                hasMore = false;
                pictures = new ArrayList<>();
            }
            FolderListResponse result = new FolderListResponse(folders, pictures, beginIndex, endIndex, hasMore);

            return new Response(HttpStatus.SC_OK, new StringEntity(SerialisationGsonBuilder.getGson().toJson(result), ContentType.create("application/json", "UTF-8")));
        } else {
            return PhototoHandler.http404;
        }
    }
}
