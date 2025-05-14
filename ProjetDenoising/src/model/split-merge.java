import org.apache.commons.math3.linear.*;

public class RGBMerger/Split {

    public static RealVector mergeRGB(RealVector r, RealVector g, RealVector b) {
        int n = r.getDimension();

        if (g.getDimension() != n || b.getDimension() != n) {
            throw new IllegalArgumentException("Les vecteurs R, G et B doivent avoir la même dimension.");
        }

        double[] merged = new double[3 * n];

        for (int i = 0; i < n; i++) {
            merged[3 * i]     = r.getEntry(i);import java.util.Arrays;

            merged[3 * i + 1] = g.getEntry(i);
            merged[3 * i + 2] = b.getEntry(i);
        }

        return new ArrayRealVector(merged);
    }
    public static RealVector[] splitRGB(RealVector vectorizedPatch) {
        int length = vectorizedPatch.getDimension();
        if (length % 3 != 0) {
            throw new IllegalArgumentException("Le vecteur ne représente pas des pixels RGB valides (taille non multiple de 3)");
        }

        int n = length / 3;

        double[] red = new double[n];
        double[] green = new double[n];
        double[] blue = new double[n];

        for (int i = 0; i < n; i++) {
            red[i] = vectorizedPatch.getEntry(3 * i);
            green[i] = vectorizedPatch.getEntry(3 * i + 1);
            blue[i] = vectorizedPatch.getEntry(3 * i + 2);
        }

        RealVector rVec = new ArrayRealVector(red);
        RealVector gVec = new ArrayRealVector(green);
        RealVector bVec = new ArrayRealVector(blue);

        return new RealVector[] { rVec, gVec, bVec };
    }
}

