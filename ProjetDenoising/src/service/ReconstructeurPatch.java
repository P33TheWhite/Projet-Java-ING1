package service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import service.Convert;

import model.*;
public class ReconstructeurPatch {
	
	public static Photo reconstructPatch(ArrayList<Patch> yPatch, int l, int c) {
	    ArrayList<ArrayList<Pixel>> matrice = new ArrayList<>();
	    Pixel[][] matriceFinale;
	    Photo photo;
	    ArrayList<ArrayList<Integer>> nbPixel = new ArrayList<>();
	    BufferedImage image;

	    for (int i = 0; i < l; i++) {
	        ArrayList<Pixel> lignePixel = new ArrayList<>();
	        ArrayList<Integer> ligneCompteur = new ArrayList<>();
	        for (int j = 0; j < c; j++) {
	            lignePixel.add(new Pixel(0, 0, 0));
	            ligneCompteur.add(0);
	        }
	        matrice.add(lignePixel);
	        nbPixel.add(ligneCompteur);
	    }

	    ajouterPatchs(yPatch, matrice, nbPixel);
	    matriceFinale = trimMatrice(matrice, nbPixel);
	    image = Convert.convertirMatriceEnImage(matriceFinale);
	    photo = new Photo(image, matriceFinale.length, matriceFinale[0].length, matriceFinale);
	    return photo;
	}

	private static void ajouterPatchs(ArrayList<Patch> yPatch, ArrayList<ArrayList<Pixel>> matrice, ArrayList<ArrayList<Integer>> nbPixel) {
	    for (Patch p : yPatch) {
	        int x = p.getPosition()[0];
	        int y = p.getPosition()[1];
	        Pixel[][] m = p.getMatrice();

	        for (int i = 0; i < m.length; i++) {
	            for (int j = 0; j < m[0].length; j++) {
	                int xi = x + i;
	                int yj = y + j;
	                if (xi >= matrice.size() || yj >= matrice.get(0).size()) continue;

	                int nbUtilisation = nbPixel.get(xi).get(yj);
	                Pixel pixel;
	                if (nbUtilisation == 0) {
	                    pixel = m[i][j];
	                } else {
	                    Pixel current = matrice.get(xi).get(yj);
	                    pixel = new Pixel(
	                        (current.getRouge() * nbUtilisation + m[i][j].getRouge()) / (nbUtilisation + 1),
	                        (current.getVert() * nbUtilisation + m[i][j].getVert()) / (nbUtilisation + 1),
	                        (current.getBleu() * nbUtilisation + m[i][j].getBleu()) / (nbUtilisation + 1)
	                    );
	                }

	                matrice.get(xi).set(yj, pixel);
	                nbPixel.get(xi).set(yj, nbUtilisation + 1);
	            }
	        }
	    }
	}

	private static Pixel[][] trimMatrice(ArrayList<ArrayList<Pixel>> matrice, ArrayList<ArrayList<Integer>> nbPixel) {
		int l = nbPixel.size();
		int c = nbPixel.get(0).size();
		int minI = l, maxI = -1;
	    int minJ = c, maxJ = -1;

	    // Parcours pour détecter les bornes utiles
	    for (int i = 0; i < l; i++) {
	        for (int j = 0; j < c; j++) {
	            if (nbPixel.get(i).get(j) > 0) {
	                if (i < minI) minI = i;
	                if (i > maxI) maxI = i;
	                if (j < minJ) minJ = j;
	                if (j > maxJ) maxJ = j;
	            }
	        }
	    }

	    // Si aucune case utile trouvée
	    if (maxI < minI || maxJ < minJ) {
	        return new Pixel[0][0]; // matrice vide
	    }

	    int hauteur = maxI - minI + 1;
	    int largeur = maxJ - minJ + 1;
	    Pixel[][] matriceFinale = new Pixel[hauteur][largeur];

	    for (int i = 0; i < hauteur; i++) {
	        for (int j = 0; j < largeur; j++) {
	            matriceFinale[i][j] = matrice.get(minI + i).get(minJ + j);
	        }
	    }

	    System.out.println("Découpage utile -> Hauteur: " + hauteur + ", Largeur: " + largeur);
	    return matriceFinale;
	}

}


