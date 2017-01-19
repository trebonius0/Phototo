package phototo.core.metadata.exif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class ExifToolDownloader {

    private static final Pattern namePattern = Pattern.compile("http://owl\\.phy\\.queensu\\.ca/~phil/exiftool/exiftool-[0-9]+\\.[0-9]+\\.zip");
    private static final String exifToolRslUrl = "http://owl.phy.queensu.ca/~phil/exiftool/rss.xml";
    private static final String tmpFilename = "tmp.zip";
    private static final String targetFilename = "exiftool.exe";

    public static void run(HttpClient httpClient, FileSystem fileSystem, boolean forceExifToolsDownload) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        if (isWindows) {
            if (!fileSystem.getPath(targetFilename).toFile().exists() || forceExifToolsDownload) {
                System.out.println("Starting exifTools download");
                downloadExifTools(getExifToolsZipUrl(httpClient), httpClient, fileSystem);
                System.out.println("End of exifTools download");
            }
        }
    }

    private static String getExifToolsZipUrl(HttpClient httpClient) throws IOException {
        HttpGet request = new HttpGet(exifToolRslUrl);
        HttpResponse response = httpClient.execute(request);
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Matcher m = namePattern.matcher(result.toString());
            if (m.find()) {
                return m.group(0);
            } else {
                throw new IOException("Cannot find the exiftool url in the provided rss");
            }
        }
    }

    private static void downloadExifTools(String exifToolsUrl, HttpClient httpClient, FileSystem fileSystem) throws IOException {
        HttpGet request = new HttpGet(exifToolsUrl);
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
                ZipEntry ze = zis.getNextEntry();

                if (ze != null) {
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
            }

            // Delete .zipFile
            tmpFile.delete();
        }
    }

}
