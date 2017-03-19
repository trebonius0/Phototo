class PhotatoRequestResults {
    public folders: PhotatoFolder[];
    public medias: PhotatoMedia[];
}

class PictureInfos {
    public url: string;
    public width: number;
    public height: number;
}

class PhotatoPicturePosition {
    public hardcodedPosition: string;
    public coordinatesDescription: string;
}

abstract class PhotatoItem {
    public thumbnail: PictureInfos;
}

class PhotatoMedia extends PhotatoItem {
    public mediaType: string;

    public title: string;

    public name: string;

    public tags: string[];

    public persons: string[];

    public position: PhotatoPicturePosition;

    public fullscreenPicture: PictureInfos;

    public timestamp: number;
}

class PhotatoPicture extends PhotatoMedia {
    public rawPicture: PictureInfos;

}

class PhotatoVideo extends PhotatoMedia {
    public videoType: string;
    public videoPath: string;
    public filesize: number;
}

class PhotatoFolder extends PhotatoItem {
}