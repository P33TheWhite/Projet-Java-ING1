package service;

import model.Photo;
import model.Imagette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour découper une photo en plusieurs imagettes.
 */
public class DecoupeImage {

    /**
     * Découpe une image en un nombre donné d'imagettes de taille égale.
     *
     * @param photo la photo à découper
     * @param n     le nombre d'imagettes souhaité
     * @return une liste d'imagettes extraites de l'image
     */
    public static List<Imagette> decoupeImage(Photo photo, int n) {
        BufferedImage image = photo.getImage();
        int largeur = photo.getLargeur();
        int hauteur = photo.getHauteur();

        List<Imagette> imagettes = new ArrayList<>();

        if (n <= 0) return imagettes;

        int numColumns = (int) Math.sqrt(n);
        int numRows = (int) Math.ceil((double) n / numColumns);

        int W = largeur / numColumns;
        int H = hauteur / numRows;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                int x = col * W;
                int y = row * H;

                if (x + W <= largeur && y + H <= hauteur) {
                    BufferedImage subImage = image.getSubimage(x, y, W, H);
                    imagettes.add(new Imagette(subImage, new Point(x, y)));
                }
            }
        }

        return imagettes;
    }
}
