class PhotatoRequestResults {
    public folders: PhotatoFolder[]
    public pictures: PhotatoPicture[]
    public beginIndex: number;
    public endIndex: number;
    public hasMore: boolean;
}

class PictureInfos {
    public url: string;
    public width: number;
    public height: number;
    public rotationId: number;
}

class PhotatoPicturePosition {
    public hardcodedPosition: string;
    public coordinatesDescription: string;
}

abstract class PhotatoItem {
    public path: string;
    public thumbnail: PictureInfos;
}

class PhotatoPicture extends PhotatoItem {
    public title: string;

    public parentAndName: string;

    public tags: string[];

    public persons: string[];

    public position: PhotatoPicturePosition;

    public picture: PictureInfos;

    public pictureDate: number;

}

class PhotatoFolder extends PhotatoItem {
}