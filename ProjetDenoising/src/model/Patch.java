package model;

import java.awt.Image;

/**
 * Représente un patch (une petite portion) d'image,
 * avec sa matrice de pixels et sa position.
 */
public class Patch {
    private Image image;
    private Pixel[][] matrice;
    private int taille; // taille totale = h * w
    private int[] position;       // position dans l'image globale (optionnelle)
    private int[] premierPixelPos; // position du premier pixel du patch dans l'image globale

    /**
     * Constructeur par défaut, initialise premierPixelPos à un tableau de taille 2.
     */
    public Patch() {
        this.premierPixelPos = new int[2];
    }

    // Getters et setters

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Pixel[][] getMatrice() {
        return matrice;
    }

    /**
     * Définit la matrice de pixels et met à jour la taille (hauteur * largeur).
     * 
     * @param matrice Matrice de pixels du patch (doit être rectangulaire)
     */
    public void setMatrice(Pixel[][] matrice) {
        this.matrice = matrice;
        if (matrice != null && matrice.length > 0 && matrice[0] != null) {
            this.taille = matrice.length * matrice[0].length;
        } else {
            this.taille = 0;
        }
    }

    public int getTaille() {
        return taille;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public int[] getPremierPixelPos() {
        return premierPixelPos;
    }

    public void setPremierPixelPos(int[] premierPixelPos) {
        this.premierPixelPos = premierPixelPos;
    }

    /**
     * Extrait les valeurs du canal spécifié ("R", "G" ou "B") dans un vecteur à une dimension.
     * 
     * @param canal Le canal de couleur à extraire ("R", "G", "B")
     * @return Un tableau de double contenant les valeurs du canal dans l'ordre ligne par ligne
     */
    public double[] extraireCanal(String canal) {
        if (matrice == null || matrice.length == 0 || matrice[0].length == 0) {
            return new double[0];
        }
        int h = matrice.length;
        int w = matrice[0].length;
        double[] vecteur = new double[h * w];

        int index = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Pixel p = matrice[i][j];
                switch (canal) {
                    case "R": vecteur[index] = p.getRouge(); break;
                    case "G": vecteur[index] = p.getVert(); break;
                    case "B": vecteur[index] = p.getBleu(); break;
                    default: vecteur[index] = 0;
                }
                index++;
            }
        }
        return vecteur;
    }
}
