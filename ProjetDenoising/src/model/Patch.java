package model;

import java.awt.Image;

public class Patch {
    private Image image;
    private Pixel[][] matrice;
    private int taille;
    private int[] position;
    private int[] premierPixelPos;

    public Patch() {
        this.premierPixelPos = new int[2];  // Position du premier pixel du patch
    }

    public Image getImage() {
        return this.image;
    }

    public Pixel[][] getMatrice() {
        return this.matrice;
    }


    public int getTaille() {
        return this.taille;
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



    public void setPremierPixelPos(int[] premierPixelPos) {
        this.premierPixelPos = premierPixelPos;
    }
    
    public int[] getPremierPixelPos() {
    	return this.premierPixelPos;
    }
    public void setPosition(int[] position) {
        this.position = position;
    }
}
