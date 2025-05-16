 package service;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

public class ACP {

     // Calcule la moyenne, la matrice de covariance et la version centrée de V
	
    public static ACPResult appliquerACP(double[][] V) {
        int M = V.length;
        int s2 = V[0].length;

        // Calcul de la moyenne mV
        double[] mV = new double[s2];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < s2; j++) {
                mV[j] += V[i][j];
            }
        }
        for (int j = 0; j < s2; j++) {
            mV[j] /= M;
        }

        // Centrer les vecteurs
        double[][] Vc = new double[M][s2];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < s2; j++) {
                Vc[i][j] = V[i][j] - mV[j];
            }
        }

        // Calcul de la matrice de covariance Γ = 1/M * Vc^T * Vc
        double[][] covariance = new double[s2][s2];
        for (int i = 0; i < s2; i++) {
            for (int j = 0; j < s2; j++) {
                for (int k = 0; k < M; k++) {
                    covariance[i][j] += Vc[k][i] * Vc[k][j];
                }
                covariance[i][j] /= M;
            }
        }

        // Diagonalisation
        RealMatrix covarianceMatrix = MatrixUtils.createRealMatrix(covariance);
        EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);

        int n = covarianceMatrix.getRowDimension();
        double[][] U = new double[n][n];
        double[] valeursPropres = new double[n];

        for (int i = 0; i < n; i++) {
            valeursPropres[i] = eig.getRealEigenvalue(i);
            RealVector vec = eig.getEigenvector(i);
            for (int j = 0; j < n; j++) {
            	// Chaque colonne de U est un vecteur propre
                U[j][i] = vec.getEntry(j);
            }
        }

        // Resultats
        ACPResult res = new ACPResult();
        res.setmV(mV);
        res.setU(U);
        res.setVc(Vc);
        res.setValeursPropres(valeursPropres);
        return res;
    }
}
