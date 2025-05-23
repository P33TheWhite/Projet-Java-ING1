package service;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
    
    /* Calcul qualité image pour image déjà bruité */
    
    /**
     * Converti la matrice en gris
     *
     * @param image débruité
     * @return la matrice de l'image en nuance de gris
     */
    public static double[][] convertirGris(Pixel[][] image) {
    	int rouge, vert, bleu;
    	double[][] gris = new double[image.length][image[0].length];
    	for (int i = 0 ; i < image.length; i++) {
    		for (int j = 0 ; j < image[0].length ; j++) {
    			rouge = (image[i][j].getRouge() >> 16) & 0xFF;
    			vert = (image[i][j].getVert() >> 8) & 0xFF;
    			bleu = image[i][j].getBleu() & 0xFF;
    			
    			//C.I.E recommandation 709
    			gris[i][j] = 0.2125*rouge + 0.7154*vert + 0.0721*bleu;
    		}
    	}
    	return gris;
    }
    
    /**
     * Calcul l'écart type de la matrice de gris
     *
     * @param la matrice de l'image en nuance de gris
     * @return l'écart type
     */
    public static double EcartTypeMatrice(double[][] gris) {
    	DescriptiveStatistics stats = new DescriptiveStatistics();
    	for (double[] valeurs : gris) {
    		for (double v : valeurs) {
    			stats.addValue(v);
    		}
    	}
    	return stats.getStandardDeviation();
    }
    
    /**
     * Calcul la variance de la matrice de gris
     *
     * @param la matrice de l'image en nuance de gris
     * @return l'écart type
     */
    public static double varianceMatrice(double[][] gris) {
    	return Math.pow(EcartTypeMatrice(gris), 2);
    }
    
    /**
     * Calcul la moyenne absolue
     *
     * @param la matrice de l'image en nuance de gris
     * @return la moyenne absolue
     */
    public static double moyenneAbsolue(double[][] gris) {
        double somme = 0.0;
        int total = 0;

        for (int i = 0; i < gris.length; i++) {
            for (int j = 0; j < gris[0].length; j++) {
                somme = somme + Math.abs(gris[i][j]);
                total++;
            }
        }

        return somme / total;
    }
    
    
    /**
     * Applique un filtre Laplacien 3x3 à une image en niveaux de gris.
     *
     * @param la matrice de l'image en nuance de gris
     * @return matrice résultat du filtre laplacien
     */
    public static double[][] appliquerLaplacien(double[][] gris) {
        int[][] noyau = {
                {1,  1, 1},
                {1, -8, 1},
                {1,  1, 1}
            };
        double somme;
        double[][] resultat = new double[gris.length][gris[0].length];

        for (int i = 1; i < gris.length -1 ; i++) {
            for (int j = 1 ; j < gris[0].length -1 ; j++) {
                somme = 0.0;
                for (int ki = -1; ki <= 1; ki++) {
                    for (int kj = -1; kj <= 1; kj++) {
                        somme = somme + noyau[ki + 1][kj + 1] * gris[i + ki][j + kj];
                    }
                }
                resultat[i][j] = somme;
            }
        }

        return resultat;
    }
    
    public static double maxAbsolue(double[][] matrice) {
        double max = 0.0;
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[0].length; j++) {
                max = Math.max(max, Math.abs(matrice[i][j]));
            }
        }
        return max;
    }

    
    /**
     * Calcul la netteté
     *
     * @param la matrice de l'image en nuance de gris
     * @return retourne la netteté
     */
    public static double calculerNettete(double[][] gris) {
        double[][] laplacien = appliquerLaplacien(gris);
        return 0.8 * moyenneAbsolue(laplacien) + 0.2 * maxAbsolue(laplacien);
    }


    /**
     * Applique le flou par moyenne
     *
     * @param la matrice de l'image en nuance de gris
     * @return la matrice floue
     */
    public static double[][] appliquerFlouMoyenne(double[][] gris) {
        double somme;
        double[][] floue = new double[gris.length][gris[0].length];

        for (int i = 1; i < gris.length - 1; i++) {
            for (int j = 1; j < gris[0].length - 1; j++) {
                somme = 0.0;
                for (int ki = -1; ki <= 1; ki++) {
                    for (int kj = -1; kj <= 1; kj++) {
                        somme = somme + gris[i + ki][j + kj];
                    }
                }
                floue[i][j] = somme / 9.0;
            }
        }

        return floue;
    }

    
    /**
     * Calcul le bruit
     *
     * @param la matrice de l'image en nuance de gris
     * @return le bruit
     */
    public static double calculerBruitMatrice(double[][] gris) {
        double[][] floue = appliquerFlouMoyenne(gris);
        double somme = 0.0;
        int total = 0;

        for (int i = 1; i < gris.length - 1; i++) {
            for (int j = 1; j < gris[0].length - 1; j++) {
                somme = somme + Math.abs(gris[i][j] - floue[i][j]);
                total++;
            }
        }

        return somme / total;
    }

    /**
     * Calcul le score NR-IQA (No Reference Image Quality Assessment)
     *
     * @param la matrice de l'image en nuance de gris
     * @return le score NR-IQA
     */
    public static double scoreQualiteNR_IQA(Pixel[][] image) {
        double[][] gris = convertirGris(image);
        double nettete = calculerNettete(gris);
        double netteteNorme = nettete / 2.0; 
        double contraste = EcartTypeMatrice(gris);
        double contrasteNorme = contraste / 6.0;
        double bruit = calculerBruitMatrice(gris);
        double bruitNorme = bruit / 0.2;

        System.out.println("nettete : " + nettete + " contrast " + contraste + "bruit " + bruit);
        return 0.7 * contrasteNorme - 1.2 * bruitNorme + 1.2 * netteteNorme;

    }

    public static String interpretationNR_IQA(Pixel[][] image) {
    	double scoreNR_IQA = scoreQualiteNR_IQA(image);
    	System.out.println(scoreNR_IQA);
    	String interpretation = new String("Score NR_IQA (No Reference Image Quality Assessment) : " + scoreNR_IQA + "\n");
    	if (scoreNR_IQA > 7) {
    		interpretation = interpretation + "Qualité Excellente";
    	} else if (scoreNR_IQA > 3.6) {
    		interpretation = interpretation + "Bonne Qualité";
    	} else {
    		interpretation = interpretation + "Mauvaise Qualité";
    	}
    	return interpretation;
    }

    
    
}
