package service;

import java.awt.image.BufferedImage;

public class EstimationBruit {

    /**
     * Estime le sigma du bruit d'une image en analysant les différences entre pixels voisins.
     * Méthode basée sur la variance locale (utilisation des gradients horizontaux et verticaux).
     *
     * @param image image bruitée (BufferedImage)
     * @return estimation du sigma du bruit
     */
    public static double estimerSigma(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double sommeDiffs = 0;
        int nbPixels = 0;

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int rgb = image.getRGB(x, y);
                int rgbRight = image.getRGB(x + 1, y);
                int rgbDown = image.getRGB(x, y + 1);

                int gray = getGray(rgb);
                int grayRight = getGray(rgbRight);
                int grayDown = getGray(rgbDown);

                int dx = gray - grayRight;
                int dy = gray - grayDown;

                sommeDiffs += dx * dx + dy * dy;
                nbPixels += 2;
            }
        }

        double variance = sommeDiffs / nbPixels;
        return Math.sqrt(variance / 2.0); // Approximation de sigma
    }

    private static int getGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }
}
