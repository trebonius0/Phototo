class FullScreenManager {

    private fullscreenPicture: KnockoutComputed<PhototoPicture>;

    constructor(fullscreenPicture: KnockoutComputed<PhototoPicture>) {
        this.fullscreenPicture = fullscreenPicture;

        $(window).resize(() => this.loadImage());
    }

    public loadImage(): void {
        if (this.fullscreenPicture()) {
            $('.fullscreenPictureThumbnail').css('z-index', 30);
            $('.fullscreenPictureImg').css('z-index', 20);
            $('.fullscreenPictureImg').attr('src', '');

            var imageHeight: number = this.fullscreenPicture().picture.height;
            var imageWidth: number = this.fullscreenPicture().picture.width;
            var imageRotationId: number = this.fullscreenPicture().picture.rotationId;
            var windowHeight: number = window.innerHeight;
            var windowWidth: number = document.body.scrollWidth;
            var arrowWidth: number = $('#fullscreenPicture .arrow').first().outerWidth();
            var arrowHeight: number = $('#fullscreenPicture .arrow').first().outerHeight();

            var imgWantedWidth: number = windowWidth > 640 ? (0.80 * windowWidth) : (0.95 * windowWidth);
            var imgWantedHeight: number = 0.80 * windowHeight;

            var ratio1 = imgWantedWidth / imageWidth;
            var ratio2 = imgWantedHeight / imageHeight;

            if (ratio1 > 1 && ratio2 > 1) {
                imgWantedWidth = imageWidth;
                imgWantedHeight = imageHeight;
            } else if (ratio1 < ratio2) {
                imgWantedHeight = ratio1 * imageHeight;
            } else {
                imgWantedWidth = ratio2 * imageWidth;
            }

            imgWantedWidth = Math.round(imgWantedWidth);
            imgWantedHeight = Math.round(imgWantedHeight);

            var rotationAngle: number = isMobileOrTablet() ? 0 : this.getRotationAngle(imageRotationId);
            var imgRotatedWidth: number = rotationAngle == 0 || rotationAngle == 180 ? imgWantedWidth : imgWantedHeight;
            var imgRotatedHeight: number = rotationAngle == 0 || rotationAngle == 180 ? imgWantedHeight : imgWantedWidth;

            var imagePositionX = Math.round((windowWidth - imgWantedWidth) / 2);
            var imagePositionY = Math.round((windowHeight - imgWantedHeight) / 2);
            var rotatedImgPositionX = Math.round((windowWidth - imgRotatedWidth) / 2);
            var rotatedImgPositionY = Math.round((windowHeight - imgRotatedHeight) / 2);
            var legendPositionX = imagePositionX;
            var legendPositionY = imagePositionY + imgWantedHeight;
            var leftArrowPositionX = rotatedImgPositionX - arrowWidth;
            var rightArrowPositionX = rotatedImgPositionX + imgRotatedWidth;
            var arrowPositionY = Math.round(rotatedImgPositionY + imgRotatedHeight / 2 - arrowHeight / 2);

            $('.fullscreenPictureThumbnail').attr('width', imgWantedWidth).attr('height', imgWantedHeight).css('top', imagePositionY + 'px').css('left', imagePositionX + 'px');
            $('.fullscreenPictureImg').attr('width', imgRotatedWidth).attr('height', imgRotatedHeight).css('top', rotatedImgPositionY + 'px').css('left', rotatedImgPositionX + 'px');
            $('#fullscreenPicture .pictureLegend').css('width', (imgWantedWidth - 5) + 'px').css('top', legendPositionY + 'px').css('left', legendPositionX + 'px');
            $('#fullscreenPicture .left-arrow').css('top', arrowPositionY + 'px').css('left', leftArrowPositionX + 'px');
            $('#fullscreenPicture .right-arrow').css('top', arrowPositionY + 'px').css('left', rightArrowPositionX + 'px');

            var src = this.fullscreenPicture().picture.url;
            var rotationAngle: number;
            if (isMobileOrTablet()) {
                src += '?height=' + imgWantedHeight + '&width=' + imgWantedWidth + '&rotationId=' + imageRotationId;
                rotationAngle = 0;
            } else {
                rotationAngle = this.getRotationAngle(imageRotationId);
            }

            $('.fullscreenPictureImg').attr('src', src);

            if (rotationAngle > 0) {
                $('.fullscreenPictureImg').css('transform', 'rotate(' + rotationAngle + 'deg)');
            }
        }
    }

    private getRotationAngle(imageRotationId: number): number {
        // This method should be removed when the css attribute "image-rotation" will be available in all browsers
        switch (imageRotationId) {
            case 3:
                return 180;

            case 6:
                return 90;

            case 8:
                return 270;

            default:
                return 0;
        }
    }
}

function switchPictureIn() {
    $('.fullscreenPictureImg').css('z-index', 30);
    $('.fullscreenPictureThumbnail').css('z-index', 20);
}