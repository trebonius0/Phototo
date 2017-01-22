package photato.helpers;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class SearchQueryHelperTest {

    @Test
    public void testGetSplittedTerms() {
        String input = "ab à école l'enfant  ést Aujourd'hui ahBon! arc-en-ciel";
        List<String> splittedTerms = SearchQueryHelper.getSplittedTerms(input);
        Assert.assertTrue(splittedTerms.contains("ecole"));
        Assert.assertFalse(splittedTerms.contains("a"));
        Assert.assertFalse(splittedTerms.contains("ab"));
        Assert.assertFalse(splittedTerms.contains("en"));
        Assert.assertFalse(splittedTerms.contains("l"));
        Assert.assertTrue(splittedTerms.contains("enfant"));
        Assert.assertTrue(splittedTerms.contains("est"));
        Assert.assertTrue(splittedTerms.contains("aujourd"));
        Assert.assertTrue(splittedTerms.contains("hui"));
        Assert.assertTrue(splittedTerms.contains("ahbon"));
        Assert.assertTrue(splittedTerms.contains("arc"));
        Assert.assertTrue(splittedTerms.contains("ciel"));
        Assert.assertFalse(splittedTerms.contains(""));
        Assert.assertFalse(splittedTerms.contains(" "));
        Assert.assertFalse(splittedTerms.contains("!"));
        Assert.assertFalse(splittedTerms.contains("'"));
        Assert.assertFalse(splittedTerms.contains("à"));
        Assert.assertFalse(splittedTerms.contains("é"));

        List<String> splittedTerms2 = SearchQueryHelper.getSplittedTerms("  ");
        Assert.assertEquals(0, splittedTerms2.size());
    }

}
