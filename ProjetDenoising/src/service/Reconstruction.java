package service;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Reconstruction {
    public static double[][] reconstruireVecteurs(double[][] alpha, double[][] U, double[] mV) {
        int M = alpha.length;
        int s2 = mV.length;
        
        double[][] Vdenoised = new double[M][s2];
        RealMatrix Umatrix = MatrixUtils.createRealMatrix(U);
        
        for (int k = 0; k < M; k++) {
            RealVector alphaK = MatrixUtils.createRealVector(alpha[k]);
            RealVector VcK = Umatrix.operate(alphaK);
            
            for (int i = 0; i < s2; i++) {
                Vdenoised[k][i] = VcK.getEntry(i) + mV[i];
            }
        }
        return Vdenoised;
    }
}