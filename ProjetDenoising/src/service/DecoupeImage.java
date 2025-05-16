package service;

import model.Photo;
import model.Imagette;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DecoupeImage {

    public static List<Imagette> decoupeImage(Photo photo, int n) {
        BufferedImage image = photo.getImage();
        int largeur = photo.getLargeur();
        int hauteur = photo.getHauteur();

        List<Imagette> imagettes = new ArrayList<>();

        if (n <= 0) return imagettes;

        // Nombre de colonnes et de lignes pour n imagettes
        int numColumns = (int) Math.sqrt(n);
        int numRows = (int) Math.ceil((double) n / numColumns);
        
        // Dimensions imagette
        int W = largeur / numColumns;
        int H = hauteur / numRows;

        // Iimage en imagettes
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                int x = col * W;
                int y = row * H;

                // sous-image inf aux dimensions de l'image
                if (x + W <= largeur && y + H <= hauteur) {
                    BufferedImage subImage = image.getSubimage(x, y, W, H);
                    imagettes.add(new Imagette(subImage, new Point(x, y)));
                }
            }
        }

        return imagettes;
    }
}
