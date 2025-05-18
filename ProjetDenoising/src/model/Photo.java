package model;

import service.Convert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Représente une photo contenant une image, ses métadonnées et sa matrice de pixels.
 */
public class Photo {
    private String fileName;
    private BufferedImage bufferedImage;
    private int largeur;
    private int hauteur;
    private String nom;
    private Pixel[][] matrice;
    private int variance;

    /**
     * Constructeur principal : charge une image à partir d'un fichier.
     *
     * @param fileName chemin absolu de l'image
     * @param nom      nom logique de la photo
     * @throws IOException si l'image ne peut pas être lue
     */
    public Photo(String fileName, String nom) throws IOException {
        this.fileName = fileName;
        this.nom = nom;

        File file = new File(fileName);
        this.bufferedImage = ImageIO.read(file);

        this.largeur = bufferedImage.getWidth();
        this.hauteur = bufferedImage.getHeight();

        // Convertir l'image en matrice de pixels
        this.matrice = Convert.convertirImageEnMatrice(this);
        this.variance = 0;
    }

    /**
     * Constructeur utilisé pour une image déjà chargée (sans matrice).
     *
     * @param image   BufferedImage
     * @param largeur largeur de l'image
     * @param hauteur hauteur de l'image
     */
    public Photo(BufferedImage image, int largeur, int hauteur) {
        this.bufferedImage = image;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.nom = "image découpée";
        this.fileName = null;
        this.matrice = null;
        this.variance = 0;
    }

    /**
     * Constructeur utilisé pour une image déjà chargée avec sa matrice.
     *
     * @param image   BufferedImage
     * @param largeur largeur de l'image
     * @param hauteur hauteur de l'image
     * @param matrice matrice de pixels associée à l'image
     */
    public Photo(BufferedImage image, int largeur, int hauteur, Pixel[][] matrice) {
        this.bufferedImage = image;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.nom = "image reconstruite";
        this.fileName = null;
        this.matrice = matrice;
        this.variance = 0;
    }

    // Getters

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

    // Setter utile à ajouter si variance doit être modifié dynamiquement
    public void setVariance(int variance) {
        this.variance = variance;
    }
}
