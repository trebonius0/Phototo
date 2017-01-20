package phototo.helpers;

import org.junit.Assert;
import org.junit.Test;

public class Md5Test {

    @Test
    public void testEncodeString() {
        Assert.assertEquals("5ef2803c1c3ba9408d09878325dbfac9", Md5.encodeString("gfdsmuoghmfns uoncd!&é"));
        Assert.assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5.encodeString(""));

        try {
            Md5.encodeString(null);
            Assert.fail();
        } catch (Exception ex) {
        }
    }

}
