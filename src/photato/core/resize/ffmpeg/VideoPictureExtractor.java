package photato.core.resize.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import photato.helpers.OsHelper;
import photato.helpers.RandomManager;

public class VideoPictureExtractor {

    public static void extractPictureFromVideo(Path videoPath, Path outputPicturePath) {
        String applicationName = OsHelper.isWindows() ? "ffmpeg.exe" : "ffmpeg";
        try {
            String commandLine = applicationName + " -y -ss 0 -i \"" + videoPath + "\" -qscale:v 2 -vframes 1 \"" + outputPicturePath + "\"";
            Process p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();

            if (!outputPicturePath.toFile().exists()) {
                throw new IOException("Error while executing: '" + commandLine + "' -- Output file does not exist");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Path extractPictureFromVideoWithRandomPath(Path videoPath, Path cacheFolder) {
        if (!Files.exists(cacheFolder)) {
            try {
                Files.createDirectories(cacheFolder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Path outputPicturePath = cacheFolder.resolve("tmp_" + RandomManager.nextInt() + ".jpg");
        extractPictureFromVideo(videoPath, outputPicturePath);
        return outputPicturePath;
    }
}
