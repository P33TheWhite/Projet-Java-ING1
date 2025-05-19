package service;

import model.Vector;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
import java.util.List;

public class ProjecteurCanalACP {
    private final RealMatrix baseOrthonormee;
    private final RealVector moyenne;
    private final double[][] donneesProjetees;

    public ProjecteurCanalACP(List<Vector> vecteurs, String canal, double lambda, boolean doux) {
        int n = vecteurs.size();       // nombre de patchs
        int d = vecteurs.get(0).getS2(); // dimension (nombre de pixels par canal)

        double[][] X = new double[n][d];

        // Construction de la matrice des données X
        for (int i = 0; i < n; i++) {
            X[i] = vecteurs.get(i).extraireCanal(canal);
        }

        RealMatrix Xmat = new Array2DRowRealMatrix(X);

        // Centrage : soustraction du vecteur moyen
        double[] moyenneCanal = new double[d];
        for (int j = 0; j < d; j++) {
            double somme = 0;
            for (int i = 0; i < n; i++) {
                somme += X[i][j];
            }
            moyenneCanal[j] = somme / n;
        }

        this.moyenne = new ArrayRealVector(moyenneCanal);

        for (int i = 0; i < n; i++) {
            Xmat.setRowVector(i, Xmat.getRowVector(i).subtract(moyenne));
        }

        // Covariance
        RealMatrix covariance = Xmat.transpose().multiply(Xmat).scalarMultiply(1.0 / n);

        // Décomposition en vecteurs propres
        EigenDecomposition eig = new EigenDecomposition(covariance);
        this.baseOrthonormee = eig.getV(); // V

        // Projection dans la base
        RealMatrix alpha = Xmat.multiply(baseOrthonormee); // α = X_c * V

        // Seuillage
        RealMatrix alphaSeuil = appliquerSeuillage(alpha, lambda, doux);

        // Reconstruction
        RealMatrix reconstruction = alphaSeuil.multiply(baseOrthonormee.transpose());

        // Re-ajout du vecteur moyen
        for (int i = 0; i < n; i++) {
            reconstruction.setRowVector(i, reconstruction.getRowVector(i).add(moyenne));
        }

        // Conversion en tableau final
        this.donneesProjetees = reconstruction.getData();
    }

    private RealMatrix appliquerSeuillage(RealMatrix alpha, double lambda, boolean doux) {
        int n = alpha.getRowDimension();
        int d = alpha.getColumnDimension();
        RealMatrix seuil = new Array2DRowRealMatrix(n, d);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                double val = alpha.getEntry(i, j);
                if (doux) {
                    // Seuillage doux
                    if (Math.abs(val) > lambda) {
                        seuil.setEntry(i, j, Math.signum(val) * (Math.abs(val) - lambda));
                    } else {
                        seuil.setEntry(i, j, 0);
                    }
                } else {
                    // Seuillage dur
                    seuil.setEntry(i, j, Math.abs(val) > lambda ? val : 0);
                }
            }
        }

        return seuil;
    }

    public double[][] getDonneesProjetees() {
        return donneesProjetees;
    }
}
