package model;

import service.Convert;
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

    // Constructeur utilisé lors de l'ouverture d'une image depuis un chemin
    public Photo(String fileName, String nom) throws IOException {
        this.fileName = fileName;
        this.nom = nom;

        File file = new File(fileName);
        this.bufferedImage = ImageIO.read(file);

        this.largeur = bufferedImage.getWidth();
        this.hauteur = bufferedImage.getHeight();
        this.matrice = Convert.convertirImageEnMatrice(this);
    }

    // Nouveau constructeur utilisé Denoising controller avec BufferedImage déjà chargé
    public Photo(BufferedImage image, int largeur, int hauteur) {
        this.bufferedImage = image;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.nom = "image découpée";
        this.fileName = null;
        this.matrice = null;
        this.variance = 0;
    }
    public Photo(BufferedImage image, int largeur, int hauteur, Pixel[][] matrice) {
        this.bufferedImage = image;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.nom = "image reconstruite";
        this.fileName = null;
        this.matrice = matrice;
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

}
