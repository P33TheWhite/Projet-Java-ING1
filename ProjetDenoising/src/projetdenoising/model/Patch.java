package projetdenoising.model;

import java.awt.Image;

public class Patch {
    private Image image;
    private Pixel[][] matrice;
    private int s;  // Taille de la fenêtre (patch)
    private int taille;
    private boolean superpose;
    private int[] position;
    private int[] premierPixelPos;

    public Patch() {
        this.superpose = false;
        this.position = new int[2];  // Position (x, y) du patch
        this.premierPixelPos = new int[2];  // Position du premier pixel du patch
    }

    public Image getImage() {
        return this.image;
    }

    public Pixel[][] getMatrice() {
        return this.matrice;
    }

    public int getS() {
        return this.s;
    }

    public int getTaille() {
        return this.taille;
    }

    public int[] getPos() {
        return this.position;
    }

    public int[] getPosition() {
        return this.premierPixelPos;
    }

    // Setters pour les variables privées
    public void setImage(Image image) {
        this.image = image;
    }

    public void setMatrice(Pixel[][] matrice) {
        this.matrice = matrice;
        this.taille = matrice.length * matrice[0].length;
    }

    public void setS(int s) {
        this.s = s;
    }

    public void setSuperpose(boolean superpose) {
        this.superpose = superpose;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public void setPremierPixelPos(int[] premierPixelPos) {
        this.premierPixelPos = premierPixelPos;
    }
}
