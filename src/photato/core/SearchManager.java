package photato.core;

import photato.helpers.SearchQueryHelper;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoMedia;
import photato.helpers.PartialStringIndex;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchManager {

    private final PartialStringIndex<PhotatoMedia> mediasIndex;
    private final boolean indexFolderName;

    public SearchManager(boolean prefixOnlyMode, boolean indexFolderName) {
        this.mediasIndex = new PartialStringIndex<>(prefixOnlyMode);
        this.indexFolderName = indexFolderName;
    }

    public List<PhotatoMedia> searchMediasInFolder(Path folder, String searchQuery) {
        List<String> searched = SearchQueryHelper.getSplittedTerms(searchQuery);

        if (searched.isEmpty()) {
            return new ArrayList<>();
        }

        List<Set<PhotatoMedia>> resultsTmp = searched.parallelStream()
                .map((String searchedTerm) -> this.mediasIndex.findContains(searchedTerm))
                .map((Collection<PhotatoMedia> medias) -> medias.stream().filter((PhotatoMedia media) -> media.fsPath.startsWith(folder)).collect(Collectors.toSet()))
                .collect(Collectors.toList());

        Set<PhotatoMedia> result = resultsTmp.get(0);
        for (int i = 1; i < resultsTmp.size(); i++) {
            result = result.parallelStream().filter(resultsTmp.get(i)::contains).collect(Collectors.toSet());
        }

        return new ArrayList<>(result);
    }

    public void addMedia(PhotatoFolder rootFolder, PhotatoMedia media) {
        String pictureName = media.fsPath.getFileName().toString();
        pictureName = pictureName.substring(0, pictureName.lastIndexOf("."));

        List<String> all = new ArrayList<>();
        if (media.persons != null) {
            all.addAll(Arrays.asList(media.persons));
        }

        if (media.tags != null) {
            all.addAll(Arrays.asList(media.tags));
        }

        if (media.title != null) {
            all.add(media.title);
        }

        if (this.indexFolderName) {
            all.addAll(Arrays.asList(rootFolder.fsPath.relativize(media.fsPath.getParent()).toString().replace("\\", "/").split("/")));
        }
        all.add(pictureName);

        if (media.position != null && media.position.coordinatesDescription != null) {
            all.addAll(SearchQueryHelper.getSplittedTerms(media.position.coordinatesDescription));
        }

        for (String word : all) {
            List<String> terms = SearchQueryHelper.getSplittedTerms(word);
            for (String term : terms) {
                this.mediasIndex.add(term, media);
            }
        }
    }

    public void removeMedia(PhotatoMedia media) {
        this.mediasIndex.remove(media);
    }

}
