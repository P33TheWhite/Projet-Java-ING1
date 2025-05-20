package model;

import java.awt.image.BufferedImage;


/**
 * Représente une photo contenant une image, ses métadonnées et sa matrice de pixels.
 */
public class Photo {
    private BufferedImage bufferedImage;
    private int largeur;
    private int hauteur;
    private String nom;
    private Pixel[][] matrice;
    private int variance;

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
