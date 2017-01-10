package com.trebonius.phototo.core;

import com.trebonius.phototo.helpers.SearchQueryHelper;
import com.trebonius.phototo.core.entities.PhototoFolder;
import com.trebonius.phototo.core.entities.PhototoPicture;
import com.trebonius.phototo.helpers.PartialStringIndex;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SearchManager {

    private final PartialStringIndex<PhototoPicture> picturesIndex;
    private final boolean indexFolderName;

    public SearchManager(boolean prefixOnlyMode, boolean indexFolderName) {
        this.picturesIndex = new PartialStringIndex<>(prefixOnlyMode);
        this.indexFolderName = indexFolderName;
    }

    public List<PhototoPicture> searchPictureInFolder(Path folder, String searchQuery) {
        List<String> searched = SearchQueryHelper.getSplittedTerms(searchQuery);

        if (searched.isEmpty()) {
            return new ArrayList<>();
        }

        List<List<PhototoPicture>> resultsTmp = searched.parallelStream()
                .map((String searchedTerm) -> this.picturesIndex.findContains(searchedTerm))
                .map((Collection<PhototoPicture> pictures) -> pictures.stream().filter((PhototoPicture phototoPicture) -> phototoPicture.fsPath.startsWith(folder)).collect(Collectors.toList()))
                .collect(Collectors.toList());

        List<PhototoPicture> result = resultsTmp.get(0);
        for (int i = 1; i < resultsTmp.size(); i++) {
            result = result.parallelStream().filter(resultsTmp.get(i)::contains).collect(Collectors.toList());
        }

        return result;
    }

    public void addPicture(PhototoFolder rootFolder, PhototoPicture picture) {
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
                all.addAll(Arrays.asList(picture.position.coordinatesDescription));
            }
        }

        for (String word : all) {
            List<String> terms = SearchQueryHelper.getSplittedTerms(word);
            for (String term : terms) {
                this.picturesIndex.add(term, picture);
            }
        }
    }

    public void removePicture(PhototoPicture picture) {
        this.picturesIndex.remove(picture);
    }

}
