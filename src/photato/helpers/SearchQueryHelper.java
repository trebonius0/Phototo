package photato.helpers;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchQueryHelper {

    /**
     * Given a list of words (which can be a search query), split them in tags 
     * relevant for the index, using multiple split, removing accentuation etc...
     * @param s the query
     * @return 
     */
    public static List<String> getSplittedTerms(String s) {
        return Arrays.asList(s.split("[ '\\,\\.-]")).parallelStream()
                .map((String str) -> normalizeString(str))
                .filter((String str) -> str.length() >= 3)
                .distinct()
                .collect(Collectors.toList());
    }
    
    public static String normalizeString(String str){
        return Normalizer.normalize(str.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^a-z0-9]+", "").trim();
    }
}
