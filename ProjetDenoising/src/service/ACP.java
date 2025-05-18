package service;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

/**
 * Classe permettant d'appliquer l'Analyse en Composantes Principales (ACP).
 */
public class ACP {

    /**
     * Applique l'Analyse en Composantes Principales (ACP) à une matrice de données.
     *
     * @param V Matrice de données (M lignes × N colonnes), où chaque ligne est un vecteur d'observation.
     * @return Résultat de l'ACP : vecteur moyen, matrice centrée, valeurs propres et vecteurs propres.
     */
    public static ACPResult appliquerACP(double[][] V) {
        int M = V.length;         // Nombre de vecteurs
        int N = V[0].length;      // Dimension des vecteurs

        // Moyenne mV
        double[] mV = new double[N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                mV[j] += V[i][j];
            }
        }
        for (int j = 0; j < N; j++) {
            mV[j] /= M;
        }

        // Vecteurs centrés Vc
        double[][] Vc = new double[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                Vc[i][j] = V[i][j] - mV[j];
            }
        }

        // Matrice de covariance
        double[][] covariance = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                for (int k = 0; k < M; k++) {
                    covariance[i][j] += Vc[k][i] * Vc[k][j];
                }
                covariance[i][j] /= M;
            }
        }

        // Diagonalisation
        RealMatrix covarianceMatrix = MatrixUtils.createRealMatrix(covariance);
        EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);

        double[][] U = new double[N][N];
        double[] valeursPropres = new double[N];

        for (int i = 0; i < N; i++) {
            valeursPropres[i] = eig.getRealEigenvalue(i);
            RealVector vec = eig.getEigenvector(i);
            for (int j = 0; j < N; j++) {
                U[j][i] = vec.getEntry(j);
            }
        }

        // Résultat
        ACPResult res = new ACPResult();
        res.setmV(mV);
        res.setU(U);
        res.setVc(Vc);
        res.setValeursPropres(valeursPropres);
        return res;
    }
}
