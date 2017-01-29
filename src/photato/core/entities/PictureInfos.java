package photato.core.entities;

import com.google.gson.annotations.Expose;

public class PictureInfos {

    @Expose
    public final String url;

    @Expose
    public final int width;

    @Expose
    public final int height;

    public final int rotationId;

    public PictureInfos(String url, int width, int height, int rotationId) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.rotationId = rotationId;
    }

}
