package photato.core;

import photato.core.SearchManager;
import java.nio.file.Path;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoPicture;
import photato.core.entities.PictureInfos;
import photato.core.metadata.Metadata;
import photato.core.metadata.gps.Position;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class SearchManagerTest {

    @Test
    public void test() {
        Path rootFolderPath = Paths.get("/home/myself/images");
        PhotatoFolder rootFolder = new PhotatoFolder(rootFolderPath, rootFolderPath);
        PhotatoFolder newYorkFolder = new PhotatoFolder(rootFolderPath, Paths.get("/home/myself/images/new-york"));
        PhotatoFolder canadaFolder = new PhotatoFolder(rootFolderPath, Paths.get("/home/myself/images/cacadada"));
        rootFolder.subFolders.put("new-york", newYorkFolder);
        rootFolder.subFolders.put("canada", canadaFolder);

        SearchManager searchManager = new SearchManager(true, true);

        Metadata metadata1 = new Metadata();
        metadata1.persons = new String[]{"Pierre-Arthur Edouard", "Moi", "Antoine Wolololo Ching"};
        metadata1.tags = new String[]{"Central park", "Manhattan"};
        metadata1.position = new Position(7d, 4d, "Central Park, New-York, États-Unis d'Amérique", null);
        PhotatoPicture photatoPicture1 = new PhotatoPicture(rootFolderPath, Paths.get("/home/myself/images/new-york/my awesome picture1.jpg"), metadata1, new PictureInfos("456454", 0, 0, 0), 546435435);

        Metadata metadata2 = new Metadata();
        metadata2.persons = new String[]{"Pierre-Arthur Edouard", "Antoine Prout"};
        metadata2.tags = new String[]{"Mont Royal"};
        metadata2.position = new Position(7d, 4d, "Montréal, Québec, Canada", null);
        PhotatoPicture photatoPicture2 = new PhotatoPicture(rootFolderPath, Paths.get("/home/myself/images/cacadada/cool me.png"), metadata2, new PictureInfos("fds", 0, 0, 0), 546435435);

        Metadata metadata3 = new Metadata();
        metadata3.persons = new String[]{};
        metadata3.tags = new String[]{};
        metadata3.position = new Position(7d, 4d, null, "waldos");
        PhotatoPicture photatoPicture3 = new PhotatoPicture(rootFolderPath, Paths.get("/home/myself/images/empty/empty.png"), metadata3, new PictureInfos("fds", 0, 0, 0), 546435435);

        searchManager.addPicture(rootFolder, photatoPicture1);
        searchManager.addPicture(rootFolder, photatoPicture2);
        searchManager.addPicture(rootFolder, photatoPicture3);

        // Search by folder name
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "canada").size());
        Assert.assertEquals(photatoPicture2, searchManager.searchPictureInFolder(rootFolder.fsPath, "canada").get(0));
        Assert.assertEquals(1, searchManager.searchPictureInFolder(canadaFolder.fsPath, "canada").size());
        Assert.assertEquals(photatoPicture2, searchManager.searchPictureInFolder(canadaFolder.fsPath, "canada").get(0));

        // Search by file name        
        Assert.assertEquals(1, searchManager.searchPictureInFolder(rootFolder.fsPath, "awesome").size());
        Assert.assertEquals(photatoPicture1, searchManager.searchPictureInFolder(rootFolder.fsPath, "awesome").get(0));
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
