package service;

import model.Pixel;

/**
 * Classe utilitaire pour évaluer la qualité d'une image en utilisant des métriques objectives
 * telles que le MSE (Mean Squared Error) et le PSNR (Peak Signal-to-Noise Ratio).
 */
public class QualiteImage {

    // Seuils de qualité PSNR
    private static final double PSNR_EXCELLENT = 40.0;
    private static final double PSNR_BON = 30.0;
    private static final double PSNR_MOYEN = 20.0;
    private static final int VALEUR_PIXEL_MAX = 255;

    /**
     * Calcule l'erreur quadratique moyenne (MSE) entre deux images.
     *
     * @param original l'image d'origine
     * @param traite   l'image traitée
     * @return la valeur du MSE
     * @throws IllegalArgumentException si les images sont nulles, vides ou de dimensions différentes
     */
    public static double calculerMSE(Pixel[][] original, Pixel[][] traite) throws IllegalArgumentException {
        validerImages(original, traite);

        double sommeErreursCarrees = 0;
        int nombrePixels = original.length * original[0].length;

        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[0].length; j++) {
                sommeErreursCarrees += calculerErreurPixel(original[i][j], traite[i][j]);
            }
        }

        return sommeErreursCarrees / (nombrePixels * 3); // 3 canaux (R, G, B)
    }

    /**
     * Calcule le PSNR à partir d'une valeur MSE.
     *
     * @param mse la valeur de l'erreur quadratique moyenne
     * @return le PSNR en décibels
     */
    public static double calculerPSNR(double mse) {
        if (mse <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return 10 * Math.log10(Math.pow(VALEUR_PIXEL_MAX, 2) / mse);
    }

    /**
     * Évalue la qualité d'une image traitée par rapport à une image originale.
     * Retourne un rapport textuel contenant le MSE, le PSNR et une appréciation qualitative.
     *
     * @param original l'image d'origine
     * @param traite   l'image traitée
     * @return une chaîne contenant l'analyse de la qualité de l'image
     */
    public static String evaluerQualiteImage(Pixel[][] original, Pixel[][] traite) {
        try {
            double mse = calculerMSE(original, traite);
            double psnr = calculerPSNR(mse);

            return String.format(
                "Métriques de qualité:\n" +
                "MSE: %.4f\n" +
                "PSNR: %.2f dB\n" +
                "Évaluation de qualité: %s",
                mse, psnr, evaluerQualite(psnr)
            );
        } catch (IllegalArgumentException e) {
            return "Erreur: " + e.getMessage();
        }
    }

    // Évalue le niveau de qualité en fonction du PSNR
    private static String evaluerQualite(double psnr) {
        if (Double.isInfinite(psnr)) {
            return "Qualité parfaite (images identiques)";
        } else if (psnr >= PSNR_EXCELLENT) {
            return "Qualité excellente";
        } else if (psnr >= PSNR_BON) {
            return "Bonne qualité";
        } else if (psnr >= PSNR_MOYEN) {
            return "Qualité moyenne";
        } else {
            return "Mauvaise qualité";
        }
    }

    // Calcule l'erreur au carré pour un pixel entre deux images
    private static double calculerErreurPixel(Pixel original, Pixel traite) {
        return Math.pow(original.getRouge() - traite.getRouge(), 2) +
               Math.pow(original.getVert() - traite.getVert(), 2) +
               Math.pow(original.getBleu() - traite.getBleu(), 2);
    }

    // Vérifie que les deux images sont valides et de même dimension
    private static void validerImages(Pixel[][] original, Pixel[][] traite) {
        if (original == null || traite == null) {
            throw new IllegalArgumentException("Les images ne peuvent pas être nulles");
        }
        if (original.length == 0 || original[0].length == 0 || 
            traite.length == 0 || traite[0].length == 0) {
            throw new IllegalArgumentException("Les images ne peuvent pas être vides");
        }
        if (original.length != traite.length || original[0].length != traite[0].length) {
            throw new IllegalArgumentException("Les images doivent avoir les mêmes dimensions");
        }
    }
}
