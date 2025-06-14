package ui;

import model.*;
import service.*;
import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DenoisingController {
    private static final double SEUIL_ENERGIE = 0.98;
    private static final double FACTEUR_SEUIL_GLOBAL = 0.6745;
    private static final double FACTEUR_SEUIL_LOCAL = 0.8094;
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
    private double[][][] canauxACP;
    private ACPResult[] acpResults;
    private double sigmaBruit;

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

            // Supprime l'image débruitée si elle existe
            if (imageDebruitee != null) {
            	Platform.runLater(() -> {
            	    imageDebruitee = null;
            	    view.setDenoisedImage(null);
            	});  // Actualise la vue pour enlever l'image débruitée
            }

            Photo photo = new Photo(imageOriginale, imageOriginale.getWidth(), imageOriginale.getHeight());
            matriceOriginale = Convert.convertirImageEnMatrice(photo);
            view.setOriginalImage(imageOriginale);
            view.setPossiblePatchSizes(imageOriginale.getWidth(), imageOriginale.getHeight());
            mettreAJourImageBruitee(view.getNoiseLevel());
            view.enableSave(true);
            view.enableCut(!isModeGlobal);

            maxDivisions = (imageOriginale.getWidth() / 16) * (imageOriginale.getHeight() / 16);
            if (maxDivisions < 1) maxDivisions = 1;

        } catch (IOException e) {
            view.showError("Erreur de lecture de l'image");
            e.printStackTrace();
        }
    }


    private void mettreAJourImageBruitee(double niveauBruit) {
        if (matriceOriginale != null) {
            new Thread(() -> {
                try {
                    Pixel[][] matriceBruitee = Bruit.noising(matriceOriginale, niveauBruit);
                    Platform.runLater(() -> {
                        imageBruitee = Convert.convertirMatriceEnImage(matriceBruitee);
                        view.setNoisyImage(imageBruitee);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        view.showError("Erreur lors de l'application du bruit");
                    });
                }
            }).start();
        }
    }

    private void sauvegarderImageBruitee(File fichierSauvegarde) {
        if (imageBruitee != null && fichierSauvegarde != null) {
            new Thread(() -> {
                try {
                    ImageIO.write(imageBruitee, "png", fichierSauvegarde);
                    Platform.runLater(() -> {
                        view.showError("Image sauvegardée avec succès");
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        view.showError("Erreur lors de la sauvegarde");
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void extrairePatchs() {
        int taillePatch = view.getPatchSize();
        int pas = view.getPatchStep();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (isModeGlobal) {
                        if (imageBruitee == null) {
                            throw new IllegalStateException("Veuillez d'abord charger une image et appliquer du bruit");
                        }

                        ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(imageBruitee, pas, taillePatch);
                        List<ArrayList<ArrayList<Patch>>> wrapper = new ArrayList<>();
                        wrapper.add(patches);

                        Platform.runLater(() -> {
                            view.displayPatches(wrapper);
                        });

                        vecteurPatch = new VecteurPatch();
                        vecteurPatch.ajouterDepuisListe(patches);
                        vecteurPatch.afficherExtraits(10);

                        canauxACP = new double[3][][];
                        canauxACP[0] = vecteurPatch.getCanal("R");
                        canauxACP[1] = vecteurPatch.getCanal("G");
                        canauxACP[2] = vecteurPatch.getCanal("B");

                        Platform.runLater(() -> {
                            view.enableDenoise(true);
                        });
                    } else {
                        if (currentImagettes == null || currentImagettes.isEmpty()) {
                            throw new IllegalStateException("Veuillez d'abord découper l'image en imagettes");
                        }

                        List<ArrayList<ArrayList<Patch>>> allPatches = new ArrayList<>();
                        
                        for (int i = 0; i < currentImagettes.size(); i++) {
                            Imagette imagette = currentImagettes.get(i);
                            BufferedImage img = imagette.getImage();
                            if (img.getWidth() < taillePatch || img.getHeight() < taillePatch) {
                                throw new IllegalStateException("La taille de patch est trop grande pour certaines imagettes");
                            }

                            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(img, pas, taillePatch);
                            allPatches.add(patches);
                            
                            final double progress = (double)(i + 1) / currentImagettes.size();
                        }

                        Platform.runLater(() -> {
                            view.displayPatches(allPatches);
                            view.enableDenoise(true);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> view.showError(e.getMessage()));
                    throw e;
                }
                return null;
            }
        };

        worker.execute();
    }

    private void effectuerDebruitage() {
        try {
            estimerSigmaBruit();
            SeuillageChoice choix = demanderChoixSeuillage();
            if (choix == null) return;


            SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    try {
                        if (isModeGlobal) {
                            validerPreconditionsPourDebruitageGlobal();
                            estimerSigmaBruit();
                            traiterTousLesCanaux(choix);
                            return imageDen();
                        } else {
                            validerPreconditionsPourDebruitageLocal();
                            return traiterImagettes(choix);
                        }
                    } finally {
                        
                    }
                }

                @Override
                protected void done() {
                    try {
                        imageDebruitee = get();
                        view.setDenoisedImage(imageDebruitee);
                        comparerQualiteImages();
                    } catch (InterruptedException | ExecutionException e) {
                        view.showError("Erreur lors du débruitage: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        view.completeProgress(); // Cache la barre de progression
                    }
                }
            };

            worker.execute();
        } catch (IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }


    


    
    /**
     * Estime le niveau de bruit sigma
     */
    private void estimerSigmaBruit() {
        // 1. Cas simple : l'utilisateur a ajouté du bruit 
        if (view.getNoiseLevel() > 0) {
            // Échelle linéaire entre 0 et 30 → proportion de bruit max sur [0–255]
            sigmaBruit = view.getNoiseLevel();
            return;
        }

        // 2. Estimation par ACP si possible
        if (acpResults != null && acpResults[0] != null) {
            try {
                double[][] alpha = Proj.calculerContributions(acpResults[0].getU(), acpResults[0].getVc());
                double[] alphaPertinents = extraireValeursAlphaPertinentes(alpha, 
                    determinerComposantesAConserver(acpResults[0]));
                double mediane = calculerMedianeAbsolue(alphaPertinents);
                sigmaBruit = mediane / 0.6745;

                if (sigmaBruit > 0 && sigmaBruit < 100) {
                    return;
                }
            } catch (Exception e) {
                System.err.println("Erreur dans l'estimation ACP du bruit: " + e.getMessage());
            }
        }

        // 3. Nouvelle estimation automatique sur image bruitée uniquement (sans image originale)
        if (view.getNoiseLevel() == 0 && imageBruitee != null) {
            try {
                sigmaBruit = EstimationBruit.estimerSigma(imageBruitee);
                if (sigmaBruit < 2.0) {
                    sigmaBruit = 2.0;
                }
                return;
            } catch (Exception e) {
                System.err.println("Erreur dans l'estimation locale du bruit: " + e.getMessage());
            }
        }

        // 4. Fallback final
        sigmaBruit = 10.0;
    }
    
    /**
     * Extrait les valeurs alpha pertinentes pour l'estimation du bruit
     * @param alpha Matrice des coefficients alpha
     * @param composantesConservees Nombre de composantes à conserver
     * @return Tableau des valeurs alpha pertinentes
     */
    private double[] extraireValeursAlphaPertinentes(double[][] alpha, int composantesConservees) {
        List<Double> valeurs = new ArrayList<>();
        for (double[] ligne : alpha) {
            for (int j = 0; j < composantesConservees && j < ligne.length; j++) {
                valeurs.add(ligne[j]);
            }
        }
        return valeurs.stream().mapToDouble(Double::doubleValue).toArray();
    }

    
    /**
     * Demande à l'utilisateur de choisir le type de seuillage
     * @return Le choix de seuillage ou null si annulé
     */
    private SeuillageChoice demanderChoixSeuillage() {
        List<String> types = Arrays.asList("Seuillage dur", "Seuillage doux");
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Seuillage dur", types);
        typeDialog.setTitle("Choix du seuillage");
        typeDialog.setHeaderText("Sélectionnez le type de seuillage à appliquer");
        Optional<String> typeResult = typeDialog.showAndWait();
        if (!typeResult.isPresent()) return null;

        List<String> methodes = Arrays.asList("Seuil universel (VisuShrink)", "Seuil bayésien (BayesShrink)");
        ChoiceDialog<String> methodeDialog = new ChoiceDialog<>("Seuil universel (VisuShrink)", methodes);
        methodeDialog.setTitle("Choix de la méthode de seuil");
        methodeDialog.setHeaderText("Sélectionnez comment calculer le seuil");
        Optional<String> methodeResult = methodeDialog.showAndWait();
        if (!methodeResult.isPresent()) return null;
        view.startProgress();
        

        return new SeuillageChoice(
            typeResult.get().equals("Seuillage dur"),
            methodeResult.get().contains("universel")
        );
    }

    /**
     * Traite tous les canaux couleur avec ACP et seuillage
     * @param choix Le choix de seuillage
     */
    private void traiterTousLesCanaux(SeuillageChoice choix) {
        acpResults = new ACPResult[3];
        
        for (int i = 0; i < 3; i++) {
            acpResults[i] = ACP.moyCov(canauxACP[i]);
            int composantesConservees = determinerComposantesAConserver(acpResults[i]);
            
            double seuil = calculerSeuil(acpResults[i], composantesConservees, choix);
            
            double[][] alpha = Proj.calculerContributions(acpResults[i].getU(), acpResults[i].getVc());
            double[][] alphaSeuille = appliquerSeuillage(alpha, seuil, choix);
            acpResults[i].setAlphaSeuille(alphaSeuille);
        }
    }

    /**
     * Détermine combien de composantes principales conserver
     * @param resultatACP Le résultat de l'ACP
     * @return Le nombre de composantes à conserver
     */
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

    /**
     * Calcule le seuil de bruit pour un canal
     * @param resultatACP Le résultat de l'ACP pour le canal
     * @param composantesConservees Nombre de composantes conservées
     * @param choix Le choix de seuillage
     * @return Le seuil calculé
     */
    private double calculerSeuil(ACPResult resultatACP, int composantesConservees, SeuillageChoice choix) {
        double[][] alpha = Proj.calculerContributions(resultatACP.getU(), resultatACP.getVc());
        double[] alphaPertinents = extraireValeursAlphaPertinentes(alpha, composantesConservees);
        
        if (choix.estUniversel) {
            // VisuShrink avec ajustement
            int L = alphaPertinents.length;
            double seuilBase = Seuillage.seuilV(sigmaBruit, L);
            return choix.estDur ? seuilBase * 1.2 : seuilBase;
        } else {
            // BayesShrink avec ajustement
            double sigmaXb2 = calculerVariance(alphaPertinents);
            double seuilBase = Seuillage.seuilB(sigmaBruit*sigmaBruit, sigmaXb2);
            return choix.estDur ? seuilBase * 1.1 : seuilBase;
        }
    }

    /**
     * Applique le seuillage aux coefficients alpha
     * @param alpha Les coefficients alpha
     * @param seuil Le seuil à appliquer
     * @param choix Le choix de seuillage
     * @return Les coefficients alpha après seuillage
     */
    private double[][] appliquerSeuillage(double[][] alpha, double seuil, SeuillageChoice choix) {
        if (choix.estDur) {
            return Seuillage.seuillageDur(alpha, seuil * 1.2);
        } else {
            return Seuillage.seuillageDoux(alpha, seuil);
        }
    }

    /**
     * Calcule la variance d'un ensemble de valeurs
     * @param valeurs Les valeurs à analyser
     * @return La variance calculée
     */
    private double calculerVariance(double[] valeurs) {
        double moyenne = Arrays.stream(valeurs).average().orElse(0);
        return Arrays.stream(valeurs)
                   .map(v -> (v - moyenne) * (v - moyenne))
                   .average()
                   .orElse(0);
    }

    /**
     * Reconstruit l'image à partir des canaux traités
     * @return L'image reconstruite
     */
    private BufferedImage imageDen() {
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
            canauxReconstruits[0],
            canauxReconstruits[1],
            canauxReconstruits[2],
            vecteurPatch.getVecteurs(),
            imageOriginale.getWidth(),
            imageOriginale.getHeight(),
            view.getPatchSize()
        );
    }

    

    /**
     * Traite toutes les imagettes pour le débruitage local
     * @param choix Le choix de seuillage
     * @return L'image débruitée reconstruite
     */
    private BufferedImage traiterImagettes(SeuillageChoice choix) {
        int taillePatch = view.getPatchSize();
        int pas = view.getPatchStep();
        BufferedImage imageResultat = new BufferedImage(
            imageOriginale.getWidth(), 
            imageOriginale.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );

        for (Imagette imagette : currentImagettes) {
            BufferedImage imagetteDebruitee = traiterImagette(imagette, choix, taillePatch, pas);
            Point position = imagette.getPosition();
            
            Graphics2D g = imageResultat.createGraphics();
            g.drawImage(imagetteDebruitee, position.x, position.y, null);
            g.dispose();
        }
        
        return imageResultat;
    }

    /**
     * Traite une imagette individuelle
     * @param imagette L'imagette à traiter
     * @param choix Le choix de seuillage
     * @param taillePatch La taille des patchs
     * @param pas Le pas d'extraction des patchs
     * @return L'imagette débruitée
     */
    private BufferedImage traiterImagette(Imagette imagette, SeuillageChoice choix, int taillePatch, int pas) {
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
            ACPResult resultat = ACP.moyCov(canaux[i]);
            int composantesConservees = determinerComposantesAConserver(resultat);
            double seuil = calculerSeuil(resultat, composantesConservees, choix);
            
            double[][] alpha = Proj.calculerContributions(resultat.getU(), resultat.getVc());
            double[][] alphaSeuille = appliquerSeuillage(alpha, seuil, choix);
            resultat.setAlphaSeuille(alphaSeuille);
            
            canauxReconstruits[i] = Reconstruction.reconstruireVecteurs(
                alphaSeuille,
                resultat.getU(),
                resultat.getmV()
            );
        }
        
        ReconstructionService service = new ReconstructionService();
        return service.reconstruireImageDepuisACP(
            canauxReconstruits[0],
            canauxReconstruits[1],
            canauxReconstruits[2],
            vp.getVecteurs(),
            img.getWidth(),
            img.getHeight(),
            taillePatch
        );
    }

    /**
     * Compare la qualité entre l'image originale et l'image débruitée
     */
    private void comparerQualiteImages() {
    	String rapportQualite = new String();
        if (view.getDenoisedImage() == null || imageOriginale == null) {
            view.showError("Impossible de comparer la qualité - aucune image débruitée disponible");
            return;
        }

        Pixel[][] pixelsOriginaux = convertirEnMatricePixels(imageOriginale);
        Pixel[][] pixelsDebruites = convertirEnMatricePixels(view.getDenoisedImage());
        
        if (view.getNoiseLevel() > 0) {
        	rapportQualite = QualiteImage.evaluerQualiteImage(pixelsOriginaux, pixelsDebruites);
        } else {
            rapportQualite = QualiteImage.interpretationNR_IQA(pixelsDebruites);
        }
        view.showQualityReport(rapportQualite);

    }

    /**
     * Convertit une BufferedImage en matrice de pixels
     * @param image L'image à convertir
     * @return La matrice de pixels correspondante
     */
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

    /**
     * Valide les préconditions pour le débruitage global
     * @throws IllegalStateException Si les préconditions ne sont pas remplies
     */
    private void validerPreconditionsPourDebruitageGlobal() {
        if (vecteurPatch == null || canauxACP == null) {
            throw new IllegalStateException("Veuillez d'abord extraire les patchs");
        }
    }

    /**
     * Valide les préconditions pour le débruitage local
     * @throws IllegalStateException Si les préconditions ne sont pas remplies
     */
    private void validerPreconditionsPourDebruitageLocal() {
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            throw new IllegalStateException("Veuillez d'abord découper l'image en imagettes");
        }
    }

    

    /**
     * Découpe l'image en imagettes selon le nombre de divisions spécifié
     */
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

    /**
     * Redimensionne une image aux dimensions spécifiées
     * @param imageOriginale L'image originale
     * @param targetWidth La largeur cible
     * @param targetHeight La hauteur cible
     * @return L'image redimensionnée
     */
    private BufferedImage redimensionnerImage(BufferedImage imageOriginale, int targetWidth, int targetHeight) {
        BufferedImage imageRedimensionnee = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imageRedimensionnee.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(imageOriginale, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return imageRedimensionnee;
    }

    /**
     * Calcule la médiane absolue d'un tableau de valeurs
     * @param valeurs Les valeurs à analyser
     * @return La médiane absolue
     */
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

    /**
     * Classe interne pour représenter le choix de seuillage
     */
    private static class SeuillageChoice {
        final boolean estDur;
        final boolean estUniversel;

        SeuillageChoice(boolean estDur, boolean estUniversel) {
            this.estDur = estDur;
            this.estUniversel = estUniversel;
        }
    }
}