package service;

import model.Photo;
import model.Imagette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DecoupeImage {

    public static List<Imagette> decoupeImage(Photo photo, int W, int n) {
        BufferedImage image = photo.getImage();
        int largeur = photo.getLargeur();
        int hauteur = photo.getHauteur();

        List<Imagette> imagettes = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            int x = random.nextInt(largeur - W + 1);
            int y = random.nextInt(hauteur - W + 1);

            BufferedImage subImage = image.getSubimage(x, y, W, W);
            imagettes.add(new Imagette(subImage, new Point(x, y)));
        }

        return imagettes;
    }
}
