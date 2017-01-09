package com.trebonius.phototo.core;

import java.nio.file.Path;
import com.trebonius.phototo.core.entities.PhototoFolder;
import com.trebonius.phototo.core.entities.PhototoPicture;
import com.trebonius.phototo.core.gps.Position;
import com.trebonius.phototo.core.metadata.Metadata;
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
        PhototoPicture phototoPicture1 = new PhototoPicture(rootFolderPath, Paths.get("/home/myself/images/new-york/my awesome picture1.jpg"), metadata1, "456454", 546435435);

        Metadata metadata2 = new Metadata();
        metadata2.persons = new String[]{"Pierre-Arthur Edouard", "Antoine Prout"};
        metadata2.tags = new String[]{"Mont Royal"};
        metadata2.position = new Position(7d, 4d, "Montréal, Québec, Canada", null);
        PhototoPicture phototoPicture2 = new PhototoPicture(rootFolderPath, Paths.get("/home/myself/images/cacadada/cool me.png"), metadata2, "fds", 546435435);

        Metadata metadata3 = new Metadata();
        metadata3.persons = new String[]{};
        metadata3.tags = new String[]{};
        metadata3.position = new Position(7d, 4d, null, new String[]{"waldos"});
        PhototoPicture phototoPicture3 = new PhototoPicture(rootFolderPath, Paths.get("/home/myself/images/empty/empty.png"), metadata3, "fds", 546435435);

        searchManager.addPicture(rootFolder, phototoPicture1);
        searchManager.addPicture(rootFolder, phototoPicture2);
        searchManager.addPicture(rootFolder, phototoPicture3);

        // Search by folder name
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "canada").size());
        Assert.assertEquals(phototoPicture2, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "canada").get(0));
        Assert.assertEquals(1, searchManager.searchPictureInFolder(canadaFolder.fsPath.toString(), "canada").size());
        Assert.assertEquals(phototoPicture2, searchManager.searchPictureInFolder(canadaFolder.fsPath.toString(), "canada").get(0));

        // Search by file name        
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "awesome").size());
        Assert.assertEquals(phototoPicture1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "awesome").get(0));
        Assert.assertEquals(0, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "jpg").size()); // Checking extension not in index
        Assert.assertEquals(0, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "images").size()); // Checking root folder not in index
        Assert.assertEquals(0, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "me").size()); // Checking too <3 chars words not in index

        // Search by person name
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Pierre-Arthur").size());
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "pierre-ar").size());
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Antoine").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Antoine wololo").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(canadaFolder.fsPath.toString(), "Antoine").size());
        Assert.assertEquals(0, searchManager.searchPictureInFolder(canadaFolder.fsPath.toString(), "Antoine wololo").size());
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Edouard").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "wololo").size());

        // Test AND
        Assert.assertEquals(2, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Pierre-Arthur").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "canada").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Pierre-Arthur canada").size());

        // Search by POSITION
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Québec").size());
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath.toString(), "Waldos").size());

    }

}
