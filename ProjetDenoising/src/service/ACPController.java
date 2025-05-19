package service;

import model.Vector;
import model.Photo;
import ui.MainView;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ACPController {

    private final boolean modeGlobal;
    private final MainView view;
    private BufferedImage derniereImageReconstruite;

    /**
     * Constructeur unifié
     * @param data soit List<Vector>, soit List<List<Vector>>
     * @param seuilDur true = seuillage dur
     * @param isLocal true = mode local (par imagette), false = global
     * @param view l'interface graphique
     */
    public ACPController(Object data, boolean seuilDur, boolean isLocal, MainView view) {
        this.view = view;
        this.modeGlobal = !isLocal;

        if (isLocal) {
            System.out.println("==== ACP LOCALE ====");
            @SuppressWarnings("unchecked")
            List<List<Vector>> liste = (List<List<Vector>>) data;
            this.derniereImageReconstruite = reconstruireImageParImagettes(liste, seuilDur);
        } else {
            System.out.println("==== ACP GLOBALE ====");
            @SuppressWarnings("unchecked")
            List<Vector> liste = (List<Vector>) data;
            this.derniereImageReconstruite = reconstruireImageGlobale(liste, seuilDur);
        }

        view.setReconstructedImage(derniereImageReconstruite);
    }

    public BufferedImage getDerniereImageReconstruite() {
        return derniereImageReconstruite;
    }

    private BufferedImage reconstruireImageGlobale(List<Vector> vecteurs, boolean seuilDur) {
        ACPParCanal acpR = new ACPParCanal(vecteurs, "R");
        ACPParCanal acpG = new ACPParCanal(vecteurs, "G");
        ACPParCanal acpB = new ACPParCanal(vecteurs, "B");

        acpR.afficherDebug("R");
        acpG.afficherDebug("G");
        acpB.afficherDebug("B");

        double[][] alphaR = seuilDur ? acpR.seuillageDur(acpR.projeterDansBase(), 50) : acpR.seuillageDoux(acpR.projeterDansBase(), 0.1);
        double[][] alphaG = seuilDur ? acpG.seuillageDur(acpG.projeterDansBase(), 50) : acpG.seuillageDoux(acpG.projeterDansBase(), 0.1);
        double[][] alphaB = seuilDur ? acpB.seuillageDur(acpB.projeterDansBase(), 50) : acpB.seuillageDoux(acpB.projeterDansBase(), 0.1);

        double[][] reconstruitR = acpR.reconstruireDepuisBase(alphaR);
        double[][] reconstruitG = acpG.reconstruireDepuisBase(alphaG);
        double[][] reconstruitB = acpB.reconstruireDepuisBase(alphaB);

        Photo photo = ReconstructeurPatch.reconstruireImageDepuisACP(vecteurs, reconstruitR, reconstruitG, reconstruitB);
        return photo.getImage();
    }

    private BufferedImage reconstruireImageParImagettes(List<List<Vector>> vecteursParImagette, boolean seuilDur) {
        List<BufferedImage> imagesReconstruites = new ArrayList<>();

        for (int k = 0; k < vecteursParImagette.size(); k++) {
            List<Vector> vecteurs = vecteursParImagette.get(k);
            System.out.println(">>> ACP sur Imagette " + k);

            ACPParCanal acpR = new ACPParCanal(vecteurs, "R");
            ACPParCanal acpG = new ACPParCanal(vecteurs, "G");
            ACPParCanal acpB = new ACPParCanal(vecteurs, "B");

            acpR.afficherDebug("R");
            acpG.afficherDebug("G");
            acpB.afficherDebug("B");

            double[][] alphaR = seuilDur ? acpR.seuillageDur(acpR.projeterDansBase(), 30) : acpR.seuillageDoux(acpR.projeterDansBase(), 0.2);
            double[][] alphaG = seuilDur ? acpG.seuillageDur(acpG.projeterDansBase(), 30) : acpG.seuillageDoux(acpG.projeterDansBase(), 0.2);
            double[][] alphaB = seuilDur ? acpB.seuillageDur(acpB.projeterDansBase(), 30) : acpB.seuillageDoux(acpB.projeterDansBase(), 0.2);

            double[][] reconstruitR = acpR.reconstruireDepuisBase(alphaR);
            double[][] reconstruitG = acpG.reconstruireDepuisBase(alphaG);
            double[][] reconstruitB = acpB.reconstruireDepuisBase(alphaB);

            Photo photo = ReconstructeurPatch.reconstruireImageDepuisACP(vecteurs, reconstruitR, reconstruitG, reconstruitB);
            imagesReconstruites.add(photo.getImage());
        }

        return imagesReconstruites.get(0); // ⚠️ Fusion complète à implémenter si besoin
    }
}
