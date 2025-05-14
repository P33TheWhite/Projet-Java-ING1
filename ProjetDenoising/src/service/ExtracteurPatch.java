package service;

import model.Patch;
import model.Pixel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ExtracteurPatch {

    /**
     * Extrait une matrice de patchs réels à partir d'une image BufferedImage.
     *
     * @param image        l'image source
     * @param pas          le pas (stride) entre deux patchs
     * @param taillePatch  la taille (largeur et hauteur) du patch carré
     * @return             une matrice (liste de listes) de patchs extraits
     */
    public static ArrayList<ArrayList<Patch>> extractPatchs(BufferedImage image, int pas, int taillePatch) {
        ArrayList<ArrayList<Patch>> matricePatchs = new ArrayList<>();

        int largeur = image.getWidth();
        int hauteur = image.getHeight();

        // Convertir BufferedImage en matrice de Pixels
        Pixel[][] matriceImage = new Pixel[hauteur][largeur];
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                matriceImage[y][x] = new Pixel(r, g, b);
            }
        }

        // Marquer les pixels déjà utilisés
        boolean[][] dejaUtilise = new boolean[hauteur][largeur];

        for (int y = 0; y <= hauteur - taillePatch; y += pas) {
            ArrayList<Patch> lignePatchs = new ArrayList<>();

            for (int x = 0; x <= largeur - taillePatch; x += pas) {
                Pixel[][] matricePatch = new Pixel[taillePatch][taillePatch];

                for (int i = 0; i < taillePatch; i++) {
                    for (int j = 0; j < taillePatch; j++) {
                        int coordY = y + i;
                        int coordX = x + j;

                        Pixel original = matriceImage[coordY][coordX];
                        Pixel copie = new Pixel(original.getRouge(), original.getVert(), original.getBleu());

                        // Vérifier le chevauchement
                        if (dejaUtilise[coordY][coordX]) {
                            copie.setestSuperpose(true);
                        } else {
                            copie.setestSuperpose(false);
                            dejaUtilise[coordY][coordX] = true;
                        }

                        matricePatch[i][j] = copie;
                    }
                }

                Patch patch = new Patch();
                patch.setMatrice(matricePatch);
                patch.setImage(image.getSubimage(x, y, taillePatch, taillePatch));
                patch.setPremierPixelPos(new int[]{x, y});  // position absolue du premier pixel
                patch.setPosition(new int[]{x, y});  // position absolue du patch dans l'image
 // position dans la grille
                patch.setS(pas);  // facultatif mais utile

                lignePatchs.add(patch);
            }

            matricePatchs.add(lignePatchs);
        }

        return matricePatchs;
    }
}
