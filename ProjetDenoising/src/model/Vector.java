package model;

/**
 * Représente un vecteur de pixels (issu d'un patch carré s × s) linéarisé en 1D.
 */
public class Vector {
    private final Pixel[] matrice;
    private final int s2;
    private final int[] premierPixelPos;

    /**
     * Construit un vecteur à partir d'une matrice carrée s × s de pixels.
     *
     * @param matrice         matrice de pixels carrée
     * @param s               taille du côté de la matrice (hauteur = largeur = s)
     * @param premierPixelPos position du premier pixel dans l'image d'origine
     */
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

    // --- Getters ---

    public Pixel[] getMatrice() {
        return matrice;
    }

    public int getS2() {
        return s2;
    }

    public int[] getPremierPixelPos() {
        return premierPixelPos;
    }

    // --- Canal Extraction ---

    /**
     * Extrait un canal ("R", "G" ou "B") sous forme de vecteur de doubles.
     *
     * @param canal "R", "G" ou "B"
     * @return tableau de valeurs du canal extrait
     */
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
