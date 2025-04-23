package projetdenoising.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import projetdenoising.model.Pixel;
import projetdenoising.service.Bruit;
import projetdenoising.service.Convert;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainApp extends Application {
    private ImageView vueOriginale = new ImageView();
    private ImageView vueBruitee = new ImageView();
    private Pixel[][] matriceOriginale = null;
    private BufferedImage imageBruitee = null;
    private Slider sliderBruit = new Slider(0, 100, 20);
    private Button boutonEnregistrerSous = new Button("Enregistrer l'image bruitée");

    @Override
    public void start(Stage fenetrePrincipale) {
        fenetrePrincipale.setTitle("Image Bruitée");

        Button boutonSelectImage = new Button("Choisir une image");
        boutonSelectImage.setOnAction(e -> {
            FileChooser selecteurFichier = new FileChooser();
            selecteurFichier.setTitle("Sélectionnez une image");
            selecteurFichier.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png")
            );
            File fichier = selecteurFichier.showOpenDialog(fenetrePrincipale);
            if (fichier != null) {
                try {
                    BufferedImage imageOriginale = ImageIO.read(fichier);

                    // Redimensionner l'image à 256x256
                    BufferedImage imageRedimensionnee = Convert.redimensionnerImage256x256(imageOriginale);
                    
                    vueOriginale.setImage(SwingFXUtils.toFXImage(imageRedimensionnee, null));

                    // Convertir l'image redimensionnée en matrice
                    matriceOriginale = Convert.convertirImageEnMatrice(fichier.getAbsolutePath());
                    mettreAJourImageBruitee(sliderBruit.getValue());

                    boutonEnregistrerSous.setDisable(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        sliderBruit.setShowTickLabels(true);
        sliderBruit.setShowTickMarks(true);
        sliderBruit.setMajorTickUnit(25);
        sliderBruit.setMinorTickCount(5);
        sliderBruit.setBlockIncrement(5);

        sliderBruit.valueProperty().addListener((obs, ancienneValeur, nouvelleValeur) -> {
            if (matriceOriginale != null) {
                mettreAJourImageBruitee(nouvelleValeur.doubleValue());
            }
        });

        boutonEnregistrerSous.setDisable(true);
        boutonEnregistrerSous.setOnAction(e -> {
            FileChooser selecteurFichier = new FileChooser();
            selecteurFichier.setTitle("Enregistrer l'image bruitée");
            selecteurFichier.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
            File fichier = selecteurFichier.showSaveDialog(fenetrePrincipale);
            if (fichier != null && imageBruitee != null) {
                try {
                    ImageIO.write(imageBruitee, "png", fichier);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        vueOriginale.setFitWidth(300);
        vueOriginale.setPreserveRatio(true);
        vueBruitee.setFitWidth(300);
        vueBruitee.setPreserveRatio(true);

        HBox conteneurImages = new HBox(20, vueOriginale, vueBruitee);
        VBox miseEnPage = new VBox(10,
                boutonSelectImage,
                new Label("Niveau de bruit :"),
                sliderBruit,
                boutonEnregistrerSous,
                conteneurImages
        );
        miseEnPage.setPadding(new Insets(10));

        Scene scene = new Scene(miseEnPage, 700, 480);
        fenetrePrincipale.setScene(scene);
        fenetrePrincipale.show();
    }

    private void mettreAJourImageBruitee(double niveauBruit) {
        Pixel[][] matriceBruitee = Bruit.noising(matriceOriginale, niveauBruit);
        imageBruitee = Convert.convertirMatriceEnImage(matriceBruitee);
        vueBruitee.setImage(SwingFXUtils.toFXImage(imageBruitee, null));
    }

    public static void main(String[] args) {
        launch(args);
    }
}