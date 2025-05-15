package service;

public class Proj {

    /**
     * Projette chaque vecteur centré dans la base U
     * @param U Base orthonormale obtenue par ACP (s2 × s2)
     * @param Vc Matrice des vecteurs centrés (M × s2)
     * @return Matrice des coefficients alpha (M × s2)
     */
    public static double[][] calculerContributions(double[][] U, double[][] Vc) {
        int M = Vc.length;
        int s2 = Vc[0].length;

        double[][] alpha = new double[M][s2];

        // alpha(k)_i = <Vc_k, u_i>
        for (int k = 0; k < M; k++) {
            for (int i = 0; i < s2; i++) {
                for (int j = 0; j < s2; j++) {
                    alpha[k][i] += Vc[k][j] * U[j][i]; // produit scalaire
                }
            }
        }
        return alpha;
    }
}
