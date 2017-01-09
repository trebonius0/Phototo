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

            var imgPositionX = Math.round((windowWidth - imgWantedWidth) / 2);
            var imgPositionY = Math.round((windowHeight - imgWantedHeight) / 2);
            var legendPositionX = imgPositionX;
            var legendPositionY = imgPositionY + imgWantedHeight;
            var leftArrowPositionX = imgPositionX - arrowWidth;
            var rightArrowPositionX = imgPositionX + imgWantedWidth;
            var arrowPositionY = Math.round(imgPositionY + imgWantedHeight / 2 - arrowHeight / 2);

            $('#fullscreenPicture img').attr('width', imgWantedWidth).attr('height', imgWantedHeight).css('top', imgPositionY + 'px').css('left', imgPositionX + 'px');
            $('#fullscreenPicture .pictureLegend').css('width', (imgWantedWidth - 5) + 'px').css('top', legendPositionY + 'px').css('left', legendPositionX + 'px');
            $('#fullscreenPicture .left-arrow').css('top', arrowPositionY + 'px').css('left', leftArrowPositionX + 'px');
            $('#fullscreenPicture .right-arrow').css('top', arrowPositionY + 'px').css('left', rightArrowPositionX + 'px');

            var src = this.fullscreenPicture().picture.url;
            if (isMobileOrTablet()) {
                src += '?height=' + imgWantedHeight + '&width=' + imgWantedWidth;
            }
            $('.fullscreenPictureImg').attr('src', src);
        }
    }
}

function switchPictureIn() {
    $('.fullscreenPictureImg').css('z-index', 30);
    $('.fullscreenPictureThumbnail').css('z-index', 20);
}