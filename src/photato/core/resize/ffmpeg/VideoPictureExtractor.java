package photato.core.resize.ffmpeg;

import java.nio.file.Path;
import photato.helpers.OsHelper;

public class VideoPictureExtractor {

    public static void extractPictureFromVideo(Path videoPath, Path outputPicturePath) {
        String applicationName = OsHelper.isWindows() ? "ffmpeg.exe" : "ffmpeg";
        try {
            String commandLine = applicationName + " -y -ss 0 -i \"" + videoPath + "\" -qscale:v 2 -vframes 1 \"" + outputPicturePath + "\"";
            Process p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
