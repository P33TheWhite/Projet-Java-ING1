package ui;

import javafx.stage.Stage;
import model.Pixel;
import model.Photo;
import model.Imagette;
import service.Bruit;
import service.Convert;
import service.DecoupeImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DenoisingController {

    private final MainView view;
    private Pixel[][] matriceOriginale;
    private BufferedImage imageBruitee;
    private BufferedImage imageOriginale;

    public DenoisingController(Stage stage) {
        this.view = new MainView(stage);
    }

    public void initialize() {
        view.setOnImageSelected(this::handleImageSelection);
        view.setOnNoiseChanged(this::updateNoisyImage);
        view.setOnSaveRequested(this::saveNoisyImage);
        view.setOnCutRequested(this::cutImage);
        view.show();
    }

    private void handleImageSelection(File file) {
        try {
            imageOriginale = ImageIO.read(file);
            matriceOriginale = Convert.convertirImageEnMatrice(file.getAbsolutePath());
            view.setOriginalImage(imageOriginale);
            updateNoisyImage(view.getNoiseLevel());
            view.enableSave(true);
            view.enableCut(true);
            view.showError(""); 
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

    private void cutImage() {
        try {
            int W = Integer.parseInt(view.getTailleImagette());
            int n = Integer.parseInt(view.getNombreImagettes());
            
            if (imageOriginale == null) {
                view.showError("Aucune image chargée !");
                return;
            }

            if (W <= 0 || n <= 0) {
                view.showError("W et n doivent être > 0");
                return;
            }

            int largeur = imageOriginale.getWidth();
            int hauteur = imageOriginale.getHeight();

            if (W > largeur || W > hauteur) {
                view.showError("La taille W dépasse les dimensions de l'image (" + largeur + "x" + hauteur + ")");
                return;
            }

            int maxImagettes = (largeur / W) * (hauteur / W);
            if (n > maxImagettes) {
                view.showError("Nombre trop élevé. Maximum possible: " + maxImagettes);
                return;
            }

            Photo photo = new Photo(imageBruitee, largeur, hauteur);
            List<Imagette> imagettes = DecoupeImage.decoupeImage(photo, W, n);
            view.displayImagettes(imagettes);
            view.showError("Découpe réussie : " + n + " imagettes de " + W + "x" + W);

        } catch (NumberFormatException e) {
            view.showError("Entrez des nombres valides pour W et n");
        } catch (Exception e) {
            view.showError("Erreur lors de la découpe");
            e.printStackTrace();
        }
    }
}