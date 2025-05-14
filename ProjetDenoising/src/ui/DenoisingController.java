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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DenoisingController {

    private final MainView view;
    private Pixel[][] matriceOriginale;
    private BufferedImage imageBruitee;
    private BufferedImage imageOriginale;
    private int maxDivisions = 1;
    private List<Imagette> currentImagettes;

    public DenoisingController(Stage stage) {
        this.view = new MainView(stage);
    }

    public void initialize() {
        view.setOnImageSelected(this::handleImageSelection);
        view.setOnNoiseChanged(this::updateNoisyImage);
        view.setOnSaveRequested(this::saveNoisyImage);
        view.setOnCutRequested(this::cutImage);
        view.setOnExtractPatchesRequested(this::extractPatchesFromImagettes);
        view.show();
    }

    private void handleImageSelection(File file) {
        try {
            BufferedImage imageChargee = ImageIO.read(file);

            // Vérifie si la largeur/hauteur est divisible par 16
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
            updateNoisyImage(view.getNoiseLevel());
            view.enableSave(true);
            view.enableCut(true);

            // Calcul du nombre maximal d'imagettes
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
    
    private void extractPatchesFromImagettes() {
        if (currentImagettes == null || currentImagettes.isEmpty()) {
            view.showError("Veuillez d'abord découper l'image en imagettes");
            return;
        }

        int patchSize = view.getPatchSize();
        int step = view.getPatchStep();

        // Vérifier que les paramètres sont valides
        for (Imagette imagette : currentImagettes) {
            BufferedImage img = imagette.getImage();
            if (img.getWidth() < patchSize || img.getHeight() < patchSize) {
                view.showError("La taille de patch est trop grande pour certaines imagettes");
                return;
            }
        }

        // Extraire les patchs de chaque imagette
        List<ArrayList<ArrayList<Patch>>> allPatches = new ArrayList<>();
        for (Imagette imagette : currentImagettes) {
            ArrayList<ArrayList<Patch>> patches = ExtracteurPatch.extractPatchs(
                imagette.getImage(), step, patchSize);

            // Afficher les positions des patchs extraits pour chaque imagette
            System.out.println("Extracting patches from Imagette...");
            for (ArrayList<Patch> lignePatchs : patches) {
                for (Patch patch : lignePatchs) {
                    int[] position = patch.getPosition();  // Récupérer la position du patch
                    System.out.println("Patch à la position : (" + position[0] + ", " + position[1] + ")");
                }
            }

            // Ajouter les patchs extraits à la liste de tous les patchs
            allPatches.add(patches);  // On ajoute simplement la liste des patchs sans l'imbriquer plus
        }

        // Afficher les patchs dans la vue
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
            currentImagettes = imagettes; // Stocker les imagettes pour extraction ultérieure
            view.displayImagettes(imagettes);
            
            // Calculer les tailles de patch possibles
            if (!imagettes.isEmpty()) {
                int minDimension = Math.min(
                    imagettes.get(0).getImage().getWidth(),
                    imagettes.get(0).getImage().getHeight()
                );
                view.setPossiblePatchSizes(minDimension);
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
