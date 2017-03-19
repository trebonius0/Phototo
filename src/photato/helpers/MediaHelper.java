package photato.helpers;

import java.nio.file.Path;
import photato.Photato;

public class MediaHelper {

    public static boolean isPictureFile(Path path) {
        String pathStr = path.toString().toLowerCase();
        String extension = FileHelper.getExtension(pathStr);
        for (String supportedExtension : Photato.supportedPictureExtensions) {
            if (supportedExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVideoFile(Path path) {
        String pathStr = path.toString().toLowerCase();
        String extension = FileHelper.getExtension(pathStr);
        for (String supportedExtension : Photato.supportedVideoExtensions) {
            if (supportedExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }
}
