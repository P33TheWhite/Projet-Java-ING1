package service;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import model.Pixel;
import model.Patch;

public class ExtracteurPatch {
    
    public static double steps = 0.5; // Variable globale pour déterminer le pourcentage de superposition des patches

    // Fonction ExtractPatchs
    public List<Patch> ExtractPatchs(BufferedImage Xb, int s) {
        List<Patch> patches = new ArrayList<>();
        
        int largeurImage = Xb.getWidth();
        int hauteurImage = Xb.getHeight();
        
        // Calcul des pas de déplacement pour l'extraction des patches
        int pas = (int) (s * (1 - steps));
        
        for (int y = 0; y + s <= hauteurImage; y += pas) {
            for (int x = 0; x + s <= largeurImage; x += pas) {
                Patch patch = new Patch();
                patch.setS(s);
                
                // Extraire la sous-image
                BufferedImage patchImage = Xb.getSubimage(x, y, s, s);
                patch.setImage(patchImage);
                
                // Créer la matrice de pixels
                Pixel[][] matrice = new Pixel[s][s];
                for (int i = 0; i < s; i++) {
                    for (int j = 0; j < s; j++) {
                        int rgb = patchImage.getRGB(j, i);
                        matrice[i][j] = Pixel.fromRGB(rgb);
                    }
                }
                patch.setMatrice(matrice);
                
                // Définir les positions
                patch.setPosition(new int[] {x, y});
                patch.setPremierPixelPos(new int[] {x, y});
                
                // Marquer si le patch est superposé
                patch.setSuperpose(steps < 1.0);
                
                patches.add(patch);
            }
        }
        
        return patches;
    }

    // Fonction DecoupeImage
    public List<Patch> DecoupeImage(BufferedImage X, int W, int n) {
        List<Patch> patches = new ArrayList<>();
        
        int largeurImage = X.getWidth();
        int hauteurImage = X.getHeight();
        
        // Calculer le nombre de patches par ligne et colonne
        int patchesPerRow = largeurImage / W;
        int patchesPerCol = hauteurImage / W;
        int totalPatches = patchesPerRow * patchesPerCol;
        
        // Limiter n au nombre maximum possible de patches
        if (n > totalPatches) {
            n = totalPatches;
        }
        
        for (int i = 0; i < n; i++) {
            int x = (i % patchesPerRow) * W;
            int y = (i / patchesPerRow) * W;
            
            Patch patch = new Patch();
            patch.setS(W);
            
            BufferedImage patchImage = X.getSubimage(x, y, W, W);
            patch.setImage(patchImage);
            
            Pixel[][] matrice = new Pixel[W][W];
            for (int j = 0; j < W; j++) {
                for (int k = 0; k < W; k++) {
                    int rgb = patchImage.getRGB(k, j);
                    matrice[j][k] = Pixel.fromRGB(rgb);
                }
            }
            patch.setMatrice(matrice);
            
            patch.setPosition(new int[] {x, y});
            patch.setPremierPixelPos(new int[] {x, y});
            
            patches.add(patch);
        }
        
        return patches;
    }
}