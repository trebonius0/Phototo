package com.trebonius.phototo.core.metadata;

import com.trebonius.phototo.core.metadata.exif.ExifMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.trebonius.phototo.core.gps.Position;
import com.trebonius.phototo.core.gps.GpsCoordinatesHelper;
import com.trebonius.phototo.core.gps.IGpsCoordinatesDescriptionGetter;
import com.trebonius.phototo.core.metadata.exif.ExifToolParser;
import com.trebonius.phototo.helpers.FileHelper;
import com.trebonius.phototo.helpers.MyGsonBuilder;
import com.trebonius.phototo.helpers.SafeSimpleDateFormat;
import com.trebonius.phototo.helpers.Tuple;
import java.io.File;

public class MetadataGetter implements IMetadataGetter, Closeable {

    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    private final FileSystem fileSystem;
    private final String metadataCacheFilename;
    private final IGpsCoordinatesDescriptionGetter coordinatesDescriptionGetter;
    private final Timer timer;
    private boolean hasNewInfos;
    private final Map<String, ExifMetadata> metadatas;
    private final Object lock = new Object();

    public MetadataGetter(FileSystem fileSystem, String metadataCacheFilename, IGpsCoordinatesDescriptionGetter coordinatesDescriptionGetter) {
        this.fileSystem = fileSystem;
        this.metadataCacheFilename = metadataCacheFilename;
        this.coordinatesDescriptionGetter = coordinatesDescriptionGetter;
        this.metadatas = readFromCache(this.fileSystem);

        this.timer = new Timer(false);
        this.startAutoSave();
    }

    @Override
    public ExifMetadata getMetadata(Path path, long lastModificationTimestamp) {
        String key = getKey(path, lastModificationTimestamp);

        synchronized (this.lock) {
            if (this.metadatas.containsKey(key)) {
                return this.metadatas.get(key);
            }
        }

        Map<String, String> exifToolLauncherResult;

        try {
            exifToolLauncherResult = ExifToolParser.getMetadata(path);
        } catch (IOException ex) {
            ex.printStackTrace();
            exifToolLauncherResult = new HashMap<>();
        }

        ExifMetadata metadata = new ExifMetadata();
        // Extraction of persons
        if (exifToolLauncherResult.containsKey(personTagKey)) {
            metadata.persons = Arrays.asList(exifToolLauncherResult.get(personTagKey).split(",")).stream().map((String person) -> person.trim()).toArray(String[]::new);
        }

        if (exifToolLauncherResult.containsKey(legendTagKey)) {
            metadata.title = exifToolLauncherResult.get(legendTagKey);
        }

        if (exifToolLauncherResult.containsKey(keywordsTagKey)) {
            metadata.tags = Arrays.asList(exifToolLauncherResult.get(keywordsTagKey).split(",")).stream().map((String t) -> t.trim()).toArray(String[]::new);
        }

        if (exifToolLauncherResult.containsKey(createDateTagKey)) {
            try {
                metadata.pictureCreationDate = DATE_FORMAT.parse(exifToolLauncherResult.get(createDateTagKey)).getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } else if (exifToolLauncherResult.containsKey(createDateTagKey2)) {
            try {
                metadata.pictureCreationDate = DATE_FORMAT.parse(exifToolLauncherResult.get(createDateTagKey2)).getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        if (exifToolLauncherResult.containsKey(imageWidthTagKey)) {
            metadata.width = Integer.parseInt(exifToolLauncherResult.get(imageWidthTagKey));
        }

        if (exifToolLauncherResult.containsKey(imageHeightTagKey)) {
            metadata.height = Integer.parseInt(exifToolLauncherResult.get(imageHeightTagKey));
        }

        Tuple<Double, Double> coordinates = GpsCoordinatesHelper.getCoordinates(exifToolLauncherResult.get(gpsPositionTagKey));
        String hardcodedPosition = exifToolLauncherResult.containsKey(createdLocalisationTagKey1) ? (exifToolLauncherResult.get(createdLocalisationTagKey1) + " " + exifToolLauncherResult.get(createdLocalisationTagKey2) + " " + exifToolLauncherResult.get(createdLocalisationTagKey3)) : null;

        if (this.coordinatesDescriptionGetter != null) {
            metadata.position = new Position(coordinates.o1, coordinates.o2, hardcodedPosition, this.coordinatesDescriptionGetter.getCoordinatesDescription(coordinates.o1, coordinates.o2));
        } else {
            metadata.position = new Position(coordinates.o1, coordinates.o2, hardcodedPosition, null);
        }

        synchronized (lock) {
            this.metadatas.put(key, metadata);
            this.hasNewInfos = true;
        }

        return metadata;
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

    private Map<String, ExifMetadata> readFromCache(FileSystem fileSystem) {
        try {
            // Read metadata from cache
            String data = FileHelper.readFile(fileSystem.getPath(this.metadataCacheFilename).toFile());
            Map<String, ExifMetadata> fromCacheFileMap = MyGsonBuilder.getGson().fromJson(data, new TypeToken<Map<String, ExifMetadata>>() {
            }.getType());

            // Remove outdated metadata
            Map<String, ExifMetadata> resultMap = new HashMap<>();
            for (Map.Entry<String, ExifMetadata> entry : fromCacheFileMap.entrySet()) {
                try {
                    String filenameLastModified = entry.getKey();
                    ExifMetadata value = entry.getValue();

                    String filename = filenameLastModified.split("_")[0];
                    long lastModifiedTimestamp = Long.parseLong(filenameLastModified.split("_")[1]);

                    File f = fileSystem.getPath(filename).toFile();
                    if (f.exists() && f.lastModified() == lastModifiedTimestamp) {
                        resultMap.put(filenameLastModified, value);
                    }
                } catch (Exception ex) {
                    System.err.println(ex);
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
        return path + "_" + lastModificationTimestamp;
    }
}
