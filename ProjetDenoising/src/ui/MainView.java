package ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import model.Imagette;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class MainView {
    private final Stage stage;
    private final ImageView originalView = new ImageView();
    private final ImageView noisyView = new ImageView();
    private final Slider noiseSlider = new Slider(0, 30, 10);
    private final Button saveButton = new Button("Enregistrer l'image bruitée");
    private final Button cutButton = new Button("Découper l'image");
    private final TextField tailleImagetteField = new TextField("100");
    private final TextField nombreImagettesField = new TextField("9");
    private final Label errorLabel = new Label();
    private final TilePane imagettesPane = new TilePane();

    private Consumer<File> onImageSelected;
    private Consumer<Double> onNoiseChanged;
    private Consumer<File> onSaveRequested;
    private Runnable onCutRequested;

    public MainView(Stage stage) {
        this.stage = stage;
        setupUI();
    }

    private void setupUI() {
        stage.setTitle("Découpe d'images dynamique");

        // Configure les TextField pour n'accepter que des nombres
        tailleImagetteField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        nombreImagettesField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));

        Button selectImage = new Button("Choisir une image");
        selectImage.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Sélectionnez une image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
            File file = chooser.showOpenDialog(stage);
            if (file != null && onImageSelected != null) {
                onImageSelected.accept(file);
            }
        });

        noiseSlider.setShowTickLabels(true);
        noiseSlider.setShowTickMarks(true);
        noiseSlider.setMajorTickUnit(10);        // Add this
        noiseSlider.setMinorTickCount(0);        // Optional: remove minor ticks
        noiseSlider.setBlockIncrement(10);
        noiseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (onNoiseChanged != null) {
                onNoiseChanged.accept(newVal.doubleValue());
            }
        });

        saveButton.setDisable(true);
        saveButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer l'image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            File file = chooser.showSaveDialog(stage);
            if (file != null && onSaveRequested != null) {
                onSaveRequested.accept(file);
            }
        });

        cutButton.setDisable(true);
        cutButton.setOnAction(e -> {
            if (onCutRequested != null) {
                onCutRequested.run();
            }
        });

        // Configuration des ImageView
        originalView.setFitWidth(300);
        originalView.setPreserveRatio(true);
        noisyView.setFitWidth(300);
        noisyView.setPreserveRatio(true);

        // Configuration du TilePane pour les imagettes
        imagettesPane.setHgap(10);
        imagettesPane.setVgap(10);
        imagettesPane.setPadding(new Insets(10));
        imagettesPane.setPrefColumns(3);

        // Organisation des contrôles
        HBox imageControls = new HBox(10, 
                new Label("Taille (W):"), tailleImagetteField,
                new Label("Nombre:"), nombreImagettesField,
                cutButton
        );
        imageControls.setAlignment(Pos.CENTER_LEFT);

        HBox imageContainer = new HBox(20, originalView, noisyView);
        
        VBox layout = new VBox(10,
                selectImage,
                new Label("Niveau de bruit:"),
                noiseSlider,
                new HBox(10, saveButton),
                imageControls,
                errorLabel,
                imageContainer,
                new Label("Imagettes générées:"),
                imagettesPane
        );
        layout.setPadding(new Insets(15));

        // Style de l'erreur
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        stage.setScene(new Scene(layout, 800, 700));
    }

    public void show() {
        stage.show();
    }

    // Getters pour les valeurs des champs
    public String getTailleImagette() {
        return tailleImagetteField.getText();
    }

    public String getNombreImagettes() {
        return nombreImagettesField.getText();
    }

    public double getNoiseLevel() {
        return noiseSlider.getValue();
    }

    // Méthodes d'affichage
    public void setOriginalImage(BufferedImage image) {
        originalView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void setNoisyImage(BufferedImage image) {
        noisyView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void displayImagettes(List<Imagette> imagettes) {
        imagettesPane.getChildren().clear();
        for (Imagette imagette : imagettes) {
            ImageView iv = new ImageView(SwingFXUtils.toFXImage(imagette.getImage(), null));
            iv.setFitWidth(100);
            iv.setPreserveRatio(true);
            imagettesPane.getChildren().add(iv);
        }
    }

    // Gestion des événements
    public void setOnImageSelected(Consumer<File> handler) {
        this.onImageSelected = handler;
    }

    public void setOnNoiseChanged(Consumer<Double> handler) {
        this.onNoiseChanged = handler;
    }

    public void setOnSaveRequested(Consumer<File> handler) {
        this.onSaveRequested = handler;
    }

    public void setOnCutRequested(Runnable handler) {
        this.onCutRequested = handler;
    }

    // Activation des boutons
    public void enableSave(boolean enabled) {
        saveButton.setDisable(!enabled);
    }

    public void enableCut(boolean enabled) {
        cutButton.setDisable(!enabled);
    }

    // Affichage des erreurs
    public void showError(String message) {
        errorLabel.setText(message);
    }
}