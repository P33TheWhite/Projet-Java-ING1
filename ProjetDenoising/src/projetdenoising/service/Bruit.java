package projetdenoising.service;

import java.util.Random;

import projetdenoising.model.Pixel;

public class Bruit {
    private double sigma;

    public Bruit() {
        this.sigma = 0;
    }

    public Bruit(double sigma) {
        this.sigma = sigma;
    }

    public static Pixel[][] noising(Pixel[][] image, double sigma) {
        if (image == null) return null;

        int height = image.length;
        int width = image[0].length;
        Pixel[][] noisedImage = new Pixel[height][width];
        Random random = new Random();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel originalPixel = image[i][j];

                double noiseFactorR = random.nextGaussian() * (sigma);
                double noiseFactorG = random.nextGaussian() * (sigma);
                double noiseFactorB = random.nextGaussian() * (sigma);

                int newR = clamp((int) (originalPixel.getRouge() + noiseFactorR));
                int newG = clamp((int) (originalPixel.getVert() + noiseFactorG));
                int newB = clamp((int) (originalPixel.getBleu() + noiseFactorB));

                noisedImage[i][j] = new Pixel(newR, newG, newB);
            }
        }

        return noisedImage;
    }

    // Méthode utilitaire pour rester dans l'intervalle 0-255
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
