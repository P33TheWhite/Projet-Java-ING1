package service;

import model.Patch;
import model.Vector;
import model.Pixel;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe gérant une collection de vecteurs issus de patches d'image.
 * Permet d'ajouter des vecteurs à partir de listes de patches,
 * d'extraire les données des canaux R, G, B et d'afficher des extraits.
 */
public class VecteurPatch {
    private final List<Vector> vecteurs;

    public VecteurPatch() {
        this.vecteurs = new ArrayList<>();
    }

    /**
     * Ajoute à la liste interne des vecteurs extraits de listes de patches.
     * 
     * @param liste Liste de listes de patches (ex : List<ArrayList<Patch>> ou ArrayList<ArrayList<Patch>>)
     */
    public void ajouterDepuisListe(List<? extends List<Patch>> liste) {
        for (List<Patch> patchList : liste) {
            for (Patch p : patchList) {
                Pixel[][] matrice = p.getMatrice();
                int hauteur = matrice.length;
                int largeur = matrice[0].length;

                // Vérification que le patch est carré
                if (hauteur != largeur) {
                    System.err.println("❌ Patch non carré détecté : " + hauteur + "x" + largeur +
                            " à la position " + p.getPremierPixelPos()[0] + "," + p.getPremierPixelPos()[1]);
                    continue;
                }

                try {
                    Vector v = new Vector(matrice, hauteur, p.getPremierPixelPos());
                    vecteurs.add(v);
                } catch (IllegalArgumentException e) {
                    System.err.println("Erreur de vectorisation : " + e.getMessage());
                }
            }
        }
    }

    /**
     * Extrait les données d'un canal (R, G ou B) de tous les vecteurs.
     * 
     * @param canal Nom du canal ("R", "G", ou "B")
     * @return Matrice contenant les valeurs du canal pour chaque vecteur
     */
    public double[][] getCanal(String canal) {
        int n = vecteurs.size();
        int taille = vecteurs.get(0).getS2();
        double[][] data = new double[n][taille];

        for (int i = 0; i < n; i++) {
            data[i] = vecteurs.get(i).extraireCanal(canal);
        }

        return data;
    }

    /**
     * Affiche un extrait des premiers pixels des canaux R, G et B
     * pour un nombre donné de vecteurs et de valeurs par vecteur.
     * 
     * @param combien Nombre d'éléments à afficher par canal
     */
    public void afficherExtraits(int combien) {
        System.out.println(">>> Premiers pixels du canal R :");
        afficherCanal("R", combien);
        System.out.println(">>> Premiers pixels du canal G :");
        afficherCanal("G", combien);
        System.out.println(">>> Premiers pixels du canal B :");
        afficherCanal("B", combien);
    }

    /**
     * Affiche les valeurs d'un canal donné pour un certain nombre de vecteurs.
     * 
     * @param canal Nom du canal ("R", "G", ou "B")
     * @param combien Nombre d'éléments à afficher
     */
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

    /**
     * Retourne la liste interne des vecteurs.
     * 
     * @return Liste des vecteurs
     */
    public List<Vector> getVecteurs() {
        return this.vecteurs;
    }
}
