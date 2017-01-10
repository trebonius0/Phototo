package com.trebonius.phototo.helpers;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

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

}
