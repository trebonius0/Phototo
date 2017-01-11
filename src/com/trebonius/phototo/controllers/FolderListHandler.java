package com.trebonius.phototo.controllers;

import com.trebonius.phototo.helpers.MyGsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import com.trebonius.phototo.core.PhototoFilesManager;
import com.trebonius.phototo.core.entities.PhototoFolder;
import com.trebonius.phototo.core.entities.PhototoPicture;
import com.trebonius.phototo.helpers.QueryStringHelper;
import java.util.Arrays;
import java.util.Map;

public class FolderListHandler implements HttpRequestHandler {

    private final String prefix;
    private final Path rootFolder;
    private final PhototoFilesManager phototoFilesManager;

    public FolderListHandler(String prefix, Path rootFolder, PhototoFilesManager phototoFilesManager) {
        this.prefix = prefix;
        this.rootFolder = rootFolder;
        this.phototoFilesManager = phototoFilesManager;
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase();
        if (!method.equals("GET")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String target = request.getRequestLine().getUri();
        if (!target.startsWith(this.prefix)) {
            throw new HttpException("Incorrect route");
        }

        Map<String, String> queryStringMap = QueryStringHelper.splitSearchQuery(target);
        if (queryStringMap.keySet().containsAll(Arrays.asList(new String[]{"folder", "beginIndex", "endIndex"}))) {
            String folderTmp = queryStringMap.get("folder");
            while (folderTmp.length() > 0 && folderTmp.startsWith("/")) { // Remove the leading slashes if needed
                folderTmp = folderTmp.substring(1);
            }

            String folder = this.rootFolder.resolve(folderTmp).toString();

            String query = queryStringMap.get("query"); // Can be null

            int beginIndex = Integer.parseInt(queryStringMap.get("beginIndex"));
            int endIndex = Integer.parseInt(queryStringMap.get("endIndex"));

            List<PhototoFolder> folders = beginIndex == 0 ? (query == null ? this.phototoFilesManager.getFoldersInFolder(folder) : this.phototoFilesManager.searchFoldersInFolder(folder, query)) : new ArrayList<>();
            List<PhototoPicture> pictures = query == null ? this.phototoFilesManager.getPicturesInFolder(folder) : this.phototoFilesManager.searchPicturesInFolder(folder, query);

            folders.sort((PhototoFolder f1, PhototoFolder f2) -> f1.filename.compareTo(f2.filename));
            pictures.sort((PhototoPicture p1, PhototoPicture p2) -> Long.compare(p1.lastModificationTimestamp, p2.lastModificationTimestamp));

            boolean hasMore;

            if (pictures.size() > beginIndex) {
                hasMore = Math.min(endIndex, pictures.size()) == endIndex;
                pictures = pictures.subList(beginIndex, Math.min(endIndex, pictures.size()));
            } else {
                hasMore = false;
                pictures = new ArrayList<>();
            }
            FolderListResponse result = new FolderListResponse(folders, pictures, beginIndex, endIndex, hasMore);

            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity body = new StringEntity(MyGsonBuilder.getGson().toJson(result), ContentType.create("application/json", "UTF-8"));
            response.setEntity(body);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            StringEntity entity = new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
        }
    }
}
