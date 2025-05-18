package service;

import java.util.Random;
import model.Pixel;

/**
 * Classe utilitaire pour ajouter du bruit gaussien à une image.
 */
public class Bruit {
    private double sigma; // Écart-type du bruit gaussien

    /**
     * Constructeur par défaut (sigma = 0).
     */
    public Bruit() {
        this.sigma = 0;
    }

    /**
     * Constructeur avec paramètre.
     * 
     * @param sigma écart-type du bruit à appliquer
     */
    public Bruit(double sigma) {
        this.sigma = sigma;
    }

    /**
     * Applique un bruit gaussien à une image.
     * 
     * @param image image originale sous forme de matrice de pixels
     * @param sigma écart-type du bruit gaussien
     * @return une nouvelle image bruitée
     */
    public static Pixel[][] noising(Pixel[][] image, double sigma) {
        if (image == null) return null;

        int height = image.length;
        int width = image[0].length;
        Pixel[][] noisedImage = new Pixel[height][width];
        Random random = new Random();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel originalPixel = image[i][j];

                double noiseFactorR = random.nextGaussian() * sigma;
                double noiseFactorG = random.nextGaussian() * sigma;
                double noiseFactorB = random.nextGaussian() * sigma;

                int newR = clamp((int) (originalPixel.getRouge() + noiseFactorR));
                int newG = clamp((int) (originalPixel.getVert() + noiseFactorG));
                int newB = clamp((int) (originalPixel.getBleu() + noiseFactorB));

                noisedImage[i][j] = new Pixel(newR, newG, newB);
            }
        }

        return noisedImage;
    }

    /**
     * Contraint une valeur entre 0 et 255.
     * 
     * @param value valeur à contraindre
     * @return valeur comprise entre 0 et 255
     */
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
