package photato.helpers;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class FileHelperTest {

    @Test
    public void testGetExtension() {
        Assert.assertEquals("jpg", FileHelper.getExtension("/home/me/img/test.jpg"));
        Assert.assertEquals("ab", FileHelper.getExtension("/home/me/img/test.ab"));
        Assert.assertEquals("ab", FileHelper.getExtension("/home/me/img.toto/test.ab"));
        Assert.assertNull(FileHelper.getExtension("/home/me/img/test"));
    }

    @Test
    public void testFileIgnoreDetection() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Files.createDirectory(fileSystem.getPath("/data"));
            Files.createDirectory(fileSystem.getPath("/data/ok"));
            Files.createDirectory(fileSystem.getPath("/data/bad"));

            // Create ok file
            Files.createFile(fileSystem.getPath("/data/ok/toto"));

            // Create ignore file
            Files.createFile(fileSystem.getPath("/data/bad/.photatoignore"));

            Assert.assertFalse(FileHelper.folderContainsIgnoreFile(fileSystem.getPath("/data/ok")));
            Assert.assertTrue(FileHelper.folderContainsIgnoreFile(fileSystem.getPath("/data/bad")));
        }
    }

}
