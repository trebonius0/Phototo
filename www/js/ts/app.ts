/// <reference path="d/jquery.d.ts" />
/// <reference path="d/knockout.d.ts" />
/// <reference path="entities.ts" />
/// <reference path="breadcrumb.ts" />
/// <reference path="layout.ts" />
/// <reference path="historyState.ts" />
/// <reference path="messages.ts" />

class GalleryViewModel {
    private static batchSize: number = 50;
    public bannerMessage: KnockoutObservable<string>;
    public medias: KnockoutComputed<PhotatoMedia[]>;
    public folders: KnockoutObservableArray<PhotatoFolder>;
    public currentFolder: KnockoutObservable<string>;
    public currentFolderName: KnockoutComputed<string>;
    public currentSearchQuery: KnockoutObservable<string>;
    public currentPageTitle: KnockoutComputed<string>;
    public breadcrumbs: KnockoutComputed<Breadcrumb[]>;
    private layoutManager: LayoutManager;
    private currentAjaxRequest: any;
    private allMedias: KnockoutObservableArray<PhotatoMedia>;
    private displayedPicturesCount: KnockoutObservable<number>;

    constructor() {
        this.bannerMessage = ko.observable<string>("");
        this.currentFolder = ko.observable<string>("");
        this.currentFolderName = ko.computed<string>(() => this.currentFolder().substring(this.currentFolder().lastIndexOf("/") + 1) || "Photato gallery");

        this.currentFolder = ko.observable<string>("");
        this.currentSearchQuery = ko.observable<string>(null);
        this.displayedPicturesCount = ko.observable<number>(GalleryViewModel.batchSize);

        this.folders = ko.observableArray<PhotatoFolder>();
        this.allMedias = ko.observableArray<PhotatoMedia>();

        this.medias = ko.computed<PhotatoMedia[]>(() => {
            if (this.allMedias().length > this.displayedPicturesCount()) {
                return this.allMedias().slice(0, this.displayedPicturesCount());
            } else {
                return this.allMedias();
            }
        });


        this.breadcrumbs = ko.computed<Breadcrumb[]>(() => {
            var result: Breadcrumb[] = [new Breadcrumb("Home", "")];

            if (this.currentFolder() !== "" && this.currentFolder() !== "/") {
                var elmnts: string[] = this.currentFolder().split("/");

                for (var i = 0; i < elmnts.length; i++) {
                    var sub = elmnts.slice(0, i + 1);
                    var url = sub.join('/');
                    var title = elmnts[i];
                    var b = new Breadcrumb(title, url);
                    result.push(b);
                }
            }

            return result;
        });

        this.currentPageTitle = ko.computed<string>(() => this.currentSearchQuery() ? ("« " + this.currentSearchQuery() + " »") : this.currentFolderName());

        var searched: string = GalleryViewModel.getUrlParameter("search");
        var currentFolder: string = decodeURIComponent(window.location.pathname.substring(1));
        if (searched) {
            this.currentFolder(currentFolder);
            this.runSearch(GalleryViewModel.getUrlParameter("search"), false);
        } else {
            this.moveToFolder(currentFolder, false);
        }

        this.layoutManager = new LayoutManager('#pictures-gallery', 3);

        this.initOnPopState();
        this.registerOnScrollEvents();
    }

    public runSearchFromForm(searchElement: any): void {
        var query: string = $(searchElement).find('.search-input').val();
        this.runSearch(query, true);
    }

    private runSearch(query: string, saveInHistory: boolean): void {
        document.title = query + " (" + this.currentFolderName() + ")";

        if (query.trim().length > 0) {
            this.doAjaxRequest(this.currentFolder(), query, saveInHistory);
        }
    }

    public moveToFolder(newFolder: string, saveInHistory: boolean): void {
        document.title = newFolder || "Photato gallery";

        this.doAjaxRequest(newFolder, null, saveInHistory);
    }

    private doAjaxRequest(folder: string, query: string, saveInHistory: boolean) {
        if (saveInHistory) {
            this.pushState(folder, query, false);
        }

        window.scroll(0, 0); // Reset scroll to prevent OnScroll events from being sent
        this.folders([]);
        this.allMedias([]);
        this.bannerMessage("");
        this.displayedPicturesCount(GalleryViewModel.batchSize);
        this.currentSearchQuery(query);
        this.currentFolder(folder);

        var that = this;

        var queryParameter = query ? ("&query=" + encodeURIComponent(query)) : '';

        this.currentAjaxRequest && this.currentAjaxRequest.abort();
        this.currentAjaxRequest = $.ajax("/api/list?folder=" + encodeURIComponent(folder) + queryParameter)
            .success(function(res: PhotatoRequestResults) {
                that.folders(res.folders);
                that.allMedias(res.medias);

                if (res.folders.length == 0 && res.medias.length == 0) {
                    that.bannerMessage(Messages.noResult);
                }

                var state = <HistoryState>history.state;
                if (state) {
                    state.folders = that.folders();
                    state.allMedias = that.allMedias();
                    state.bannerMessage = that.bannerMessage();
                    state.displayedPicturesCount = that.displayedPicturesCount();
                    history.replaceState(state, null, null);
                }

                that.layoutManager.run();
            }).error(function() {
                that.bannerMessage(Messages.serverError);
            });
    }


