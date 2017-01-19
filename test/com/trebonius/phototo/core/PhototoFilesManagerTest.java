package com.trebonius.phototo.core;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.trebonius.phototo.core.entities.PhototoFolder;
import com.trebonius.phototo.core.entities.PhototoPicture;
import com.trebonius.phototo.core.metadata.IMetadataAggregator;
import com.trebonius.phototo.core.metadata.Metadata;
import com.trebonius.phototo.core.thumbnails.IThumbnailGenerator;
import com.trebonius.phototo.helpers.Tuple;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class PhototoFilesManagerTest {

    private static class ThumbnailsGeneratorMock implements IThumbnailGenerator {

        private static final Set<String> thumbnails = new HashSet<>();

        @Override
        public void generateThumbnail(Path originalFilename, long lastModifiedTimestamp) {
            thumbnails.add(originalFilename.toString());
        }

        @Override
        public void deleteThumbnail(Path originalFilename, long lastModifiedTimestamp) {
            thumbnails.remove(originalFilename.toString());
        }

        public boolean contains(Path originalFilename, long lastModifiedTimestamp) {
            return thumbnails.contains(originalFilename.toString());
        }

        @Override
        public void cleanOutdated() {
        }

        @Override
        public String getThumbnailUrl(Path originalFilename, long lastModifiedTimestamp) {
            return "/thumbnail/" + originalFilename + "_" + lastModifiedTimestamp;
        }

        @Override
        public int getThumbnailWidth(int originalWidth, int originalHeight) {
            return 10;
        }

        @Override
        public int getThumbnailHeight(int originalWidth, int originalHeight) {
            return 20;
        }

    }

    private static class MetadataGetterMock implements IMetadataAggregator {

        private final Map<String, List<String>> map = new HashMap<>();

        public void addMetadata(Path path, String metadata) {
            List<String> l = this.map.get(path.toString());
            if (l == null) {
                l = new ArrayList<>();
                this.map.put(path.toString(), l);
            }

            l.add(metadata);
        }

        @Override
        public Metadata getMetadata(Path path, long lastModificationTimestamp) {
            Metadata metadata = new Metadata();

            List<String> tagsList = this.map.get(path.toString());
            metadata.tags = tagsList == null ? new String[0] : tagsList.toArray(new String[tagsList.size()]);
            metadata.persons = new String[0];

            return metadata;
        }

        @Override
        public Map<Path, Metadata> getMetadatas(List<Tuple<Path, Long>> paths) {
            Map<Path, Metadata> result = new HashMap<>();
            for (Tuple<Path, Long> pathTuple : paths) {
                result.put(pathTuple.o1, this.getMetadata(pathTuple.o1, pathTuple.o2));
            }
            
            return result;
        }

    }

    @Test
    public void test() throws Exception {
        String rootFolder = "/home/myself/images";
        int sleepDelayForWaitingWatcherThread = 5000;

        MetadataGetterMock metadataGetterMock = new MetadataGetterMock();
        ThumbnailsGeneratorMock thumbnailsGeneratorMock = new ThumbnailsGeneratorMock();

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Files.deleteIfExists(fileSystem.getPath(rootFolder));
            Files.createDirectories(fileSystem.getPath(rootFolder));
            Files.createDirectory(fileSystem.getPath(rootFolder + "/new-york"));
            Files.createDirectories(fileSystem.getPath(rootFolder + "/cacadada/titi"));
            Files.createDirectory(fileSystem.getPath(rootFolder + "/tmptmp"));

            Path phototoPicture1 = fileSystem.getPath("/home/myself/images/new-york/my awesome picture1.jpg");
            Path phototoPicture2 = fileSystem.getPath("/home/myself/images/cacadada/titi/cool me.png");
            Files.createFile(phototoPicture1);
            Files.createFile(phototoPicture2);

            metadataGetterMock.addMetadata(phototoPicture1, "Pierre-Arthur Edouard");
            metadataGetterMock.addMetadata(phototoPicture1, "Moi");
            metadataGetterMock.addMetadata(phototoPicture1, "Antoine Wolololo Ching");
            metadataGetterMock.addMetadata(phototoPicture1, "Central park");
            metadataGetterMock.addMetadata(phototoPicture1, "Manhattan");
            metadataGetterMock.addMetadata(phototoPicture1, "New-York");
            metadataGetterMock.addMetadata(phototoPicture1, "États-Unis d'Amérique");

            metadataGetterMock.addMetadata(phototoPicture2, "Pierre-Arthur Edouard");
            metadataGetterMock.addMetadata(phototoPicture2, "Antoine Prout");
            metadataGetterMock.addMetadata(phototoPicture2, "Mont Royal");
            metadataGetterMock.addMetadata(phototoPicture2, "Montréal");
            metadataGetterMock.addMetadata(phototoPicture2, "Québec");
            metadataGetterMock.addMetadata(phototoPicture2, "Canada");

            try (PhototoFilesManager phototoFilesManager = new PhototoFilesManager(fileSystem.getPath(rootFolder), fileSystem, metadataGetterMock, thumbnailsGeneratorMock, true, true, false)) {
                // TEST SEARCH
                List<PhototoPicture> res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "pierre-arthur");
                Assert.assertEquals(2, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images/cacadada/titi", "pierre-arthur");
                Assert.assertEquals(1, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images/cacadada", "pierre-arthur");
                Assert.assertEquals(1, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "cacadada");
                Assert.assertEquals(1, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "titi");
                Assert.assertEquals(1, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images/tmptmp", "pierre-arthur");
                Assert.assertEquals(0, res.size());

                // TEST DIRECTORY LISTING
                List<PhototoFolder> foldersInFolder = phototoFilesManager.getFoldersInFolder("/home/myself/images");
                Assert.assertEquals(2, foldersInFolder.size()); // Not counting empty folders
                foldersInFolder = phototoFilesManager.getFoldersInFolder("/home/myself/cacadada/titi"); // Parent folder
                Assert.assertEquals(0, foldersInFolder.size());
                foldersInFolder = phototoFilesManager.getFoldersInFolder("/home/myself/images/cacadada");
                Assert.assertEquals(1, foldersInFolder.size());
                List<PhototoPicture> picturesInFolder = phototoFilesManager.getPicturesInFolder("/home/myself/images/cacadada/titi");
                Assert.assertEquals(1, picturesInFolder.size());

                // TEST THUMBNAIL GENERATION
                Assert.assertTrue(thumbnailsGeneratorMock.contains(phototoPicture1, 0));
                Assert.assertTrue(thumbnailsGeneratorMock.contains(phototoPicture2, 0));

                // TEST ONLINE FILE ADDITION
                Path phototoPicture3 = fileSystem.getPath("/home/myself/images/cacadada/santa.jpg"); // Picture with no metadata
                Files.createFile(phototoPicture3);
                Thread.sleep(sleepDelayForWaitingWatcherThread);
                picturesInFolder = phototoFilesManager.getPicturesInFolder("/home/myself/images/cacadada");
                foldersInFolder = phototoFilesManager.getFoldersInFolder("/home/myself/images/cacadada");
                Assert.assertEquals(1, picturesInFolder.size());
                Assert.assertEquals(1, foldersInFolder.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "santa");
                Assert.assertEquals(1, res.size());
                Assert.assertTrue(thumbnailsGeneratorMock.contains(phototoPicture3, 0));

                // TEST ONLINE FILE DELETION
                Files.delete(phototoPicture3);
                Thread.sleep(sleepDelayForWaitingWatcherThread);
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "santa");
                Assert.assertEquals(0, res.size());
                Assert.assertFalse(thumbnailsGeneratorMock.contains(phototoPicture3, 0));

                // TEST ONLINE FOLDER CREATION
                Path phototoPicture4_0 = fileSystem.getPath("/home/myself/images/carry fisher");
                Path phototoPicture4_1 = fileSystem.getPath("/home/myself/images/carry fisher/vador.jpg");
                Files.createDirectory(phototoPicture4_0);
                Files.createFile(phototoPicture4_1);
                Thread.sleep(sleepDelayForWaitingWatcherThread);
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "carry fisher");
                Assert.assertEquals(1, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "vador");
                Assert.assertEquals(1, res.size());
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images/carry fisher", "vador");
                Assert.assertEquals(1, res.size());
                Assert.assertTrue(thumbnailsGeneratorMock.contains(phototoPicture4_1, 0));

                // TEST ONLINE MODIFICATION
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "yoda");
                Assert.assertEquals(0, res.size());
                metadataGetterMock.addMetadata(phototoPicture4_1, "yoda");
                Files.setLastModifiedTime(phototoPicture4_1, FileTime.fromMillis(System.currentTimeMillis()));
                Thread.sleep(sleepDelayForWaitingWatcherThread);
                res = phototoFilesManager.searchPicturesInFolder("/home/myself/images", "yoda");
                Assert.assertEquals(1, res.size());
            }
        }
    }

}
