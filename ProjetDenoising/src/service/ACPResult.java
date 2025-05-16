package service;

public class ACPResult {
    private double[] mV;             // Moyenne de chaque coordonnée
    private double[][] U;            // Base orthonormale
    private double[][] Vc;           // Matrice centrée
    private double[] valeursPropres; // Valeurs propres
    private double[][] alphaSeuille; // coefficients projetés et seuillés

    public double[] getmV() {
        return mV;
    }

    public void setmV(double[] mV) {
        this.mV = mV;
    }

    public double[][] getU() {
        return U;
    }

    public void setU(double[][] U) {
        this.U = U;
    }

    public double[][] getVc() {
        return Vc;
    }

    public void setVc(double[][] Vc) {
        this.Vc = Vc;
    }

    public double[] getValeursPropres() {
        return valeursPropres;
    }

    public void setValeursPropres(double[] valeursPropres) {
        this.valeursPropres = valeursPropres;
    }

    public double[][] getAlphaSeuille() {
        return alphaSeuille;
    }

    public void setAlphaSeuille(double[][] alphaSeuille) {
        this.alphaSeuille = alphaSeuille;
    }
}