    public openLightGallery(pictureIndex: number): void {
        this.pushState(this.currentFolder(), this.currentSearchQuery(), true);
        var dynamicEl: any[] = this.allMedias().map((media: PhotatoMedia) => {
            if (media.mediaType === "picture") {
                var picture: PhotatoPicture = <PhotatoPicture>media;
                return {
                    src: picture.fullscreenPicture.url,
                    thumb: picture.thumbnail.url,
                    subHtml: GalleryViewModel.getLightGallerySubHtml(picture),
                    downloadUrl: picture.rawPicture.url,
                    width: picture.fullscreenPicture.width,
                }
            } else {
                var video: PhotatoVideo = <PhotatoVideo>media;
                return {
                    html: '<video class="lg-video-object lg-html5 video-js vjs-default-skin" controls preload="none"><source src="' + video.videoPath + '" type="' + video.videoType + '">' + Messages.videosNotSupported + '</video>',
                    downloadUrl: video.videoPath,
                    poster: video.fullscreenPicture.url,
                }
            }
        });
        
        // Light gallery reset
        $('#lightgallery').remove();
        $('body').append('<div id="lightgallery"></div>');

        var lg = (<any>$('#lightgallery'));
        lg.lightGallery({
            dynamic: true,
            dynamicEl: dynamicEl,
            index: pictureIndex,

            speed: 400,
            hideBarsDelay: 2000,
            loop: false,
            counter: false,
            preload: 2,
            startClass: 'lg-fade',
            videoMaxWidth: '1200px'
        });

        lg.on('onCloseAfter.lg', (e) => {
            history.back();
        });
    }


    private static getUrlParameter(wantedParameter: string): string {
        var sPageURL: string = decodeURIComponent(window.location.search.substring(1));
        var sURLVariables: string[] = sPageURL.split('&');

        for (var i = 0; i < sURLVariables.length; i++) {
            var sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === wantedParameter) {
                return sParameterName.length === 1 ? 'true' : sParameterName[1].trim();
            }
        }
    }

    private pushState(newFolder: string, newSearchQuery: string, newFullScreenOpened: boolean): void {
        var newUrl = "/" + newFolder;
        if (newSearchQuery) {
            newUrl += "?search=" + newSearchQuery;
        }

        if (!isMobileOrTablet()) { // Desktop, we dont care about the previous button for closing the fullscreen
            newFullScreenOpened = false;
        }

        var state: HistoryState = <HistoryState>{ currentSearchQuery: this.currentSearchQuery(), currentFolder: this.currentFolder(), allMedias: this.allMedias(), folders: this.folders(), displayedPicturesCount: this.displayedPicturesCount(), bannerMessage: this.bannerMessage() };
        var newState = { currentSearchQuery: newSearchQuery, currentFolder: newFolder, fullScreenOpened: newFullScreenOpened, allPictures: this.allMedias(), folders: this.folders(), displayedPicturesCount: this.displayedPicturesCount(), bannerMessage: this.bannerMessage() };
        history.replaceState(state, null, null);

        history.pushState(newState, null, newUrl);
    }

    private initOnPopState(): void {
        window.onpopstate = (e) => {
            var state: HistoryState = e.state;

            if ($('#lightgallery').data('lightGallery')) {
                (<any>$('#lightgallery')).data('lightGallery').destroy(true);
            }

            if (state) {
                this.currentSearchQuery(state.currentSearchQuery);
                this.currentFolder(state.currentFolder);
                this.folders(state.folders);
                this.allMedias(state.allMedias);
                this.bannerMessage(state.bannerMessage);
                this.displayedPicturesCount(state.displayedPicturesCount);
            }

            this.layoutManager.run();
        };
    }


    private registerOnScrollEvents(): void {
        window.onscroll = (e) => {
            if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
                if (this.displayedPicturesCount() < this.allMedias().length) {
                    this.displayedPicturesCount(this.displayedPicturesCount() + GalleryViewModel.batchSize);

                    var state = <HistoryState>history.state;
                    state.displayedPicturesCount = this.displayedPicturesCount();
                    history.replaceState(state, null, null);

                    this.layoutManager.run();
                }
            }

        }
    }

    private static getLightGallerySubHtml(picture: PhotatoPicture): string {
        var dateStr = (new Date(picture.timestamp).toLocaleDateString());
        var title = (picture.title || picture.name);
        var positionStr = (picture.position.hardcodedPosition || (picture.position.coordinatesDescription && picture.position.coordinatesDescription.length && picture.position.coordinatesDescription) || '')
        var personsStr = (picture.persons && picture.persons.sort().join(', '));
        var tagsStr = (picture.tags && picture.tags.sort().join(', '));

        var firstRow = '<div class="dateTitle">' + dateStr + '<span class="separator">·</span>' + title + '</div>';

        var secondRowValues: string[] = [];
        if (positionStr) {
            secondRowValues.push(positionStr);
        }
        if (personsStr) {
            secondRowValues.push(personsStr);
        }
        if (tagsStr) {
            secondRowValues.push(tagsStr);
        }
        var secondRow = '<div class="subtitle">' + secondRowValues.join('<span class="separator">·</span>') + '</div>';
        return firstRow + (secondRowValues.length ? secondRow : '');
    }
}

function isMobileOrTablet() {
    var check = false;
    (function(a) { if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))) check = true; })(navigator.userAgent || navigator.vendor || window['opera']);
    return check;
}