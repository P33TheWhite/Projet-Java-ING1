package service;

public class Seuillage {

    /**
     * Applique un seuillage doux (soft thresholding) sur les coefficients de projection
     * @param alpha Matrice des coefficients (M × s2)
     * @param seuil Valeur de seuillage
     * @return Matrice des coefficients seuillés
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
     * Applique un seuillage dur sur les coefficients de projection (contributions).
     * @param Vcontrib Contributions (s2 × M) de chaque axe principal pour chaque vecteur V centré
     * @param seuil Valeur seuil lambda à appliquer
     * @return Nouvelle matrice des contributions seuillées
     */
    public static double[][] seuillageDur(double[][] Vcontrib, double seuil) {
        int lignes = Vcontrib.length;    // s2
        int colonnes = Vcontrib[0].length; // M

        double[][] contributionsSeuillées = new double[lignes][colonnes];

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                double val = Vcontrib[i][j];
                contributionsSeuillées[i][j] = Math.abs(val) >= seuil ? val : 0.0;
            }
        }
        return contributionsSeuillées;
    }
}
