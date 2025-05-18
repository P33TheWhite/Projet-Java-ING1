package service;

/**
 * Classe de méthodes pour effectuer différents types de seuillages
 * (soft thresholding, hard thresholding) et calculer des seuils universel et bayésien
 * utilisés dans le traitement du signal et le débruitage.
 */
public class Seuillage {

    /**
     * Applique un seuillage doux (soft thresholding) sur une matrice de coefficients.
     * 
     * Pour chaque coefficient a, la valeur retournée est :
     * sign(a) * max(|a| - seuil, 0)
     * 
     * @param alpha Matrice des coefficients à seuiller
     * @param seuil Valeur du seuil
     * @return Matrice de coefficients seuillés avec le seuillage doux
     */
    public static double[][] seuillageDoux(double[][] alpha, double seuil) {
        int M = alpha.length;
        int s2 = alpha[0].length;

        double[][] alphaSeuilles = new double[M][s2];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < s2; j++) {
                double a = alpha[i][j];
                alphaSeuilles[i][j] = Math.signum(a) * Math.max(Math.abs(a) - seuil, 0);
            }
        }
        return alphaSeuilles;
    }

    /**
     * Applique un seuillage dur (hard thresholding) sur une matrice de coefficients.
     * 
     * Chaque coefficient val est conservé si |val| >= seuil, sinon remplacé par 0.
     * 
     * @param Vcontrib Matrice des coefficients à seuiller
     * @param seuil Valeur du seuil
     * @return Matrice de coefficients seuillés avec le seuillage dur
     */
    public static double[][] seuillageDur(double[][] Vcontrib, double seuil) {
        int lignes = Vcontrib.length;
        int colonnes = Vcontrib[0].length;

        double[][] contributionsSeuilles = new double[lignes][colonnes];

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                double val = Vcontrib[i][j];
                contributionsSeuilles[i][j] = Math.abs(val) >= seuil ? val : 0.0;
            }
        }

        return contributionsSeuilles;
    }

    /**
     * Calcule le seuil universel (VisuShrink) utilisé pour le seuillage.
     * 
     * Formule : seuil = σ * sqrt(2 * ln(L))
     * 
     * @param sigma Écart-type du bruit (σ)
     * @param L Nombre total de pixels dans l'image (taille du signal)
     * @return Seuil universel
     */
    public static double seuilleV(double sigma, int L) {
        return sigma * Math.sqrt(2 * Math.log(L));
    }

    /**
     * Calcule le seuil bayésien (BayesShrink) utilisé pour le seuillage.
     * 
     * Formule : seuil = σ² / σ_X, où σ_X est l'écart-type estimé du signal bruité
     * 
     * @param sigma2 Variance du bruit (σ²)
     * @param sigmaXb2 Variance estimée du signal bruité (ˆσ²_Xb)
     * @return Seuil bayésien
     */
    public static double seuilleB(double sigma2, double sigmaXb2) {
        double sigmaX = Math.sqrt(Math.max(sigmaXb2 - sigma2, 0));
        if (sigmaX == 0) {
            return Double.MAX_VALUE; // Cas extrême : seuil très élevé
        }
        return sigma2 / sigmaX;
    }
}
