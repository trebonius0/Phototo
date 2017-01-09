package com.trebonius.phototo.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import com.trebonius.phototo.core.entities.PhototoItem;
import com.trebonius.phototo.core.entities.PhototoPicture;

public class FolderListHandler implements HttpRequestHandler {

    private static final Pattern queryStringPattern = Pattern.compile("query=([^&]*)");
    private static final Pattern folderPattern = Pattern.compile("folder=([^&]*)");
    private static final Pattern beginIndexPattern = Pattern.compile("beginIndex=([0-9]*)");
    private static final Pattern endIndexPattern = Pattern.compile("endIndex=([0-9]*)");
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

        Matcher mFolder = folderPattern.matcher(target);

        if (mFolder.find()) {
            String folderTmp = URLDecoder.decode(mFolder.group(1), "UTF-8");
            while (folderTmp.length() > 0 && folderTmp.startsWith("/")) {
                folderTmp = folderTmp.substring(1);
            }

            String folder = this.rootFolder.resolve(folderTmp).toString();

            Matcher mQuery = queryStringPattern.matcher(target);
            String query = mQuery.find() ? URLDecoder.decode(mQuery.group(1), "UTF-8") : null;
            if (query != null && query.length() < 3) {
                query = null;
            }

            Matcher mBeginIndex = beginIndexPattern.matcher(target);
            Matcher mEndIndex = endIndexPattern.matcher(target);
            if (mBeginIndex.find() && mEndIndex.find()) {
                int beginIndex = Integer.parseInt(mBeginIndex.group(1));
                int endIndex = Integer.parseInt(mEndIndex.group(1));

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
                PhototoServerResponse result = new PhototoServerResponse(folders, pictures, beginIndex, endIndex, hasMore);

                response.setStatusCode(HttpStatus.SC_OK);
                StringEntity body = new StringEntity(MyGsonBuilder.getGson().toJson(result), ContentType.create("application/json", "UTF-8"));
                response.setEntity(body);
            } else {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                StringEntity entity = new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
            }
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            StringEntity entity = new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
        }

    }
}
