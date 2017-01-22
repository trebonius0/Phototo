package photato.core.metadata;

import photato.core.metadata.exif.ExifMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import photato.core.metadata.gps.Position;
import photato.core.metadata.gps.GpsCoordinatesHelper;
import photato.core.metadata.gps.IGpsCoordinatesDescriptionGetter;
import photato.core.metadata.exif.ExifToolParser;
import photato.helpers.FileHelper;
import photato.helpers.SerialisationGsonBuilder;
import photato.helpers.Tuple;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataAggregator implements IMetadataAggregator, Closeable {

    private final FileSystem fileSystem;
    private final String metadataCacheFilename;
    private final IGpsCoordinatesDescriptionGetter coordinatesDescriptionGetter;
    private final Timer timer;
    private boolean hasNewInfos;
    private final Map<String, Metadata> metadatas;
    private final Object lock = new Object();

    public MetadataAggregator(FileSystem fileSystem, String metadataCacheFilename, IGpsCoordinatesDescriptionGetter coordinatesDescriptionGetter) {
        this.fileSystem = fileSystem;
        this.metadataCacheFilename = metadataCacheFilename;
        this.coordinatesDescriptionGetter = coordinatesDescriptionGetter;
        this.metadatas = readFromCache(this.fileSystem, metadataCacheFilename);

        this.timer = new Timer(false);
        this.startAutoSave();
    }

    @Override
    public Metadata getMetadata(Path path, long lastModifiedTimestamp) {
        List<Tuple<Path, Long>> paths = new ArrayList<>();
        paths.add(new Tuple<>(path, lastModifiedTimestamp));

        return getMetadatas(paths).get(path);
    }

    @Override
    public Map<Path, Metadata> getMetadatas(List<Tuple<Path, Long>> paths) {
        Map<Path, Metadata> result = new HashMap<>();

        // If in cache, returns cache info
        synchronized (this.lock) {
            List<Tuple<Path, Long>> remainingPaths = new ArrayList<>();
            for (Tuple<Path, Long> pathTuple : paths) {
                String key = getKey(pathTuple.o1, pathTuple.o2);
                if (this.metadatas.containsKey(key)) {
                    result.put(pathTuple.o1, this.metadatas.get(key));
                } else {
                    remainingPaths.add(pathTuple);
                }
            }

            paths = remainingPaths;
        }

        Map<Path, ExifMetadata> exifToolParserResults = ExifToolParser.readMetadata(paths.stream().map((Tuple<Path, Long> t) -> t.o1).collect(Collectors.toList()));

        for (Tuple<Path, Long> pathTuple : paths) {
            ExifMetadata exifMetadata = exifToolParserResults.get(pathTuple.o1);

            if (exifMetadata != null) {
                Position position;
                Tuple<Double, Double> coordinates = GpsCoordinatesHelper.getCoordinates(exifMetadata.getGPSPositionString());
                String hardcodedPosition = exifMetadata.getHardcodedPosition();

                if (this.coordinatesDescriptionGetter != null) {
                    position = new Position(coordinates.o1, coordinates.o2, hardcodedPosition, this.coordinatesDescriptionGetter.getCoordinatesDescription(coordinates.o1, coordinates.o2));
                } else {
                    position = new Position(coordinates.o1, coordinates.o2, hardcodedPosition, null);
                }

                Metadata metadata = new Metadata(exifMetadata, position);
                result.put(pathTuple.o1, metadata);

                synchronized (lock) {
                    this.metadatas.put(getKey(pathTuple.o1, pathTuple.o2), metadata);
                    this.hasNewInfos = true;
                }
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        this.timer.cancel();
    }

    private void startAutoSave() {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (hasNewInfos) {
                        hasNewInfos = false;
                        Gson g = new Gson();

                        try {
                            FileHelper.writeFile(fileSystem.getPath(metadataCacheFilename).toFile(), g.toJson(metadatas));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 20000);
    }

    private static Map<String, Metadata> readFromCache(FileSystem fileSystem, String metadataCacheFilename) {
        try {
            // Read metadata from cache
            String data = FileHelper.readFile(fileSystem.getPath(metadataCacheFilename).toFile());
            Map<String, Metadata> fromCacheFileMap = new Gson().fromJson(data, new TypeToken<Map<String, Metadata>>() {
            }.getType());

            // Remove outdated metadata
            Map<String, Metadata> resultMap = new HashMap<>();
            for (Map.Entry<String, Metadata> entry : fromCacheFileMap.entrySet()) {
                try {
                    String filenameLastModified = entry.getKey();
                    Metadata value = entry.getValue();

                    String filename = filenameLastModified.split("\\?")[0];
                    long lastModifiedTimestamp = Long.parseLong(filenameLastModified.split("\\?")[1].split("\\.")[0]);

                    File f = fileSystem.getPath(filename).toFile();
                    if (f.exists() && f.lastModified() == lastModifiedTimestamp) {
                        resultMap.put(filenameLastModified, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            System.out.println("Read " + resultMap.size() + " metadata from cache!");
            return resultMap;
        } catch (IOException ex) {
            System.err.println("Cannot read metadata from cache!");
            return new HashMap<>();
        }

    }

    private static String getKey(Path path, long lastModificationTimestamp) {
        return path + "?" + lastModificationTimestamp;
    }
}
