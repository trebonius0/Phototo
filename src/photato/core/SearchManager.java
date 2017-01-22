package photato.core;

import photato.helpers.SearchQueryHelper;
import photato.core.entities.PhotatoFolder;
import photato.core.entities.PhotatoPicture;
import photato.helpers.PartialStringIndex;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchManager {

    private final PartialStringIndex<PhotatoPicture> picturesIndex;
    private final boolean indexFolderName;

    public SearchManager(boolean prefixOnlyMode, boolean indexFolderName) {
        this.picturesIndex = new PartialStringIndex<>(prefixOnlyMode);
        this.indexFolderName = indexFolderName;
    }

    public List<PhotatoPicture> searchPictureInFolder(Path folder, String searchQuery) {
        List<String> searched = SearchQueryHelper.getSplittedTerms(searchQuery);

        if (searched.isEmpty()) {
            return new ArrayList<>();
        }

        List<Set<PhotatoPicture>> resultsTmp = searched.parallelStream()
                .map((String searchedTerm) -> this.picturesIndex.findContains(searchedTerm))
                .map((Collection<PhotatoPicture> pictures) -> pictures.stream().filter((PhotatoPicture photatoPicture) -> photatoPicture.fsPath.startsWith(folder)).collect(Collectors.toSet()))
                .collect(Collectors.toList());

        Set<PhotatoPicture> result = resultsTmp.get(0);
        for (int i = 1; i < resultsTmp.size(); i++) {
            result = result.parallelStream().filter(resultsTmp.get(i)::contains).collect(Collectors.toSet());
        }

        return new ArrayList<>(result);
    }

    public void addPicture(PhotatoFolder rootFolder, PhotatoPicture picture) {
        String pictureName = picture.fsPath.getFileName().toString();
        pictureName = pictureName.substring(0, pictureName.lastIndexOf("."));

        List<String> all = new ArrayList<>();
        if (picture.persons != null) {
            all.addAll(Arrays.asList(picture.persons));
        }

        if (picture.tags != null) {
            all.addAll(Arrays.asList(picture.tags));
        }

        if (picture.title != null) {
            all.add(picture.title);
        }

        if (this.indexFolderName) {
            all.addAll(Arrays.asList(rootFolder.fsPath.relativize(picture.fsPath.getParent()).toString().replace("\\", "/").split("/")));
        }
        all.add(pictureName);

        if (picture.position != null) {
            if (picture.position.hardcodedPosition != null) {
                all.add(picture.position.hardcodedPosition);
            } else if (picture.position.coordinatesDescription != null) {
                all.addAll(SearchQueryHelper.getSplittedTerms(picture.position.coordinatesDescription));
            }
        }

        for (String word : all) {
            List<String> terms = SearchQueryHelper.getSplittedTerms(word);
            for (String term : terms) {
                this.picturesIndex.add(term, picture);
            }
        }
    }

    public void removePicture(PhotatoPicture picture) {
        this.picturesIndex.remove(picture);
    }

}
