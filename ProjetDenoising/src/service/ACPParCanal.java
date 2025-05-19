package service;

import model.Vector;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.Covariance;

import java.util.List;

public class ACPParCanal {
    private final double[] moyenne;
    private final RealMatrix baseOrthonormee;
    private final double[][] X;  // données centrées

    public ACPParCanal(List<Vector> vecteurs, String canal) {
        int n = vecteurs.size();
        int d = vecteurs.get(0).getS2();
        X = new double[n][d];

        // Remplir les données et calculer la moyenne
        moyenne = new double[d];
        for (int i = 0; i < n; i++) {
            double[] ligne = vecteurs.get(i).extraireCanal(canal);
            for (int j = 0; j < d; j++) {
                moyenne[j] += ligne[j];
                X[i][j] = ligne[j];  // copie pour centrage
            }
        }
        for (int j = 0; j < d; j++) {
            moyenne[j] /= n;
        }

        // Centrage des données
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                X[i][j] -= moyenne[j];
            }
        }

        // Matrice de covariance
        RealMatrix mat = new Array2DRowRealMatrix(X);
        RealMatrix cov = new Covariance(mat).getCovarianceMatrix();

        // Décomposition en vecteurs propres
        EigenDecomposition eig = new EigenDecomposition(cov);
        baseOrthonormee = eig.getV();  // chaque colonne = un vecteur propre
    }

    public double[][] projeterDansBase() {
        RealMatrix Xmat = new Array2DRowRealMatrix(X);
        RealMatrix alpha = Xmat.multiply(baseOrthonormee);
        return alpha.getData();
    }

    public double[][] reconstruireDepuisBase(double[][] alpha) {
        RealMatrix alphaMat = new Array2DRowRealMatrix(alpha);
        RealMatrix Vt = baseOrthonormee.transpose();
        RealMatrix Xrec = alphaMat.multiply(Vt);
        double[][] data = Xrec.getData();

        // Remise à l’échelle d’origine (ajout de la moyenne)
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] += moyenne[j];
            }
        }
        return data;
    }

    public double[][] seuillageDur(double[][] alpha, double seuil) {
        int n = alpha.length;
        int d = alpha[0].length;
        double[][] resultat = new double[n][d];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                double a = alpha[i][j];
                resultat[i][j] = Math.abs(a) < seuil ? 0 : a;
            }
        }
        return resultat;
    }

    public double[][] seuillageDoux(double[][] alpha, double seuil) {
        int n = alpha.length;
        int d = alpha[0].length;
        double[][] resultat = new double[n][d];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                double a = alpha[i][j];
                if (Math.abs(a) < seuil) {
                    resultat[i][j] = 0;
                } else {
                    resultat[i][j] = Math.signum(a) * (Math.abs(a) - seuil);
                }
            }
        }
        return resultat;
    }

    public void afficherDebug(String canal) {
        System.out.println("Canal " + canal + " :");
        System.out.print("→ Moyenne : [");
        for (int i = 0; i < Math.min(5, moyenne.length); i++) {
            System.out.printf("%.2f ", moyenne[i]);
        }
        System.out.println("...]");
        System.out.println("→ Base orthonormée : " + baseOrthonormee.getRowDimension() + "x" + baseOrthonormee.getColumnDimension());
    }
}
