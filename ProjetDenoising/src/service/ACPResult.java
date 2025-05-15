package service;

public class ACPResult {
    private double[] mV;              // Moyenne de chaque coordonnée
    private double[][] U;            // Base orthonormale
    private double[][] Vc;           // Matrice centrée
    private double[] valeursPropres; // Valeurs propres
    private double[][] alphaSeuille; // coefficients projetés et seuillés

    // Getter et Setter pour mV
    public double[] getmV() {
        return mV;
    }

    public void setmV(double[] mV) {
        this.mV = mV;
    }

    // Getter et Setter pour U
    public double[][] getU() {
        return U;
    }

    public void setU(double[][] U) {
        this.U = U;
    }

    // Getter et Setter pour Vc
    public double[][] getVc() {
        return Vc;
    }

    public void setVc(double[][] Vc) {
        this.Vc = Vc;
    }

    // Getter et Setter pour valeursPropres
    public double[] getValeursPropres() {
        return valeursPropres;
    }

    public void setValeursPropres(double[] valeursPropres) {
        this.valeursPropres = valeursPropres;
    }

    // Getter et Setter pour alphaSeuille
    public double[][] getAlphaSeuille() {
        return alphaSeuille;
    }

    public void setAlphaSeuille(double[][] alphaSeuille) {
        this.alphaSeuille = alphaSeuille;
    }
}
