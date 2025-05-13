package service;

import model.Patch;
import model.Photo;
import model.Pixel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ExtracteurPatch {

    public static ArrayList<Patch> extractPatchs(Photo photo, int taillePatch) {
        ArrayList<Patch> listePatchs = new ArrayList<>();

        BufferedImage image = photo.getImage();
        Pixel[][] matriceImage = photo.getMatrice();
        int largeur = photo.getLargeur();
        int hauteur = photo.getHauteur();

        
        // voir les pixels utilisés
        boolean[][] dejaUtilise = new boolean[hauteur][largeur];

        for (int y = 0; y <= hauteur-taillePatch; y += taillePatch) {
            for (int x = 0; x <= largeur-taillePatch; x += taillePatch) {

            	int decalageX = Math.min(x, largeur-taillePatch);
            	int decalageY = Math.min(y, hauteur-taillePatch);

                Pixel[][] matricePatch = new Pixel[taillePatch][taillePatch];

                for (int i = 0; i < taillePatch; i++) {
                    for (int j = 0; j < taillePatch; j++) {
                        int coordY = decalageY + i;
                        int coordX = decalageX + j;

                        Pixel original = matriceImage[coordY][coordX];
                        Pixel copie = new Pixel(original.getRouge(), original.getVert(), original.getBleu());

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
                patch.setImage(image.getSubimage(decalageX, decalageY, taillePatch, taillePatch));
                patch.setPremierPixelPos(new int[]{decalageX, decalageY});

                listePatchs.add(patch);
            }
        }

        return listePatchs;
    }
}
