package phototo.helpers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class ImageHelper {

    public static BufferedImage resizeImage(BufferedImage image, int wantedWidth, int wantedHeight) {
        int w = image.getWidth();
        int h = image.getHeight();
        AffineTransform at = new AffineTransform();
        at.scale((double) wantedWidth / (double) w, (double) wantedHeight / (double) h);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage rescaledImage = new BufferedImage(wantedWidth, wantedHeight, BufferedImage.TYPE_INT_ARGB);
        rescaledImage = scaleOp.filter(image, rescaledImage);

        return rescaledImage;
    }

    public static BufferedImage resizeImageSmooth(BufferedImage image, int wantedWidth, int wantedHeight) {
        Image scaledImage = image.getScaledInstance(wantedWidth, wantedHeight, Image.SCALE_SMOOTH);
        BufferedImage imageBuff = new BufferedImage(wantedWidth, wantedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = imageBuff.createGraphics();
        g.drawImage(scaledImage, 0, 0, null, null);
        g.dispose();

        return imageBuff;
    }

    public static BufferedImage readImage(Path file, int orientation) throws IOException {
        BufferedImage image = ImageIO.read(file.toFile());
        int width = image.getWidth();
        int height = image.getHeight();

        AffineTransform t = new AffineTransform();

        switch (orientation) {
            case 1:
                break;
            case 2: // Flip X
                t.scale(-1.0, 1.0);
                t.translate(-width, 0);
                break;
            case 3: // PI rotation 
                t.translate(width, height);
                t.rotate(Math.PI);
                break;
            case 4: // Flip Y
                t.scale(1.0, -1.0);
                t.translate(0, -height);
                break;
            case 5: // - PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-1.0, 1.0);
                break;
            case 6: // -PI/2 and -width
                t.translate(height, 0);
                t.rotate(Math.PI / 2);
                break;
            case 7: // PI/2 and Flip
                t.scale(-1.0, 1.0);
                t.translate(-height, 0);
                t.translate(0, width);
                t.rotate(3 * Math.PI / 2);
                break;
            case 8: // PI / 2
                t.translate(0, width);
                t.rotate(3 * Math.PI / 2);
                break;
            default:
                break;
        }

        AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BICUBIC);

        BufferedImage destinationImage = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null);
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        destinationImage = op.filter(image, destinationImage);
        return destinationImage;
    }
}
