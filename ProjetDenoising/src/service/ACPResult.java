package service;

public class ACPResult {
    private double[] mV;              // Moyenne de chaque coordonnée
    private double[][] U;            // Base orthonormale (matrice dont chaque colonne est un vecteur propre)
    private double[][] Vc;           // Matrice centrée (chaque colonne est un patch vectorisé centré)
    private double[] valeursPropres; // Valeurs propres (variance portée par chaque axe)

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
}
