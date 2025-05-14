package test;
import java.util.Arrays;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.Covariance;
import java.util.Random;

public class PCAUtils {

    // ✅ Fonction 1 : Calcul du vecteur moyen
    public static RealVector computeMeanVector(double[][] data) {
        int nRows = data.length;
        int nCols = data[0].length;
        double[] mean = new double[nCols];

        for (double[] row : data) {
            for (int j = 0; j < nCols; j++) {
                mean[j] += row[j];
            }
        }

        for (int j = 0; j < nCols; j++) {
            mean[j] /= nRows;
        }

        return new ArrayRealVector(mean);
    }

    // ✅ Fonction 2 : Calcul de la matrice de covariance (centrée)
    public static RealMatrix computeCovarianceMatrix(double[][] data) {
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        RealVector mean = computeMeanVector(data);
        RealMatrix centered = matrix.copy();

        for (int i = 0; i < centered.getRowDimension(); i++) {
            centered.setRowVector(i, centered.getRowVector(i).subtract(mean));
        }

        RealMatrix covMatrix = new Covariance(centered).getCovarianceMatrix();
        return covMatrix;
    }

    // ✅ Fonction 3 : Calcul des vecteurs propres (base orthonormée)
    public static void computeEigenBasis(RealMatrix covMatrix) {
        EigenDecomposition eig = new EigenDecomposition(covMatrix);

        System.out.println("Valeurs propres :");
        for (double val : eig.getRealEigenvalues()) {
            System.out.printf("%.4f ", val);
        }

        System.out.println("\nVecteurs propres (colonnes de la base orthonormée) :");
        for (int i = 0; i < covMatrix.getColumnDimension(); i++) {
            RealVector vec = eig.getEigenvector(i);
            System.out.println("Vecteur propre " + (i + 1) + " : " + vec);
        }
    }

    // ✅ Générateur de données aléatoires
    public static double[][] generateRandomVectors(int rows, int cols) {
        double[][] data = new double[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = rand.nextDouble();
            }
        }
        return data;
    }
    public static double[] projectOntoEigenBasis(double[] vector, RealVector mean, RealMatrix eigenVectors) {
    // Centre le vecteur : x - μ
    RealVector centered = new ArrayRealVector(vector).subtract(mean);

    // Projection : α = Vᵗ · (x - μ)
    RealMatrix eigenVectorsT = eigenVectors.transpose(); // Vᵗ
    RealVector projection = eigenVectorsT.operate(centered);

    return projection.toArray(); // Coordonnées projetées (α₁, α₂, ..., α_d)
    }

    // ✅ Exemple d'utilisation
    public static void main(String[] args) {
    double[][] data = generateRandomVectors(500, 100);

    RealVector mean = computeMeanVector(data);
    RealMatrix covMatrix = computeCovarianceMatrix(data);

    EigenDecomposition eig = new EigenDecomposition(covMatrix);
    RealMatrix eigenVectors = eig.getV(); // colonnes = vecteurs propres

    // Projection d’un vecteur centré (par ex, le premier vecteur du dataset)
    double[] projection = projectOntoEigenBasis(data[0], mean, eigenVectors);

    System.out.println("Coordonnées projetées du vecteur 0 :");
    System.out.println(Arrays.toString(projection));
    }
}

