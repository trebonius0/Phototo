package photato;

import java.nio.file.FileSystem;
import photato.core.PhotatoFilesManager;
import photato.core.metadata.MetadataAggregator;
import photato.core.resize.thumbnails.ThumbnailGenerator;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import photato.core.resize.fullscreen.FullScreenImageGetter;
import photato.core.metadata.gps.IGpsCoordinatesDescriptionGetter;
import photato.controllers.CssHandler;
import photato.controllers.DefaultHandler;
import photato.controllers.FolderListHandler;
import photato.controllers.ImageHandler;
import photato.controllers.JsHandler;
import photato.core.metadata.exif.ExifToolDownloader;
import photato.core.metadata.gps.OSMGpsCoordinatesDescriptionGetter;
import java.nio.file.Path;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import photato.controllers.FullScreenImageHandler;
import photato.core.resize.ffmpeg.FfmpegDownloader;

public class Photato {

    public static final String[] supportedPictureExtensions = new String[]{"jpg", "jpeg", "png", "bmp"};
    public static final String[] supportedVideoExtensions = new String[]{"mp4", "webm"};
    private static final String serverName = "Photato/1.0";
    private static final String thumbnailCacheFolder = "cache/thumbnails";
    private static final String fullscreenCacheFolder = "cache/fullscreen";
    private static final String extractedPicturesCacheFolder = "cache/extracted";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: <picturesRootFolder>");
            System.exit(-1);
        }

        FileSystem fileSystem = FileSystems.getDefault();
        Path rootFolder = getRootFolder(fileSystem, args[0]);
        System.out.println("Starting exploration of folder " + rootFolder + "...");

        if (!Files.exists(fileSystem.getPath("cache"))) {
            Files.createDirectory(fileSystem.getPath("cache"));
        }

        HttpClient httpClient = HttpClientBuilder.create().setUserAgent(serverName).build();

        ExifToolDownloader.run(httpClient, fileSystem, PhotatoConfig.forceFfmpegToolsDownload);
        FfmpegDownloader.run(httpClient, fileSystem, PhotatoConfig.forceExifToolsDownload);

        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(fileSystem, rootFolder, thumbnailCacheFolder, extractedPicturesCacheFolder, PhotatoConfig.thumbnailHeight, PhotatoConfig.thumbnailQuality);
        IGpsCoordinatesDescriptionGetter gpsCoordinatesDescriptionGetter = new OSMGpsCoordinatesDescriptionGetter(httpClient, PhotatoConfig.addressElementsCount);
        MetadataAggregator metadataGetter = new MetadataAggregator(fileSystem, "cache/metadata.cache", gpsCoordinatesDescriptionGetter);
        FullScreenImageGetter fullScreenImageGetter = new FullScreenImageGetter(fileSystem, rootFolder, fullscreenCacheFolder, extractedPicturesCacheFolder, PhotatoConfig.fullScreenPictureQuality, PhotatoConfig.maxFullScreenPictureWitdh, PhotatoConfig.maxFullScreenPictureHeight);

        PhotatoFilesManager photatoFilesManager = new PhotatoFilesManager(rootFolder, fileSystem, metadataGetter, thumbnailGenerator, fullScreenImageGetter, PhotatoConfig.prefixModeOnly, PhotatoConfig.indexFolderName, PhotatoConfig.useParallelPicturesGeneration);

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(60000)
                .setTcpNoDelay(true)
                .build();

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(PhotatoConfig.serverPort)
                .setServerInfo(serverName)
                .setSocketConfig(socketConfig)
                .setExceptionLogger(new StdErrorExceptionLogger())
                .registerHandler(Routes.rawPicturesRootUrl + "/*", new ImageHandler(rootFolder, Routes.rawPicturesRootUrl))
                .registerHandler(Routes.fullScreenPicturesRootUrl + "/*", new FullScreenImageHandler(fileSystem.getPath(fullscreenCacheFolder), Routes.fullScreenPicturesRootUrl, fullScreenImageGetter, photatoFilesManager))
                .registerHandler(Routes.thumbnailRootUrl + "/*", new ImageHandler(fileSystem.getPath(thumbnailCacheFolder), Routes.thumbnailRootUrl))
                .registerHandler(Routes.listItemsApiUrl, new FolderListHandler(Routes.listItemsApiUrl, rootFolder, photatoFilesManager))
                .registerHandler("/img/*", new ImageHandler(fileSystem.getPath("www/img"), "/img"))
                .registerHandler("/js/*", new JsHandler(fileSystem.getPath("www/js"), "/js"))
                .registerHandler("/css/*", new CssHandler(fileSystem.getPath("www/css"), "/css"))
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

    private static Path getRootFolder(FileSystem fileSystem, String args0) {
        if (!args0.endsWith("/")) {
            args0 += "/";
        }
        return fileSystem.getPath(args0);
    }

}
