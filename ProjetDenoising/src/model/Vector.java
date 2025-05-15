package model;

public class Vector {
    private Pixel[] matrice;
    private int s2;
    private int[] premierPixelPos;

    public Vector(Pixel[][] matrice, int s, int[] premierPixelPos) {
        if (matrice.length != s || matrice[0].length != s) {
            throw new IllegalArgumentException("La matrice doit être carrée de taille s × s.");
        }

        this.s2 = s * s;
        this.premierPixelPos = premierPixelPos;
        this.matrice = new Pixel[s2];

        int index = 0;
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                this.matrice[index++] = matrice[i][j];
            }
        }
    }

    public Pixel[] getMatrice() {
        return this.matrice;
    }

    public int getS2() {
        return this.s2;
    }

    public int[] getPremierPixelPos() {
        return this.premierPixelPos;
    }

    public double[] extraireCanal(String canal) {
        double[] result = new double[s2];

        for (int i = 0; i < s2; i++) {
            switch (canal) {
                case "R": result[i] = matrice[i].getRouge(); break;
                case "G": result[i] = matrice[i].getVert(); break;
                case "B": result[i] = matrice[i].getBleu(); break;
                default: result[i] = 0;
            }
        }

        return result;
    }
}
