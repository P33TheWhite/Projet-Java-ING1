package model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Photo {
    private String fileName;
    private BufferedImage bufferedImage;
    private int largeur;
    private int hauteur;
    private String nom;
    private Pixel[][] matrice;
    private int variance;

    public Photo(String fileName, String nom) throws IOException {
        this.fileName = fileName;
        this.nom = nom;

        File file = new File(fileName);
        this.bufferedImage = ImageIO.read(file);

        this.largeur = bufferedImage.getWidth();
        this.hauteur = bufferedImage.getHeight();
        this.matrice = convertirImageEnMatrice(fileName);
    }

    public BufferedImage getImage() {
        return bufferedImage;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public String getNom() {
        return nom;
    }

    public Pixel[][] getMatrice() {
        return matrice;
    }

    public int getVariance() {
        return variance;
    }

    private Pixel[][] convertirImageEnMatrice(String cheminImage) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(cheminImage));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        Pixel[][] matrice = new Pixel[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = bufferedImage.getRGB(j, i);
                matrice[i][j] = Pixel.fromRGB(rgb);
            }
        }
        return matrice;
    }
}