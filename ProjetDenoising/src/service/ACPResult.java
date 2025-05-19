package service;

/**
 * Classe contenant les résultats de l'Analyse en Composantes Principales (ACP).
 */
public class ACPResult {
    private double[] mV;              // Moyenne des vecteurs
    private double[][] U;            // Vecteurs propres (base orthonormale)
    private double[][] Vc;           // Matrice centrée
    private double[] valeursPropres; // Valeurs propres associées aux vecteurs propres
    private double[][] alphaSeuille; // Coefficients projetés et éventuellement seuillés

    /**
     * @return le vecteur moyen (mV)
     */
    public double[] getmV() {
        return mV;
    }

    /**
     * Définit le vecteur moyen.
     * 
     * @param mV vecteur moyen à définir
     */
    public void setmV(double[] mV) {
        this.mV = mV;
    }

    /**
     * @return la matrice des vecteurs propres (U)
     */
    public double[][] getU() {
        return U;
    }

    /**
     * Définit la matrice des vecteurs propres.
     * 
     * @param U matrice des vecteurs propres à définir
     */
    public void setU(double[][] U) {
        this.U = U;
    }

    /**
     * @return la matrice centrée (Vc)
     */
    public double[][] getVc() {
        return Vc;
    }

    /**
     * Définit la matrice centrée.
     * 
     * @param Vc matrice centrée à définir
     */
    public void setVc(double[][] Vc) {
        this.Vc = Vc;
    }

    /**
     * @return le tableau des valeurs propres
     */
    public double[] getValeursPropres() {
        return valeursPropres;
    }

    /**
     * Définit les valeurs propres.
     * 
     * @param valeursPropres tableau des valeurs propres à définir
     */
    public void setValeursPropres(double[] valeursPropres) {
        this.valeursPropres = valeursPropres;
    }

    /**
     * @return la matrice des coefficients projetés seuillés
     */
    public double[][] getAlphaSeuille() {
        return alphaSeuille;
    }

    /**
     * Définit les coefficients projetés seuillés.
     * 
     * @param alphaSeuille matrice à définir
     */
    public void setAlphaSeuille(double[][] alphaSeuille) {
        this.alphaSeuille = alphaSeuille;
    }
}