package photato.core.metadata.exif;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import photato.helpers.SerialisationGsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import photato.helpers.FileHelper;
import photato.helpers.OsHelper;

public class ExifToolParser {

    private static final int batchSize = 50;

    public static Map<Path, ExifMetadata> readMetadata(List<Path> filenames) {
        String applicationName = OsHelper.isWindows() ? "exiftool.exe" : "exiftool";
        Map<Path, ExifMetadata> result = new ConcurrentHashMap<>();

        Stream<List<Path>> parallelStream = IntStream.range(0, (filenames.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> filenames.subList(i * batchSize, Math.min(filenames.size(), (i + 1) * batchSize)))
                .parallel(); // Splitting entry list into batches

        parallelStream.forEach((List<Path> filenamesSublist) -> {
            try {
                String filenamesCommandLineParameter = filenamesSublist.stream() // Concat all the filenames together, after surrounding them with quotes
                        .map((Path filename) -> "\"" + filename.toString() + "\"")
                        .collect(Collectors.joining(" "));

                String commandLine = applicationName + " " + filenamesCommandLineParameter + " -charset utf-8 " + (OsHelper.isWindows() ? "-charset filename=Latin" : "") + " -j -c \"%.8f\"";
                Process p = runCommandLine(commandLine);

                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), "UTF-8"));
                StringBuilder errBuilder = new StringBuilder();
                while ((line = err.readLine()) != null) {
                    errBuilder.append(line).append("\n");
                }
                p.waitFor();

                String resultString = builder.toString();
                if (resultString == null || resultString.isEmpty()) {
                    throw new Exception("Empty exiftool result. Error for commandline: '" + commandLine + "': " + errBuilder.toString());
                }

                List<ExifMetadata> metadataList = new Gson().fromJson(resultString, new TypeToken<List<ExifMetadata>>() {
                }.getType());
                result.putAll(metadataList.stream().collect(Collectors.toMap(x -> Paths.get(x.getSourceFile()), x -> x)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return result;
    }

    private static Process runCommandLine(String commandLine) throws IOException, InterruptedException {
        Process p;
        if (OsHelper.isWindows()) {
            p = Runtime.getRuntime().exec(commandLine);
        } else {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", commandLine);
            p = pb.start();
        }
        return p;
    }
}
