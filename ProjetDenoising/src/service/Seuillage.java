package service;

public class Seuillage {

    public static double[][] seuillageDoux(double[][] alpha, double seuil) {
        if (alpha == null || alpha.length == 0 || alpha[0] == null) {
            throw new IllegalArgumentException("Matrice alpha invalide");
        }
        
        int M = alpha.length;
        int s2 = alpha[0].length;

        double[][] alphaSeuilles = new double[M][s2];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < s2; j++) {
                double a = alpha[i][j];
                // Seuillage doux : sign(a) * max(|a| - seuil, 0
                alphaSeuilles[i][j] = Math.signum(a) * Math.max(Math.abs(a) - seuil, 0);
            }
        }
        return alphaSeuilles;
    }

    public static double[][] seuillageDur(double[][] alpha, double seuil) {
        if (alpha == null || alpha.length == 0 || alpha[0] == null) {
            throw new IllegalArgumentException("Matrice alpha invalide");
        }
        
        int M = alpha.length;
        int s2 = alpha[0].length;

        double[][] alphaSeuilles = new double[M][s2];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < s2; j++) {
                double a = alpha[i][j];
                // Seuillage dur: a si |a| >= seuil, 0 sinon
                alphaSeuilles[i][j] = Math.abs(a) >= seuil ? a : 0.0;
            }
        }
        return alphaSeuilles;
    }
    

    public static double ajusterSeuilPourSeuillageDur(double seuilDoux) {
    	//3.18 : optimum théorique calculer
        return seuilDoux * 3.2;
    }
}