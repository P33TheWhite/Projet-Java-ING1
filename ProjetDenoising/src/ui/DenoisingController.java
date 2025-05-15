package ui;

import javafx.stage.Stage;
import model.Pixel;
import model.Photo;
import model.Imagette;
import model.Patch;
import service.Bruit;
import service.Convert;
import service.DecoupeImage;
import service.ExtracteurPatch;
import service.VecteurPatch;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DenoisingController {

    private final MainView view;
    private Pixel[][] matriceOriginale;
    private BufferedImage imageBruitee;
    private BufferedImage imageOriginale;
    private int maxDivisions = 1;
    private List<Imagette> currentImagettes;
    private boolean isModeGlobal = false;

    public DenoisingController(MainView view) {
        this.view = view;
        initialize();
    }

    private void initialize() {
        view.setOnImageSelected(this::handleImageSelection);
        view.setOnNoiseChanged(this::updateNoisyImage);
        view.setOnSaveRequested(this::saveNoisyImage);
        view.setOnCutRequested(this::cutImage);
        view.setOnExtractPatchesRequested(this::extractPatches);
    }

    public void setModeGlobal(boolean modeGlobal) {
        this.isModeGlobal = modeGlobal;
        if (modeGlobal) {
            view.lancerModeGlobal();
        } else {
            view.lancerModeLocal();
        }
    }

    private void handleImageSelection(File file) {
        try {
            BufferedImage imageChargee = ImageIO.read(file);
            int largeur = imageChargee.getWidth();
            int hauteur = imageChargee.getHeight();
            int nouvelleLargeur = largeur - (largeur % 16);
            int nouvelleHauteur = hauteur - (hauteur % 16);

            if (nouvelleLargeur != largeur || nouvelleHauteur != hauteur) {
                imageOriginale = resizeImage(imageChargee, nouvelleLargeur, nouvelleHauteur);
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
            updateNoisyImage(view.getNoiseLevel());
            view.enableSave(true);
            view.enableCut(!isModeGlobal);

            int maxDivisionsWidth = imageOriginale.getWidth() / 16;
            int maxDivisionsHeight = imageOriginale.getHeight() / 16;
            maxDivisions = maxDivisionsWidth * maxDivisionsHeight;
            if (maxDivisions < 1) maxDivisions = 1;

        } catch (IOException e) {
            view.showError("Erreur de lecture de l'image");
            e.printStackTrace();
        }
    }

    private void updateNoisyImage(double noiseLevel) {
        if (matriceOriginale != null) {
            Pixel[][] matriceBruitee = Bruit.noising(matriceOriginale, noiseLevel);
            imageBruitee = Convert.convertirMatriceEnImage(matriceBruitee);
            view.setNoisyImage(imageBruitee);
        }
    }

    private void saveNoisyImage(File saveFile) {
        if (imageBruitee != null && saveFile != null) {
            try {
                ImageIO.write(imageBruitee, "png", saveFile);
                view.showError("Image sauvegardée avec succès");
            } catch (IOException e) {
                view.showError("Erreur lors de la sauvegarde");
                e.printStackTrace();
            }
        }
    }

    private void extractPatches() {
        int patchSize = view.getPatchSize();
        int step = view.getPatchStep();

        if (isModeGlobal) {
            if (imageBruitee == null) {
                view.showError("Veuillez d'abord charger une image et appliquer du bruit");
                return;
            }

            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(imageBruitee, step, patchSize);
            List<ArrayList<ArrayList<Patch>>> wrapper = new ArrayList<>();
            wrapper.add(patches);
            view.displayPatches(wrapper);

            // ✅ Vectorisation globale
            VecteurPatch vp = new VecteurPatch();
            vp.ajouterDepuisListe(patches);
            vp.afficherExtraits(10);  // Montre les 10 premiers pixels de chaque canal
            return;
        }

        // Sinon mode local
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            view.showError("Veuillez d'abord découper l'image en imagettes");
            return;
        }

        List<ArrayList<ArrayList<Patch>>> allPatches = new ArrayList<>();
        int imagetteIndex = 0;

        for (Imagette imagette : currentImagettes) {
            BufferedImage img = imagette.getImage();
            if (img.getWidth() < patchSize || img.getHeight() < patchSize) {
                view.showError("La taille de patch est trop grande pour certaines imagettes");
                return;
            }

            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(img, step, patchSize);
            allPatches.add(patches);

            // ✅ Vectorisation locale pour chaque imagette
            VecteurPatch vp = new VecteurPatch();
            vp.ajouterDepuisListe(patches);
            System.out.println(">>> Imagette " + imagetteIndex);
            vp.afficherExtraits(10);
            imagetteIndex++;
        }

        view.displayPatches(allPatches);
    }

    private void cutImage() {
        try {
            if (imageOriginale == null) {
                view.showError("Aucune image chargée !");
                return;
            }

            int divisions = view.getNombreDivisions();

            if (divisions < 2) {
                view.showError("Le nombre de divisions doit être ≥ 1");
                return;
            }
            if (divisions % 2 != 0) {
                view.showError("Le nombre de divisions doit être pair");
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
                int width = imagettes.get(0).getImage().getWidth();
                int height = imagettes.get(0).getImage().getHeight();
                view.setPossiblePatchSizes(width, height);
            }

            view.displayImagettes(imagettes);
            view.showError("Découpe réussie : " + divisions + " imagettes");
        } catch (Exception e) {
            view.showError("Erreur lors de la découpe");
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }
}
