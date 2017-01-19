package com.trebonius.phototo.core;

import java.nio.file.Path;
import com.trebonius.phototo.core.entities.PhototoFolder;
import com.trebonius.phototo.core.entities.PhototoPicture;
import com.trebonius.phototo.core.entities.PictureInfos;
import com.trebonius.phototo.core.metadata.Metadata;
import com.trebonius.phototo.core.metadata.gps.Position;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class SearchManagerTest {

    @Test
    public void test() {
        Path rootFolderPath = Paths.get("/home/myself/images");
        PhototoFolder rootFolder = new PhototoFolder(rootFolderPath, rootFolderPath);
        PhototoFolder newYorkFolder = new PhototoFolder(rootFolderPath, Paths.get("/home/myself/images/new-york"));
        PhototoFolder canadaFolder = new PhototoFolder(rootFolderPath, Paths.get("/home/myself/images/cacadada"));
        rootFolder.subFolders.put("new-york", newYorkFolder);
        rootFolder.subFolders.put("canada", canadaFolder);

        SearchManager searchManager = new SearchManager(true, true);

        Metadata metadata1 = new Metadata();
        metadata1.persons = new String[]{"Pierre-Arthur Edouard", "Moi", "Antoine Wolololo Ching"};
        metadata1.tags = new String[]{"Central park", "Manhattan"};
        metadata1.position = new Position(7d, 4d, "Central Park, New-York, États-Unis d'Amérique", null);
        PhototoPicture phototoPicture1 = new PhototoPicture(rootFolderPath, Paths.get("/home/myself/images/new-york/my awesome picture1.jpg"), metadata1, new PictureInfos("456454", 0, 0), 546435435);

        Metadata metadata2 = new Metadata();
        metadata2.persons = new String[]{"Pierre-Arthur Edouard", "Antoine Prout"};
        metadata2.tags = new String[]{"Mont Royal"};
        metadata2.position = new Position(7d, 4d, "Montréal, Québec, Canada", null);
        PhototoPicture phototoPicture2 = new PhototoPicture(rootFolderPath, Paths.get("/home/myself/images/cacadada/cool me.png"), metadata2, new PictureInfos("fds", 0, 0), 546435435);

        Metadata metadata3 = new Metadata();
        metadata3.persons = new String[]{};
        metadata3.tags = new String[]{};
        metadata3.position = new Position(7d, 4d, null, "waldos");
        PhototoPicture phototoPicture3 = new PhototoPicture(rootFolderPath, Paths.get("/home/myself/images/empty/empty.png"), metadata3, new PictureInfos("fds", 0, 0), 546435435);

        searchManager.addPicture(rootFolder, phototoPicture1);
        searchManager.addPicture(rootFolder, phototoPicture2);
        searchManager.addPicture(rootFolder, phototoPicture3);

        // Search by folder name
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "canada").size());
        Assert.assertEquals(phototoPicture2, searchManager.searchPictureInFolder(rootFolder.fsPath, "canada").get(0));
        Assert.assertEquals(1, searchManager.searchPictureInFolder(canadaFolder.fsPath, "canada").size());
        Assert.assertEquals(phototoPicture2, searchManager.searchPictureInFolder(canadaFolder.fsPath, "canada").get(0));

        // Search by file name        
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "awesome").size());
        Assert.assertEquals(phototoPicture1, searchManager.searchPictureInFolder(rootFolder.fsPath, "awesome").get(0));
        Assert.assertEquals(0, searchManager.searchPictureInFolder(rootFolder.fsPath, "jpg").size()); // Checking extension not in index
        Assert.assertEquals(0, searchManager.searchPictureInFolder(rootFolder.fsPath, "images").size()); // Checking root folder not in index
        Assert.assertEquals(0, searchManager.searchPictureInFolder(rootFolder.fsPath, "me").size()); // Checking too <3 chars words not in index

        // Search by person name
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath, "Pierre-Arthur").size());
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath, "pierre-ar").size());
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath, "Antoine").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "Antoine wololo").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(canadaFolder.fsPath, "Antoine").size());
        Assert.assertEquals(0, searchManager.searchPictureInFolder(canadaFolder.fsPath, "Antoine wololo").size());
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath, "Edouard").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "wololo").size());

        // Test AND
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath, "Pierre-Arthur").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "canada").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "Pierre-Arthur canada").size());

        // Search by POSITION
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "Québec").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "Waldos").size());

    }

}
