package com.trebonius.phototo.core.metadata;

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
import com.trebonius.phototo.core.thumbnails.IThumbnailGenerator;
import com.trebonius.phototo.core.gps.GpsCoordinatesHelper;
import com.trebonius.phototo.core.gps.IGpsCoordinatesDescriptionGetter;
import com.trebonius.phototo.helpers.FileHelper;
import com.trebonius.phototo.helpers.SafeSimpleDateFormat;
import com.trebonius.phototo.helpers.Tuple;

public class MetadataGetter implements IMetadataGetter, Closeable {

    private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private static final String personTagKey = "Region Person Display Name";
    private static final String legendTagKey = "Title";
    private static final String keywordsTagKey = "Subject";
    private static final String createDateTagKey = "Create Date";
    private static final String createDateTagKey2 = "Modify Date";
    private static final String imageWidthTagKey = "Image Width";
    private static final String imageHeightTagKey = "Image Height";
    private static final String createdLocalisationTagKey1 = "Location Created City";
    private static final String createdLocalisationTagKey2 = "Location Created Province State";
    private static final String createdLocalisationTagKey3 = "Location Created Country Name";
    private static final String gpsPositionTagKey = "GPS Position";
    private final FileSystem fileSystem;
    private final String metadataCacheFilename;
    private final IGpsCoordinatesDescriptionGetter coordinatesDescriptionGetter;
    private final Timer timer;
    private boolean hasNewInfos;
    private final Map<String, Metadata> newMetadata;
    private final Map<String, Metadata> previousMetadata;
    private final Object lock = new Object();

    public MetadataGetter(FileSystem fileSystem, String metadataCacheFilename, IGpsCoordinatesDescriptionGetter coordinatesDescriptionGetter) {
        this.fileSystem = fileSystem;
        this.metadataCacheFilename = metadataCacheFilename;
        this.coordinatesDescriptionGetter = coordinatesDescriptionGetter;
        this.newMetadata = new HashMap<>();

        this.timer = new Timer(false);

        this.previousMetadata = readFromCache(this.fileSystem);
    }

    @Override
    public Metadata getMetadata(Path path, long lastModificationTimestamp, IThumbnailGenerator thumbnailGenerator) {
        String key = getKey(path, lastModificationTimestamp);

        synchronized (lock) {
            if (this.previousMetadata.containsKey(key)) {
                this.newMetadata.put(key, this.previousMetadata.get(key));
                this.hasNewInfos = true;
                return this.newMetadata.get(key);
            } else if (this.newMetadata.containsKey(key)) {
                return this.newMetadata.get(key);
            }
        }

        Map<String, String> exifToolLauncherResult;

        try {
            exifToolLauncherResult = ExifToolLauncher.run(path);
        } catch (IOException ex) {
            ex.printStackTrace();
            exifToolLauncherResult = new HashMap<>();
        }

        Metadata metadata = new Metadata();
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

        metadata.thumbnailHeight = thumbnailGenerator.getThumbnailHeight(metadata.width, metadata.height);
        metadata.thumbnailWidth = thumbnailGenerator.getThumbnailWidth(metadata.width, metadata.height);

        Tuple<Double, Double> coordinates = GpsCoordinatesHelper.getCoordinates(exifToolLauncherResult.get(gpsPositionTagKey));
        String hardcodedPosition = exifToolLauncherResult.containsKey(createdLocalisationTagKey1) ? (exifToolLauncherResult.get(createdLocalisationTagKey1) + " " + exifToolLauncherResult.get(createdLocalisationTagKey2) + " " + exifToolLauncherResult.get(createdLocalisationTagKey3)) : null;

        if (this.coordinatesDescriptionGetter != null) {
            metadata.position = new Position(coordinates.o1, coordinates.o2, hardcodedPosition, this.coordinatesDescriptionGetter.getCoordinatesDescription(coordinates.o1, coordinates.o2));
        } else {
            metadata.position = new Position(coordinates.o1, coordinates.o2, hardcodedPosition, new String[0]);
        }

        synchronized (lock) {
            this.newMetadata.put(key, metadata);
            this.hasNewInfos = true;
        }

        return metadata;
    }

    @Override
    public void close() throws IOException {
        this.timer.cancel();
    }

    @Override
    public void startAutoSave() {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (hasNewInfos) {
                        hasNewInfos = false;
                        Gson g = new Gson();

                        try {
                            FileHelper.writeFile(fileSystem.getPath(metadataCacheFilename).toFile(), g.toJson(newMetadata));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 20000);
    }

    private Map<String, Metadata> readFromCache(FileSystem fileSystem) {
        try {
            String data = FileHelper.readFile(fileSystem.getPath(this.metadataCacheFilename).toFile());
            Map<String, Metadata> res = new Gson().fromJson(data, new TypeToken<Map<String, Metadata>>() {
            }.getType());
            System.out.println("Read " + res.size() + " metadata from cache!");
            return res;
        } catch (IOException ex) {
            System.err.println("Cannot read metadata from cache!");
            return new HashMap<>();
        }

    }

    private static String getKey(Path path, long lastModificationTimestamp) {
        return path + "_" + lastModificationTimestamp;
    }
}
