package photato.core.resize.ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import photato.helpers.OsHelper;

public class FfmpegDownloader {

    private static final String tmpFilename = "ffmpeg.exe.tmp";
    private static final String targetFilename = "ffmpeg.exe";
    private static final long maxDelayBeforeRedownload = 30 * 86400 * 1000L;

    public static void run(HttpClient httpClient, FileSystem fileSystem, boolean forceFfmpegDownload) throws IOException {
        if (OsHelper.isWindows()) {
            String v = System.getProperty("sun.arch.data.model"); // 32 or 64 bits
            if (!fileSystem.getPath(targetFilename).toFile().exists()
                    || forceFfmpegDownload
                    || Files.getLastModifiedTime(fileSystem.getPath(targetFilename)).toMillis() + maxDelayBeforeRedownload < System.currentTimeMillis()) {
                System.out.println("Starting ffmpeg download");
                downloadFfmpegTools("https://ffmpeg.zeranoe.com/builds/win" + v + "/static/ffmpeg-latest-win" + v + "-static.zip", httpClient, fileSystem);
                System.out.println("End of ffmpeg download");
            }
        }
    }

    private static void downloadFfmpegTools(String ffmpegUrl, HttpClient httpClient, FileSystem fileSystem) throws IOException {
        HttpGet request = new HttpGet(ffmpegUrl);
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            // Download the zip file
            File tmpFile = fileSystem.getPath(tmpFilename).toFile();
            tmpFile.delete(); // Delete the tmp file in case it already exists
            try (InputStream inputStream = entity.getContent();
                    OutputStream outputStream = new FileOutputStream(tmpFile)) {
                IOUtils.copy(inputStream, outputStream);
            }

            // Unzip
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tmpFile))) {
                ZipEntry ze;

                do {
                    ze = zis.getNextEntry();
                } while (!ze.getName().endsWith("/ffmpeg.exe"));

                File newFile = fileSystem.getPath(targetFilename).toFile();
                newFile.delete(); // Delete in case it already exists

                byte[] buffer = new byte[4096];
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }

            // Delete .zipFile
            tmpFile.delete();
        }
    }

}
