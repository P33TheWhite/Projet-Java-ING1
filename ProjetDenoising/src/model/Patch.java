package model;

import java.awt.Image;

public class Patch {
    private Image image;
    private Pixel[][] matrice;
    private int taille;
    private int[] position;
    private int[] premierPixelPos;

    public Patch() {
        this.premierPixelPos = new int[2];
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

    public void setTaille(int taille) {
        this.taille = taille;
    }

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

    public int[] getPosition() {
        return this.position;
    }

    public double[] extraireCanal(String canal) {
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
