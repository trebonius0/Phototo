package com.trebonius.phototo;

import java.nio.file.FileSystem;
import com.trebonius.phototo.core.PhototoFilesManager;
import com.trebonius.phototo.core.metadata.MetadataGetter;
import com.trebonius.phototo.core.thumbnails.ThumbnailGenerator;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import com.trebonius.phototo.core.fullscreen.FullScreenImageEntityGetter;
import com.trebonius.phototo.core.gps.IGpsCoordinatesDescriptionGetter;
import com.trebonius.phototo.controllers.CssHandler;
import com.trebonius.phototo.controllers.DefaultHandler;
import com.trebonius.phototo.controllers.FolderListHandler;
import com.trebonius.phototo.controllers.ImageHandler;
import com.trebonius.phototo.controllers.JsHandler;
import com.trebonius.phototo.controllers.StdErrorExceptionLogger;

public class Phototo {

    public static final String[] supportedExtensions = new String[]{"jpg", "jpeg", "png", "bmp"};
    public static String thumbnailRootUrl = "/img/thumbnail";
    public static String fullSizePicturesRootUrl = "/img/fullsize";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: <picturesRootFolder>");
            System.exit(-1);
        }
        String rootFolder = args[0];
        boolean prefixModeOnly = true;
        boolean indexFolderName = false;
        System.out.println("Starting exploration of folder " + rootFolder + "...");

        FileSystem fileSystem = FileSystems.getDefault();
        if (!Files.exists(fileSystem.getPath("cache"))) {
            Files.createDirectory(fileSystem.getPath("cache"));
        }

        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(fileSystem, "cache/thumbnails");
        IGpsCoordinatesDescriptionGetter googleGpsCoordinatesDescriptionGetter = null;
        MetadataGetter metadataGetter = new MetadataGetter(fileSystem, "cache/metadata.cache", googleGpsCoordinatesDescriptionGetter);
        FullScreenImageEntityGetter fullScreenResizeGenerator = new FullScreenImageEntityGetter(fileSystem, "cache/fullsize");

        PhototoFilesManager phototoFilesManager = new PhototoFilesManager(rootFolder, fileSystem, metadataGetter, thumbnailGenerator, prefixModeOnly, indexFolderName);

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(60000)
                .setTcpNoDelay(true)
                .build();

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(8186)
                .setServerInfo("Phototo/1.0")
                .setSocketConfig(socketConfig)
                .setExceptionLogger(new StdErrorExceptionLogger())
                .registerHandler(fullSizePicturesRootUrl + "/*", new ImageHandler(fullSizePicturesRootUrl, fileSystem.getPath(rootFolder), fullScreenResizeGenerator))
                .registerHandler(thumbnailRootUrl + "/*", new ImageHandler(thumbnailRootUrl, fileSystem.getPath("cache/thumbnails"), null))
                .registerHandler("/api/list", new FolderListHandler("/api/list", fileSystem.getPath(rootFolder), phototoFilesManager))
                .registerHandler("/img/*", new ImageHandler("/img", fileSystem.getPath("www/img"), null))
                .registerHandler("/js/*", new JsHandler("/js", fileSystem.getPath("www/js")))
                .registerHandler("/css/*", new CssHandler("/css", fileSystem.getPath("www/css")))
                .registerHandler("*", new DefaultHandler(fileSystem.getPath("www")))
                .create();
        server.start();
        System.out.println("Server started on port " + server.getLocalPort());
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown(5, TimeUnit.SECONDS);
            }
        });
    }

}
