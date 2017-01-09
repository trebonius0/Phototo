class LayoutManager {
    private containerDescriptor: string;
    private margin: number;

    constructor(containerDescriptor: string, margin: number) {
        this.containerDescriptor = containerDescriptor;
        this.margin = margin;

        $(window).resize(() => this.run());
    }

    public run(): void {
        var container = $(this.containerDescriptor);
        var containerWidth: number = container.width() - 10;

        var pictures = container.find('img');

        var currentRow = [];
        var currentSum: number = 0;

        pictures.each((i, e) => {
            var pictureWidth: number = parseInt($(e).data('width'));
            currentSum += pictureWidth;
            currentRow.push(e);

            if (currentSum > containerWidth) {
                var newSum = 0;
                var maxWidth = containerWidth - (currentRow.length - 1) * this.margin;

                for (var j = 0; j < currentRow.length; j++) {
                    var originalWidth: number = parseInt($(currentRow[j]).data('width'));
                    var newWidth: number;

                    if (j == currentRow.length - 1) {
                        newWidth = maxWidth - newSum;
                    } else {
                        newWidth = Math.round(originalWidth * maxWidth / currentSum);
                        newSum += newWidth;
                    }
                    $(currentRow[j]).attr('width', newWidth);

                    if (j != currentRow.length - 1) {
                        $(currentRow[j]).css('margin-right', this.margin + 'px');
                    }
                    $(currentRow[j]).css('margin-bottom', this.margin + 'px');
                }

                currentSum = 0;
                currentRow = [];
            }
        });

        // For last line elements
        for (var j = 0; j < currentRow.length; j++) {
            $(currentRow[j]).css('margin-right', this.margin + 'px');
            $(currentRow[j]).css('margin-bottom', this.margin + 'px');
        }
    }
}


