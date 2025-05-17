package ui;

import javafx.scene.control.ChoiceDialog;
import model.Imagette;
import model.Patch;
import model.Photo;
import model.Pixel;
import service.ACP;
import service.ACPResult;
import service.Bruit;
import service.Convert;
import service.DecoupeImage;
import service.ExtracteurPatch;
import service.Proj;
import service.QualiteImage;
import service.Reconstruction;
import service.ReconstructionService;
import service.Seuillage;
import service.VecteurPatch;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DenoisingController {
    // Constantes
    private static final double SEUIL_ENERGIE = 0.98; // 0,95 de base
    private static final double FACTEUR_SEUIL_GLOBAL = 0.6745; // de base 1.349 = 0.6745 * 2
    private static final double FACTEUR_SEUIL_LOCAL = 0.8094; // de base 1.01175 = 0.6745 * 1.5 et la x1,2
    private static final int VALEUR_PIXEL_MAX = 255;
    
    private final MainView view;
    private Pixel[][] matriceOriginale;
    private BufferedImage imageBruitee;
    private BufferedImage imageOriginale;
    private BufferedImage imageDebruitee;
    private int maxDivisions = 1;
    private List<Imagette> currentImagettes;
    private boolean isModeGlobal = false;
    private VecteurPatch vecteurPatch;
    private double[][][] canauxACP; // [0]=R, [1]=G, [2]=B
    private ACPResult[] acpResults;

    public DenoisingController(MainView view) {
        this.view = view;
        initialiser();
    }

    private void initialiser() {
        view.setOnImageSelected(this::gererSelectionImage);
        view.setOnNoiseChanged(this::mettreAJourImageBruitee);
        view.setOnSaveRequested(this::sauvegarderImageBruitee);
        view.setOnCutRequested(this::decouperImage);
        view.setOnExtractPatchesRequested(this::extrairePatchs);
        view.setOnDenoiseRequested(this::effectuerDebruitage);
    }

    public void setModeGlobal(boolean modeGlobal) {
        this.isModeGlobal = modeGlobal;
        if (modeGlobal) {
            view.lancerModeGlobal();
        } else {
            view.lancerModeLocal();
        }
    }

    private void gererSelectionImage(File fichier) {
        try {
            BufferedImage imageChargee = ImageIO.read(fichier);
            int largeur = imageChargee.getWidth();
            int hauteur = imageChargee.getHeight();
            
            // Taille divisible par 16
            int nouvelleLargeur = largeur - (largeur % 16);
            int nouvelleHauteur = hauteur - (hauteur % 16);
            
            if (nouvelleLargeur != largeur || nouvelleHauteur != hauteur) {
                imageOriginale = redimensionnerImage(imageChargee, nouvelleLargeur, nouvelleHauteur);
                view.showError("Image redimensionnée à " + nouvelleLargeur + "x" + nouvelleHauteur +
                        " pour correspondre à une taille divisible par 16");
            } else {
                imageOriginale = imageChargee;
                view.showError("");
            }

            Photo photo = new Photo(imageOriginale, imageOriginale.getWidth(), imageOriginale.getHeight());
            matriceOriginale = Convert.convertirImageEnMatrice(photo);
            view.setOriginalImage(imageOriginale);
            view.setPossiblePatchSizes(imageOriginale.getWidth(), imageOriginale.getHeight());
            mettreAJourImageBruitee(view.getNoiseLevel());
            view.enableSave(true);
            view.enableCut(!isModeGlobal);

            // Nombre maximum de divisions
            maxDivisions = (imageOriginale.getWidth() / 16) * (imageOriginale.getHeight() / 16);
            if (maxDivisions < 1) maxDivisions = 1;

        } catch (IOException e) {
            view.showError("Erreur de lecture de l'image");
            e.printStackTrace();
        }
    }

    private void mettreAJourImageBruitee(double niveauBruit) {
        if (matriceOriginale != null) {
            Pixel[][] matriceBruitee = Bruit.noising(matriceOriginale, niveauBruit);
            imageBruitee = Convert.convertirMatriceEnImage(matriceBruitee);
            view.setNoisyImage(imageBruitee);
        }
    }

    private void sauvegarderImageBruitee(File fichierSauvegarde) {
        if (imageBruitee != null && fichierSauvegarde != null) {
            try {
                ImageIO.write(imageBruitee, "png", fichierSauvegarde);
                view.showError("Image sauvegardée avec succès");
            } catch (IOException e) {
                view.showError("Erreur lors de la sauvegarde");
                e.printStackTrace();
            }
        }
    }

    private void extrairePatchs() {
        int taillePatch = view.getPatchSize();
        int pas = view.getPatchStep();

        if (isModeGlobal) {
            if (imageBruitee == null) {
                view.showError("Veuillez d'abord charger une image et appliquer du bruit");
                return;
            }

            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(imageBruitee, pas, taillePatch);
            List<ArrayList<ArrayList<Patch>>> wrapper = new ArrayList<>();
            wrapper.add(patches);
            view.displayPatches(wrapper);

            vecteurPatch = new VecteurPatch();
            vecteurPatch.ajouterDepuisListe(patches);
            vecteurPatch.afficherExtraits(10);

            // Extraction des canaux pour ACP
            canauxACP = new double[3][][];
            canauxACP[0] = vecteurPatch.getCanal("R");
            canauxACP[1] = vecteurPatch.getCanal("G");
            canauxACP[2] = vecteurPatch.getCanal("B");

            view.enableDenoise(true);
            return;
        }

        // Mode local
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            view.showError("Veuillez d'abord découper l'image en imagettes");
            return;
        }

        List<ArrayList<ArrayList<Patch>>> allPatches = new ArrayList<>();
        
        for (Imagette imagette : currentImagettes) {
            BufferedImage img = imagette.getImage();
            if (img.getWidth() < taillePatch || img.getHeight() < taillePatch) {
                view.showError("La taille de patch est trop grande pour certaines imagettes");
                return;
            }

            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(img, pas, taillePatch);
            allPatches.add(patches);
        }

        view.displayPatches(allPatches);
        view.enableDenoise(true);
    }

    private void effectuerDebruitage() {
        if (isModeGlobal) {
            effectuerDebruitageGlobal();
        } else {
            effectuerDebruitageLocal();
        }
        
        // Comparaison de qualité après débruitage
        comparerQualiteImages();
    }

    private void effectuerDebruitageGlobal() {
        try {
            validerPreconditionsPourDebruitageGlobal();
            
            StrategieSeuillage strategie = obtenirStrategieSeuillageDepuisUtilisateur();
            if (strategie == null) return;

            traiterTousLesCanaux(strategie, true);
            
            imageDebruitee = reconstruireImage();
            view.setDenoisedImage(imageDebruitee);
            
            afficherMessageCompletion(strategie, true);
        } catch (IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    private void effectuerDebruitageLocal() {
        try {
            validerPreconditionsPourDebruitageLocal();
            
            StrategieSeuillage strategie = obtenirStrategieSeuillageDepuisUtilisateur();
            if (strategie == null) return;

            imageDebruitee = traiterImagettes(strategie);
            view.setDenoisedImage(imageDebruitee);
            
            afficherMessageCompletion(strategie, false);
        } catch (IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    // Méthodes de validation
    private void validerPreconditionsPourDebruitageGlobal() {
        if (vecteurPatch == null || canauxACP == null) {
            throw new IllegalStateException("Veuillez d'abord extraire les patchs");
        }
    }

    private void validerPreconditionsPourDebruitageLocal() {
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            throw new IllegalStateException("Veuillez d'abord découper l'image en imagettes");
        }
    }

    // Sélection du seuillage
    private StrategieSeuillage obtenirStrategieSeuillageDepuisUtilisateur() {
        List<String> choix = Arrays.asList("Seuillage dur", "Seuillage doux");
        ChoiceDialog<String> dialogue = new ChoiceDialog<>("Seuillage dur", choix);
        dialogue.setTitle("Choix du seuillage");
        dialogue.setHeaderText("Sélectionnez le type de seuillage à appliquer");
        dialogue.setContentText("Méthode:");

        Optional<String> resultat = dialogue.showAndWait();
        if (!resultat.isPresent()) return null;

        return resultat.get().equals("Seuillage dur") 
            ? new StrategieSeuillageDur() 
            : new StrategieSeuillageDoux();
    }

    // Traitement des canaux pour le débruitage global
    private void traiterTousLesCanaux(StrategieSeuillage strategie, boolean isGlobal) {
        acpResults = new ACPResult[3];
        String[] nomsCanaux = {"Rouge", "Vert", "Bleu"};
        int[] composantesConservees = new int[3];
        
        for (int i = 0; i < 3; i++) {
            acpResults[i] = ACP.appliquerACP(canauxACP[i]);
            composantesConservees[i] = determinerComposantesAConserver(acpResults[i]);
            
            double seuil = calculerSeuil(acpResults[i], composantesConservees[i], isGlobal);
            appliquerSeuillage(strategie, acpResults[i], seuil, composantesConservees[i]);
        }
    }

    private int determinerComposantesAConserver(ACPResult resultatACP) {
        double energieTotale = Arrays.stream(resultatACP.getValeursPropres()).sum();
        double energieCourante = 0;
        int k = 0;
        
        for (; k < resultatACP.getValeursPropres().length; k++) {
            energieCourante += resultatACP.getValeursPropres()[k];
            if (energieCourante >= SEUIL_ENERGIE * energieTotale) {
                break;
            }
        }
        return k + 1;
    }

    private double calculerSeuil(ACPResult resultatACP, int composantesConservees, boolean isGlobal) {
        double[][] alpha = Proj.calculerContributions(resultatACP.getU(), resultatACP.getVc());
        double[] alphaPertinents = extraireValeursAlphaPertinentes(alpha, composantesConservees);
        
        double mediane = calculerMedianeAbsolue(alphaPertinents);
        
        // Facteurs de seuillage ajustés
        double facteur = isGlobal 
            ? FACTEUR_SEUIL_GLOBAL
            : FACTEUR_SEUIL_LOCAL;
        
        // Estimer l'écart-type du bruit avant seuillage
        double sigma = mediane / 0.6745; // Estimation robuste de l'écart-type basée sur MAD
        double seuil = sigma * facteur;
        
        return seuil;
    }

    private double[] extraireValeursAlphaPertinentes(double[][] alpha, int composantesConservees) {
        List<Double> valeurs = new ArrayList<>();
        for (double[] ligne : alpha) {
            for (int j = 0; j < composantesConservees && j < ligne.length; j++) {
                valeurs.add(ligne[j]);
            }
        }
        return valeurs.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private void appliquerSeuillage(StrategieSeuillage strategie, ACPResult resultatACP, double seuil, int composantesConservees) {
        double[][] alpha = Proj.calculerContributions(resultatACP.getU(), resultatACP.getVc());
        double[][] alphaSeuille;
        
        // Ajuster le seuil pour le seuillage dur si nécessaire
        if (strategie instanceof StrategieSeuillageDur) {
            seuil = Seuillage.ajusterSeuilPourSeuillageDur(seuil);
        }
        
        // Appliquer le seuillage approprié
        if (strategie instanceof StrategieSeuillageDur) {
            alphaSeuille = Seuillage.seuillageDur(alpha, seuil);
        } else {
            alphaSeuille = Seuillage.seuillageDoux(alpha, seuil);
        }
        
        // Mise à zéro des composantes non conservées
        for (int m = 0; m < alphaSeuille.length; m++) {
            for (int n = composantesConservees; n < alphaSeuille[m].length; n++) {
                alphaSeuille[m][n] = 0;
            }
        }
        
        // Stockage de l'alpha seuillé dans le résultat ACP
        resultatACP.setAlphaSeuille(alphaSeuille);
    }

    // Reconstruction de l'image
    private BufferedImage reconstruireImage() {
        double[][][] canauxReconstruits = new double[3][][];
        
        for (int i = 0; i < 3; i++) {
            canauxReconstruits[i] = Reconstruction.reconstruireVecteurs(
                acpResults[i].getAlphaSeuille(),
                acpResults[i].getU(),
                acpResults[i].getmV()
            );
        }
        
        ReconstructionService service = new ReconstructionService();
        return service.reconstruireImageDepuisACP(
            canauxReconstruits[0], // Rouge
            canauxReconstruits[1], // Vert
            canauxReconstruits[2], // Bleu
            vecteurPatch.getVecteurs(),
            imageOriginale.getWidth(),
            imageOriginale.getHeight(),
            view.getPatchSize()
        );
    }

    // Traitement par imagettes pour le débruitage local
    private BufferedImage traiterImagettes(StrategieSeuillage strategie) {
        int taillePatch = view.getPatchSize();
        int pas = view.getPatchStep();
        BufferedImage imageResultat = new BufferedImage(
            imageOriginale.getWidth(), 
            imageOriginale.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );

        for (Imagette imagette : currentImagettes) {
            BufferedImage imagetteDebruitee = traiterImagette(imagette, strategie, taillePatch, pas);
            Point position = imagette.getPosition();
            
            Graphics2D g = imageResultat.createGraphics();
            g.drawImage(imagetteDebruitee, position.x, position.y, null);
            g.dispose();
        }
        
        return imageResultat;
    }

    private BufferedImage traiterImagette(Imagette imagette, StrategieSeuillage strategie, int taillePatch, int pas) {
        BufferedImage img = imagette.getImage();
        ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(img, pas, taillePatch);
        VecteurPatch vp = new VecteurPatch();
        vp.ajouterDepuisListe(patches);
        
        double[][][] canaux = new double[3][][];
        canaux[0] = vp.getCanal("R");
        canaux[1] = vp.getCanal("G");
        canaux[2] = vp.getCanal("B");
        
        double[][][] canauxReconstruits = new double[3][][];
        
        for (int i = 0; i < 3; i++) {
            ACPResult resultat = ACP.appliquerACP(canaux[i]);
            int composantesConservees = determinerComposantesAConserver(resultat);
            double seuil = calculerSeuil(resultat, composantesConservees, false);
            
            appliquerSeuillage(strategie, resultat, seuil, composantesConservees);
            
            canauxReconstruits[i] = Reconstruction.reconstruireVecteurs(
                resultat.getAlphaSeuille(),
                resultat.getU(),
                resultat.getmV()
            );
        }
        
        ReconstructionService service = new ReconstructionService();
        return service.reconstruireImageDepuisACP(
            canauxReconstruits[0], // Rouge
            canauxReconstruits[1], // Vert
            canauxReconstruits[2], // Bleu
            vp.getVecteurs(),
            img.getWidth(),
            img.getHeight(),
            taillePatch
        );
    }

    // Comparaison de qualité
    private void comparerQualiteImages() {
        if (view.getDenoisedImage() == null || imageOriginale == null) {
            view.showError("Impossible de comparer la qualité - aucune image débruitée disponible");
            return;
        }

        Pixel[][] pixelsOriginaux = convertirEnMatricePixels(imageOriginale);
        Pixel[][] pixelsDebruites = convertirEnMatricePixels(view.getDenoisedImage());
        
        String rapportQualite = QualiteImage.evaluerQualiteImage(pixelsOriginaux, pixelsDebruites);
        view.showQualityReport(rapportQualite);
    }

    private Pixel[][] convertirEnMatricePixels(BufferedImage image) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();
        Pixel[][] pixels = new Pixel[hauteur][largeur];
        
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                Color couleur = new Color(image.getRGB(x, y));
                pixels[y][x] = new Pixel(couleur.getRed(), couleur.getGreen(), couleur.getBlue());
            }
        }
        return pixels;
    }

    private double calculerMedianeAbsolue(double[] valeurs) {
        double[] valeursAbsolues = Arrays.stream(valeurs).map(Math::abs).toArray();
        Arrays.sort(valeursAbsolues);
        
        int longueur = valeursAbsolues.length;
        if (longueur % 2 == 0) {
            return (valeursAbsolues[longueur / 2 - 1] + valeursAbsolues[longueur / 2]) / 2.0;
        } else {
            return valeursAbsolues[longueur / 2];
        }
    }

    private void afficherMessageCompletion(StrategieSeuillage strategie, boolean isGlobal) {
        String methode = isGlobal ? "Global" : "Local";
        String nomStrategie = strategie instanceof StrategieSeuillageDur ? "dur" : "doux";
        view.showError("Débruitage " + methode.toLowerCase() + " terminé (seuillage " + nomStrategie + ")");
    }

    private void decouperImage() {
        try {
            if (imageOriginale == null) {
                view.showError("Aucune image chargée !");
                return;
            }

            int divisions = view.getNombreDivisions();

            if (divisions < 1) {
                view.showError("Le nombre de divisions doit être ≥ 1");
                return;
            }
            
            if (divisions > maxDivisions) {
                view.showError("Nombre de divisions trop grand (max " + maxDivisions + ")");
                return;
            }

            Photo photo = new Photo(imageBruitee, imageOriginale.getWidth(), imageOriginale.getHeight());
            List<Imagette> imagettes = DecoupeImage.decoupeImage(photo, divisions);
            currentImagettes = imagettes;

            if (!imagettes.isEmpty()) {
                int largeur = imagettes.get(0).getImage().getWidth();
                int hauteur = imagettes.get(0).getImage().getHeight();
                view.setPossiblePatchSizes(largeur, hauteur);
            }

            view.displayImagettes(imagettes);
            view.showError("Découpe réussie : " + divisions + " imagettes");
        } catch (Exception e) {
            view.showError("Erreur lors de la découpe");
            e.printStackTrace();
        }
    }

    private BufferedImage redimensionnerImage(BufferedImage imageOriginale, int targetWidth, int targetHeight) {
        BufferedImage imageRedimensionnee = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imageRedimensionnee.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(imageOriginale, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return imageRedimensionnee;
    }

    private interface StrategieSeuillage {
        double appliquerSeuil(double valeur, double seuil);
    }

    private static class StrategieSeuillageDur implements StrategieSeuillage {
        @Override
        public double appliquerSeuil(double valeur, double seuil) {
            return Math.abs(valeur) >= seuil ? valeur : 0.0;
        }
    }

    private static class StrategieSeuillageDoux implements StrategieSeuillage {
        @Override
        public double appliquerSeuil(double valeur, double seuil) {
            return Math.signum(valeur) * Math.max(Math.abs(valeur) - seuil, 0);
        }
    }
}
