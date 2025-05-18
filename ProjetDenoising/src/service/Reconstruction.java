package service;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Classe utilitaire pour reconstruire des vecteurs à partir de coefficients projetés (alpha),
 * une base orthogonale (U), et une moyenne (mV).
 *
 * Cette reconstruction est typiquement utilisée dans des techniques de réduction de dimension
 * comme l'ACP (Analyse en Composantes Principales).
 */
public class Reconstruction {

    /**
     * Reconstruit les vecteurs d'origine à partir de leurs coefficients projetés dans la base U,
     * puis en ajoutant la moyenne mV.
     *
     * @param alpha les coefficients projetés (M vecteurs de dimension r)
     * @param U     la matrice de base (r lignes, s2 colonnes)
     * @param mV    le vecteur moyen (de dimension s2)
     * @return un tableau 2D contenant les vecteurs reconstruits (M vecteurs de dimension s2)
     */
    public static double[][] reconstruireVecteurs(double[][] alpha, double[][] U, double[] mV) {
        int M = alpha.length;     // Nombre de vecteurs à reconstruire
        int s2 = mV.length;       // Dimension finale des vecteurs reconstruits

        double[][] Vdenoised = new double[M][s2];
        RealMatrix Umatrix = MatrixUtils.createRealMatrix(U);

        for (int k = 0; k < M; k++) {
            RealVector alphaK = MatrixUtils.createRealVector(alpha[k]);
            RealVector VcK = Umatrix.operate(alphaK); // Produit matriciel U * alpha_k

            for (int i = 0; i < s2; i++) {
                Vdenoised[k][i] = VcK.getEntry(i) + mV[i]; // Ajout du vecteur moyen
            }
        }

        return Vdenoised;
    }
}
