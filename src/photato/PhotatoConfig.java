package photato;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import org.ini4j.Ini;

public class PhotatoConfig {

    private static final String configFile = "photato.ini";
    public static int serverPort;
    public static boolean prefixModeOnly;
    public static boolean indexFolderName;
    public static boolean useParallelPicturesGeneration;
    public static boolean forceExifToolsDownload;
    public static Long resizedPicturesCacheMaxSize;
    public static int fullScreenPictureQuality;
    public static int maxFullScreenPictureWitdh;
    public static int maxFullScreenPictureHeight;
    public static int thumbnailHeight;
    public static int thumbnailQuality;

    static {
        try {
            Ini ini = new Ini(new File(configFile));
            serverPort = Integer.parseInt(ini.get("global", "serverPort"));
            prefixModeOnly = Boolean.parseBoolean(ini.get("index", "prefixModeOnly"));
            indexFolderName = Boolean.parseBoolean(ini.get("index", "indexFolderName"));
            useParallelPicturesGeneration = Boolean.parseBoolean(ini.get("thumbnail", "useParallelPicturesGeneration"));
            forceExifToolsDownload = Boolean.parseBoolean(ini.get("global", "forceExifToolsDownload"));

            try {
                resizedPicturesCacheMaxSize = Long.parseLong(ini.get("fullscreen", "resizedPicturesCacheMaxSize"));
            } catch (NumberFormatException ex) {
                resizedPicturesCacheMaxSize = null;
            }
            
            fullScreenPictureQuality = Integer.parseInt(ini.get("fullscreen", "fullScreenPictureQuality"));
            maxFullScreenPictureWitdh = Integer.parseInt(ini.get("fullscreen", "maxFullScreenPictureWitdh"));
            maxFullScreenPictureHeight = Integer.parseInt(ini.get("fullscreen", "maxFullScreenPictureHeight"));
            thumbnailHeight = Integer.parseInt(ini.get("thumbnail", "thumbnailHeight"));
            thumbnailQuality = Integer.parseInt(ini.get("thumbnail", "thumbnailQuality"));

        } catch (Exception ex) {
            throw new IllegalArgumentException("Incorrect config file : " + configFile + " - " + ex);
        }
    }
}
