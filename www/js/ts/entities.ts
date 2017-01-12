class PhototoRequestResults {
    public folders: PhototoFolder[]
    public pictures: PhototoPicture[]
    public beginIndex: number;
    public endIndex: number;
    public hasMore: boolean;
}

class PictureInfos {
    public url: string;
    public width: number;
    public height: number;
}

class PhototoPicturePosition {
    public hardcodedPosition: string;
    public coordinatesDescription: string;
}

abstract class PhototoItem {
    public path: string;
    public thumbnail: PictureInfos;
}

class PhototoPicture extends PhototoItem {
    public title: string;

    public parentAndName: string;

    public tags: string[];

    public persons: string[];

    public position: PhototoPicturePosition;

    public picture: PictureInfos;

    public lastModificationTimestamp: number;

    public pictureCreationDate: number;

}

class PhototoFolder extends PhototoItem {
}