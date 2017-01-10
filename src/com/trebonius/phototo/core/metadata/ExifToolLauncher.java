package com.trebonius.phototo.core.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class ExifToolLauncher {

    public static Map<String, String> run(Path filename) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        String applicationName = isWindows ? "exiftool.exe" : "exiftool";

        Map<String, String> result = new HashMap<>();

        String commandLine = applicationName + " \"" + filename.toString() + "\" -charset utf-8 -c \"%.8f\"";
        Process p = Runtime.getRuntime().exec(commandLine);

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
        BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), "UTF-8"));
        String line = null;

        try {
            while ((line = input.readLine()) != null) {
                String[] split = line.split(":", 2);
                result.put(split[0].trim(), split.length > 1 ? split[1].trim() : null);
            }
            while ((line = err.readLine()) != null) {
                System.err.println("Error for " + filename + ": " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }

        return result;
    }
}
