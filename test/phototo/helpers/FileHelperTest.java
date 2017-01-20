package phototo.helpers;

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

}
