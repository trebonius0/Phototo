package phototo.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class FileHelper {

    public synchronized static String readFile(String filename) throws IOException {
        return readFile(new File(filename));
    }

    public synchronized static String readFile(File filename) throws IOException {

        StringBuilder result = new StringBuilder();

        InputStream ips = new FileInputStream(filename);
        InputStreamReader ipsr = new InputStreamReader(ips);
        try (BufferedReader br = new BufferedReader(ipsr)) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    public synchronized static String[] readFileLines(File filename) throws IOException {

        List<String> result = new ArrayList<>();

        InputStream ips = new FileInputStream(filename);
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipsr);
        String line;
        while ((line = br.readLine()) != null) {
            result.add(line);
        }
        br.close();

        return result.toArray(new String[result.size()]);
    }

    public synchronized static void writeFile(File filename, String data, boolean gzip) throws IOException {
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(filename);

            if (gzip) {
                outputStream = new GZIPOutputStream(outputStream);
            }

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
                writer.write(data);
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public synchronized static void writeFile(File file, byte[] data) throws IOException {
        file.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }

    }

    public synchronized static void writeFile(File filename, String data) throws IOException {
        writeFile(filename, data, false);
    }

    public synchronized static void writeLines(File filename, String[] lines, boolean gzip) throws IOException {
        writeFile(filename, String.join("\n", lines), gzip);
    }

    public synchronized static void writeLines(File filename, Collection<String> lines, boolean gzip) throws IOException {
        writeFile(filename, String.join("\n", lines), gzip);
    }

    public synchronized static void writeLines(File filename, String[] lines) throws IOException {
        writeFile(filename, String.join("\n", lines));
    }

    public synchronized static void writeLines(File filename, Collection<String> lines) throws IOException {
        writeFile(filename, String.join("\n", lines));
    }

    public synchronized static void appendFile(File filename, String data) throws IOException {

        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename, true))) {
            out.write(data);
        }
    }

    public synchronized static void appendFileLine(File filename, String data) throws IOException {
        appendFile(filename, data + "\n");
    }

    public static String getExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }

}
