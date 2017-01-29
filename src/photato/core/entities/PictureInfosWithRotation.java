package photato.core.entities;

public class PictureInfosWithRotation extends PictureInfos {

    public final int rotationId;

    public PictureInfosWithRotation(String url, int width, int height, int rotationId) {
        super(url, width, height);
        this.rotationId = rotationId;
    }

}
