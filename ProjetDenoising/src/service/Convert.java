package service;

import model.Pixel;
import model.Photo;

import java.awt.image.BufferedImage;

/**
 * Classe utilitaire pour convertir entre des objets Photo/Pixel et des images BufferedImage.
 */
public class Convert {

    /**
     * Convertit une image Photo en matrice de pixels.
     *
     * @param photo l'objet Photo contenant l'image à convertir
     * @return une matrice de pixels représentant l'image
     */
    public static Pixel[][] convertirImageEnMatrice(Photo photo) {
        BufferedImage image = photo.getImage(); 
        int largeur = image.getWidth();
        int hauteur = image.getHeight();
        Pixel[][] matrice = new Pixel[hauteur][largeur];

        for (int i = 0; i < hauteur; i++) {
            for (int j = 0; j < largeur; j++) {
                int rgb = image.getRGB(j, i);
                matrice[i][j] = Pixel.fromRGB(rgb);
            }
        }
        return matrice;
    }

    /**
     * Convertit une matrice de pixels en image BufferedImage.
     *
     * @param matrice matrice de pixels à convertir
     * @return une image BufferedImage construite à partir de la matrice
     */
    public static BufferedImage convertirMatriceEnImage(Pixel[][] matrice) {
        int hauteur = matrice.length;
        int largeur = matrice[0].length;
        BufferedImage image = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < hauteur; i++) {
            for (int j = 0; j < largeur; j++) {
                Pixel pixel = matrice[i][j];
                image.setRGB(j, i, pixel.toRGB());
            }
        }
        return image;
    }
}
