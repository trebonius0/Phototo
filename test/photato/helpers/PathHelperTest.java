package photato.helpers;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class PathHelperTest {

    @Test
    public void testPathHelper() {
        Tuple<String, Map<String, String>> r = PathHelper.splitPathAndQuery("//a/b//é/c");
        Assert.assertEquals("a/b//é/c", r.o1);
        Assert.assertEquals(0, r.o2.size());
        
        r = PathHelper.splitPathAndQuery("to%3Fto+%26+t%C3%A9t%C3%A9?w=4534&kfdsfdsfsd=pi%26pi%3Fta");
        Assert.assertEquals("to?to & tété", r.o1);
        Assert.assertEquals(2, r.o2.size());
        Assert.assertEquals("4534", r.o2.get("w"));
        Assert.assertEquals("pi&pi?ta", r.o2.get("kfdsfdsfsd"));
    }

}
