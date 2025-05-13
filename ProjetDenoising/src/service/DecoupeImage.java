package service;

import model.Photo;
import model.Imagette;

import java.awt.*;
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

        // Déterminer le nombre de colonnes et de lignes nécessaires pour avoir n imagettes
        int numColumns = (int) Math.sqrt(n);  // Nombre de colonnes
        int numRows = (int) Math.ceil((double) n / numColumns); // Nombre de lignes

        // Calcul des dimensions de chaque imagette
        int W = largeur / numColumns;  // Largeur de chaque imagette
        int H = hauteur / numRows;    // Hauteur de chaque imagette

        // Boucles pour découper l'image en imagettes
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                int x = col * W;
                int y = row * H;

                // Vérifier que la sous-image ne dépasse pas les dimensions de l'image
                if (x + W <= largeur && y + H <= hauteur) {
                    BufferedImage subImage = image.getSubimage(x, y, W, H);
                    imagettes.add(new Imagette(subImage, new Point(x, y)));
                }
            }
        }

        return imagettes;
    }
}
