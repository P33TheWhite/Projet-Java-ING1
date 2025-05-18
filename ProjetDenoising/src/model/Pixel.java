package model;

/**
 * Représente un pixel avec ses composantes RGB et un état de superposition.
 */
public class Pixel {
    private int rouge;
    private int vert;
    private int bleu;
    private boolean estSuperpose;

    /**
     * Construit un pixel à partir de ses composantes RVB.
     *
     * @param rouge composante rouge (0-255)
     * @param vert  composante verte (0-255)
     * @param bleu  composante bleue (0-255)
     */
    public Pixel(int rouge, int vert, int bleu) {
        this.rouge = clamp(rouge);
        this.vert = clamp(vert);
        this.bleu = clamp(bleu);
        this.estSuperpose = false;
    }

    // --- Getters ---

    public int getRouge() {
        return rouge;
    }

    public int getVert() {
        return vert;
    }

    public int getBleu() {
        return bleu;
    }

    public boolean isSuperpose() {
        return estSuperpose;
    }

    // --- Setters ---

    public void setRouge(int rouge) {
        this.rouge = clamp(rouge);
    }

    public void setVert(int vert) {
        this.vert = clamp(vert);
    }

    public void setBleu(int bleu) {
        this.bleu = clamp(bleu);
    }

    public void setSuperpose(boolean estSuperpose) {
        this.estSuperpose = estSuperpose;
    }

    // --- Utilitaires ---

    /**
     * Convertit ce pixel en entier RGB.
     *
     * @return une valeur entière contenant les 3 canaux RGB
     */
    public int toRGB() {
        return (rouge << 16) | (vert << 8) | bleu;
    }

    /**
     * Crée un Pixel à partir d'une valeur RGB entière.
     *
     * @param rgb valeur entière codant les 3 canaux RGB
     * @return un nouveau Pixel
     */
    public static Pixel fromRGB(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new Pixel(r, g, b);
    }

    /**
     * Clamp une valeur entre 0 et 255.
     *
     * @param value la valeur à clampler
     * @return la valeur entre 0 et 255
     */
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
