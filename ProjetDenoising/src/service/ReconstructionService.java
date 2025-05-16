package service;

import java.awt.image.BufferedImage;
import java.util.List;

import model.Vector;

public class ReconstructionService {

    public BufferedImage reconstruireImageDepuisACP(double[][] canalRReconstruit, double[][] canalGReconstruit, double[][] canalBReconstruit, List<Vector> vecteurs, int largeurImage, int hauteurImage, int taillePatch) {
        
    	BufferedImage image = new BufferedImage(largeurImage, hauteurImage, BufferedImage.TYPE_INT_RGB);

        int[][][] compteur = new int[hauteurImage][largeurImage][3];  // Compteur chevauchement
        int[][] sommeR = new int[hauteurImage][largeurImage];
        int[][] sommeG = new int[hauteurImage][largeurImage];
        int[][] sommeB = new int[hauteurImage][largeurImage];

        for (int idx = 0; idx < vecteurs.size(); idx++) {
            int[] pos = vecteurs.get(idx).getPremierPixelPos();
            int x = pos[0];
            int y = pos[1];
            int k = 0;

            for (int i = 0; i < taillePatch; i++) {
                for (int j = 0; j < taillePatch; j++) {
                    int px = x + j;
                    int py = y + i;

                    if (px >= largeurImage || py >= hauteurImage) continue;

                    int r = (int) canalRReconstruit[idx][k];
                    int g = (int) canalGReconstruit[idx][k];
                    int b = (int) canalBReconstruit[idx][k];
                    k++;

                    sommeR[py][px] += r;
                    sommeG[py][px] += g;
                    sommeB[py][px] += b;
                    compteur[py][px][0]++;
                }
            }
        }

        for (int y = 0; y < hauteurImage; y++) {
            for (int x = 0; x < largeurImage; x++) {
                int count = compteur[y][x][0];
                if (count == 0) continue;

                int r = sommeR[y][x] / count;
                int g = sommeG[y][x] / count;
                int b = sommeB[y][x] / count;

                // Clamp entre 0 et 255
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}
