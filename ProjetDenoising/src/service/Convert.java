package service;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;

import model.Pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Convert {

    public static Pixel[][] convertirImageEnMatrice(String cheminImage) throws IOException {
        BufferedImage image = ImageIO.read(new File(cheminImage));
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
