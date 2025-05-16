package service;

import model.Patch;
import model.Vector;
import model.Pixel;

import java.util.ArrayList;
import java.util.List;

public class VecteurPatch {
    private List<Vector> vecteurs;

    public VecteurPatch() {
        this.vecteurs = new ArrayList<>();
    }

    // Gerer des List<ArrayList<Patch>> et ArrayList<ArrayList<Patch>>
    public void ajouterDepuisListe(List<? extends List<Patch>> liste) {

        for (List<Patch> patchList : liste) {
            for (Patch p : patchList) {
                Pixel[][] matrice = p.getMatrice();
                int h = matrice.length;
                int w = matrice[0].length;

                if (h != w) {
                    System.err.println("Patch non carré détecté : " + h + "x" + w +
                            " à la position " + p.getPremierPixelPos()[0] + "," + p.getPremierPixelPos()[1]);
                    continue;
                }
                
                try {
                    Vector v = new Vector(matrice, h, p.getPremierPixelPos());
                    vecteurs.add(v);
                } catch (IllegalArgumentException e) {
                    System.err.println("Erreur de vectorisation : " + e.getMessage());
                }
            }
        }
    }

    public double[][] getCanal(String canal) {
        int n = vecteurs.size();
        int taille = vecteurs.get(0).getS2();
        double[][] data = new double[n][taille];

        for (int i = 0; i < n; i++) {
            data[i] = vecteurs.get(i).extraireCanal(canal);
        }
        return data;
    }

    public void afficherExtraits(int combien) {
        System.out.println(">>> Premiers pixels du canal R :");
        afficherCanal("R", combien);
        System.out.println(">>> Premiers pixels du canal G :");
        afficherCanal("G", combien);
        System.out.println(">>> Premiers pixels du canal B :");
        afficherCanal("B", combien);
    }

    private void afficherCanal(String canal, int combien) {
        double[][] canalData = getCanal(canal);
        for (int i = 0; i < Math.min(combien, canalData.length); i++) {
            System.out.print("[" + i + "] ");
            for (int j = 0; j < Math.min(combien, canalData[i].length); j++) {
                System.out.printf("%.1f ", canalData[i][j]);
            }
            System.out.println();
        }
    }
    
    public List<Vector> getVecteurs() {
        return this.vecteurs;
    }
}